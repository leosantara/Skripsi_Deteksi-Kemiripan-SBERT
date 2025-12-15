# similarity_service.py
from fastapi import FastAPI, HTTPException, BackgroundTasks
from pydantic import BaseModel
from typing import List, Optional, Dict, Any
import uvicorn
import numpy as np
import pandas as pd
import faiss
import time
import pickle
import os
import json
import traceback
import re
from sentence_transformers import SentenceTransformer
from sklearn.cluster import KMeans
from Sastrawi.StopWordRemover.StopWordRemoverFactory import StopWordRemoverFactory
from Sastrawi.Stemmer.StemmerFactory import StemmerFactory
from sklearn.feature_extraction.text import TfidfVectorizer

# IMPORT MODUL LOKAL (Pastikan file Preprocessing.py ada di folder yang sama)
from Preprocessing import preprocess_and_split_proposal

# ===================== 1. KONFIGURASI & PATH =====================
# Sesuaikan path ini dengan folder di laptopmu
BASE_DIR = r"D:\Media\Kuliah\Skripsi\ScripTI\Versi 4\Index_and_Clusters"

INDEX_PATH = os.path.join(BASE_DIR, "proposal_vectors.index")
EMB_META_PATH = os.path.join(BASE_DIR, "proposal_metadata.pkl")
CLUSTER_MODEL_PATH = os.path.join(BASE_DIR, "topic_cluster_model.pkl")
CLUSTER_MAP_PATH = os.path.join(BASE_DIR, "proposal_clusters.csv")
CLUSTER_INFO_PATH = os.path.join(BASE_DIR, "cluster_info_complete.json")

# Path Model SBERT Final (Hasil Training Terbaik)
MODEL_PATH = r"D:\Media\Kuliah\Skripsi\ScripTI\Versi 4\Model\Trained_SBERT\finetuned_all-nusabert-base_v1"

# Konstanta Kolom CSV
PID_COL = "proposal_id"
ASPECT_COL = "aspect"
SENT_COL = "text"

# Konstanta Bisnis
THRESHOLD_SIMILARITY = 0.50
MIN_MATCH_FOR_UI = 0.40
MAX_MATCHES_PER_PROPOSAL = 20

# ===================== 2. DTO / SCHEMAS =====================

class ProposalPayload(BaseModel):
    proposal_id: int
    judul: str
    latar_belakang: Optional[str] = ""
    rumusan: Optional[str] = ""
    tujuan: Optional[str] = ""

class SimilarityMatch(BaseModel):
    aspectInput: str
    inputSentence: str
    aspectDb: str
    dbSentence: str
    similarity: float

# DTO untuk menerima data mentah dari Spring Boot (Re-index)
class ProposalRawData(BaseModel):
    proposal_id: int
    judul: str
    latar_belakang: str
    rumusan: str
    tujuan: str
    id_dosen_1: Optional[int] = None
    id_dosen_2: Optional[int] = None

# DTO Rekomendasi Dosen (Split Dosen 1 & 2)
class DosenRecommendation(BaseModel):
    clusterId: int
    topicLabel: str 
    ratio: float
    recommendedDosen1Ids: List[int]
    recommendedDosen2Ids: List[int]

class SimilarityProposal(BaseModel):
    proposalId: int
    score: float
    judul: str
    totalSentences: int
    similarCount: int
    ignoredCount: int
    matches: Optional[List[SimilarityMatch]] = []

class SimilarityResponse(BaseModel):
    queryProposalId: int
    modelName: str
    topK: int
    results: List[SimilarityProposal]
    dosenRecommendations: Optional[List[DosenRecommendation]] = None

# ===================== 3. ENGINE LOADER =====================

def init_engine():
    print("=== INITIALIZING AI ENGINE ===")
    
    # 1. Load Model SBERT
    print(f"Loading Model: {MODEL_PATH}")
    try:
        model = SentenceTransformer(MODEL_PATH)
    except Exception as e:
        print(f"❌ Gagal Load Model: {e}")
        raise e
    
    # 2. Load FAISS Index
    print(f"Loading Index: {INDEX_PATH}")
    try:
        index = faiss.read_index(INDEX_PATH)
    except:
        print("⚠️ Index belum ada. Sistem berjalan tanpa index (harus re-index dulu).")
        index = None
    
    # 3. Load Metadata
    print(f"Loading Metadata: {EMB_META_PATH}")
    try:
        meta = pd.read_pickle(EMB_META_PATH)
    except:
        meta = pd.DataFrame()
    
    # 4. Load Clustering Assets
    print(f"Loading Cluster Model: {CLUSTER_MODEL_PATH}")
    try:
        with open(CLUSTER_MODEL_PATH, 'rb') as f:
            kmeans = pickle.load(f)
    except:
        kmeans = None
        
    # 5. Load Cluster Info (Topik & Dosen)
    print(f"Loading Cluster Info: {CLUSTER_INFO_PATH}")
    try:
        with open(CLUSTER_INFO_PATH, 'r') as f:
            cluster_info = json.load(f)
    except:
        print("⚠️ Cluster Info belum ada.")
        cluster_info = {}
    
    return {
        "model": model, 
        "index": index, 
        "meta": meta,
        "kmeans": kmeans,
        "cluster_info": cluster_info
    }

ENGINE = init_engine()

# Tambahkan ini di bawah ENGINE = init_engine()
INDEXING_STATUS = {
    "is_running": False,
    "progress_percent": 0.0,
    "message": "Idle. Siap menerima permintaan re-index.",
    "last_update_time": time.time(),
    "start_time": None
}

# ===================== 4. HELPER FUNCTIONS =====================

def aspects_compatible(asp_q: str, asp_db: str) -> bool:
    """Cek kesesuaian aspek (misal Judul vs Judul)"""
    if not asp_q or not asp_db: return False
    def clean(s): return re.sub(r'[\s_]+', '', str(s).lower())
    a1 = clean(asp_q)
    a2 = clean(asp_db)
    # Mapping alias
    aliases = {"rumusan": "rumusanmasalah", "rumusanmasalah": "rumusan"}
    if a1 == a2: return True
    if aliases.get(a1) == a2: return True
    if aliases.get(a2) == a1: return True
    return False

def get_dosen_recommendation(emb_q: np.ndarray) -> List[DosenRecommendation]:
    """Logika Voting Cluster untuk Rekomendasi Dosen"""
    kmeans = ENGINE["kmeans"]
    cluster_info = ENGINE.get("cluster_info", {})
    
    if not kmeans: return []

    # Prediksi cluster setiap kalimat
    sentence_labels = kmeans.predict(emb_q)
    
    # Hitung voting
    unique, counts = np.unique(sentence_labels, return_counts=True)
    total = len(sentence_labels)
    
    recs = []
    sorted_indices = np.argsort(-counts) # Urutkan dari terbanyak
    
    # Ambil Top 2 Cluster
    for i in sorted_indices[:2]:
        cluster_id = int(unique[i])
        ratio = counts[i] / total
        
        if ratio < 0.15: continue # Ambang batas minimal 15%
        
        # Ambil info dari JSON
        info = cluster_info.get(str(cluster_id), {})
        topic_name = info.get("name", f"Topik Kelompok {cluster_id}")
        d1_ids = info.get("dosen1", [])
        d2_ids = info.get("dosen2", [])
        
        recs.append(DosenRecommendation(
            clusterId=cluster_id,
            topicLabel=f"Topik: {topic_name}",
            ratio=float(ratio),
            recommendedDosen1Ids=d1_ids,
            recommendedDosen2Ids=d2_ids
        ))
        
    return recs

# ===================== 5. CORE LOGIC (SIMILARITY CHECK) =====================

def process_similarity(proposal: ProposalPayload, top_k: int = 5):
    model = ENGINE["model"]
    index = ENGINE["index"]
    meta = ENGINE["meta"]
    
    # Cek kesiapan engine
    if index is None or meta.empty:
        return SimilarityResponse(
            queryProposalId=proposal.proposal_id,
            modelName="ScripTI-Error",
            topK=top_k, results=[], dosenRecommendations=[]
        )

    # 1. Preprocessing
    payload_dict = proposal.dict()
    sentences_data = preprocess_and_split_proposal(payload_dict)
    
    if not sentences_data:
        return SimilarityResponse(queryProposalId=proposal.proposal_id, modelName="ScripTI-SBERT", topK=top_k, results=[], dosenRecommendations=[])
        
    texts_q = [x['text'] for x in sentences_data]
    aspects_q = [x['aspect'] for x in sentences_data]
    num_q = len(texts_q)
    
    # 2. Encoding Input
    if "e5" in MODEL_PATH.lower():
        texts_input = ["query: " + t for t in texts_q]
    else:
        texts_input = texts_q
        
    emb_q = model.encode(texts_input, show_progress_bar=False)
    emb_q = np.array(emb_q).astype('float32')
    faiss.normalize_L2(emb_q)
    
    # 3. FAISS Search
    # Cari 50 kandidat per kalimat
    D, I = index.search(emb_q, k=50)
    
    # 4. Scoring Coverage
    candidates_scores = {}
    
    for i in range(num_q):
        for rank, idx in enumerate(I[i]):
            if idx < 0: continue
            score = float(D[i][rank])
            score = max(0.0, min(1.0, score))
            
            # Ambil PID dari metadata
            row = meta.iloc[idx] 
            pid = int(row[PID_COL])
            
            if pid == proposal.proposal_id: continue
            
            # Cek Aspek
            asp_db = row[ASPECT_COL]
            if not aspects_compatible(aspects_q[i], asp_db): continue
            
            if pid not in candidates_scores:
                candidates_scores[pid] = np.zeros(num_q)
            
            # Simpan skor tertinggi untuk kalimat ke-i
            if score > candidates_scores[pid][i]:
                candidates_scores[pid][i] = score

    final_results = []
    for pid, scores_array in candidates_scores.items():
        similar_count = np.sum(scores_array >= THRESHOLD_SIMILARITY)
        ignored_count = num_q - similar_count
        coverage_score = float(similar_count) / float(num_q)
        
        if coverage_score <= 0.0: continue
        
        # Ambil Judul (Fallback)
        try:
            judul_db = meta[meta[PID_COL] == str(pid)].iloc[0]['text']
        except:
            try:
                 judul_db = meta[meta[PID_COL] == pid].iloc[0]['text']
            except:
                 judul_db = f"Proposal #{pid}"

        final_results.append(SimilarityProposal(
            proposalId=pid, score=coverage_score, judul=str(judul_db)[:200],
            totalSentences=num_q, similarCount=int(similar_count), ignoredCount=int(ignored_count),
            matches=[]
        ))
    
    final_results.sort(key=lambda x: x.score, reverse=True)
    top_results = final_results[:top_k]
    
    # 6. Enrich Detail (Re-Encode untuk Akurasi)
    for res in top_results:
        pid = res.proposalId
        best_matches = []
        
        # Filter metadata pakai string ID
        rows_db = meta[meta[PID_COL] == str(pid)]
        if rows_db.empty: rows_db = meta[meta[PID_COL] == pid]
        if rows_db.empty: continue
        
        texts_db = rows_db[SENT_COL].astype(str).tolist()
        aspects_db = rows_db[ASPECT_COL].astype(str).tolist()
        
        # Encode lokal
        if "e5" in MODEL_PATH.lower():
            texts_db_input = ["passage: " + t for t in texts_db]
        else:
            texts_db_input = texts_db
            
        emb_db_local = model.encode(texts_db_input, show_progress_bar=False)
        emb_db_local = np.array(emb_db_local).astype('float32')
        faiss.normalize_L2(emb_db_local)
        
        sim_matrix = np.dot(emb_q, emb_db_local.T)
        
        for i in range(num_q):
            best_idx_local = np.argmax(sim_matrix[i])
            best_score = float(sim_matrix[i][best_idx_local])
            
            if best_score < MIN_MATCH_FOR_UI: continue
            if not aspects_compatible(aspects_q[i], aspects_db[best_idx_local]): continue
                
            best_matches.append(SimilarityMatch(
                aspectInput=aspects_q[i], inputSentence=texts_q[i],
                aspectDb=aspects_db[best_idx_local], dbSentence=texts_db[best_idx_local],
                similarity=best_score
            ))
            
        best_matches.sort(key=lambda x: x.similarity, reverse=True)
        res.matches = best_matches[:MAX_MATCHES_PER_PROPOSAL]

    # 7. Generate Rekomendasi Dosen
    dosen_recs = get_dosen_recommendation(emb_q)

    return SimilarityResponse(
        queryProposalId=proposal.proposal_id,
        modelName="ScripTI-NusaBERT-Coverage",
        topK=top_k, results=top_results, dosenRecommendations=dosen_recs
    )

# ===================== 6. RE-INDEXING TASK (LOGIKA UPDATE DATA) =====================

def run_reindexing_task(raw_data: List[ProposalRawData]):
    global ENGINE
    global INDEXING_STATUS
    print("🔄 MEMULAI PROSES RE-INDEXING (V2: Auto-Naming & Split Dosen)...")
    
    # ----------------------------------------------------
    # START: Reset Status
    INDEXING_STATUS["is_running"] = True
    INDEXING_STATUS["progress_percent"] = 0.0
    INDEXING_STATUS["message"] = "Starting preprocessing and data conversion..."
    INDEXING_STATUS["start_time"] = time.time()
    # ----------------------------------------------------
    
    try:
        # A. PREPROCESSING
        text_rows = []
        dosen_rows = []
        
        for p in raw_data:
            dosen_rows.append({
                "proposal_id": str(p.proposal_id),
                "id_dosen_1": p.id_dosen_1,
                "id_dosen_2": p.id_dosen_2,
                "judul": p.judul
            })
            payload_pre = {
                "proposal_id": p.proposal_id, "judul": p.judul,
                "latar_belakang": p.latar_belakang, "rumusan": p.rumusan, "tujuan": p.tujuan
            }
            sentences = preprocess_and_split_proposal(payload_pre)
            text_rows.extend(sentences)
            
        print(f"   -> Total Proposal: {len(raw_data)} | Total Kalimat: {len(text_rows)}")
        
        df_texts = pd.DataFrame(text_rows)
        df_dosen = pd.DataFrame(dosen_rows)
        df_texts['text'] = df_texts['text'].astype(str)
        df_texts['proposal_id'] = df_texts['proposal_id'].astype(str)
        
        # Backup CSV
        df_texts.to_csv(os.path.join(BASE_DIR, "dataTuning_updated.csv"), index=False, encoding='utf-8-sig')
        # ----------------------------------------------------
        INDEXING_STATUS["progress_percent"] = 15.0
        INDEXING_STATUS["message"] = "Encoding vectors (SBERT inference)..."
        # ----------------------------------------------------
        # B. ENCODING
        model = ENGINE["model"]
        sentences_list = df_texts['text'].tolist()
        if "e5" in MODEL_PATH.lower():
            sentences_input = ["query: " + s for s in sentences_list]
        else:
            sentences_input = sentences_list
            
        print("   -> Encoding vectors...")
        embeddings = model.encode(sentences_input, batch_size=64, show_progress_bar=True, convert_to_tensor=False)
        embeddings = np.array(embeddings).astype('float32')
        faiss.normalize_L2(embeddings)
        # ----------------------------------------------------
        INDEXING_STATUS["progress_percent"] = 40.0
        INDEXING_STATUS["message"] = "Building FAISS index and saving metadata..."
        # ----------------------------------------------------
        
        # C. INDEXING
        print("   -> Rebuilding Index...")
        d = embeddings.shape[1]
        new_index = faiss.IndexFlatIP(d)
        new_index.add(embeddings)
        faiss.write_index(new_index, INDEX_PATH)
        df_texts.to_pickle(EMB_META_PATH)
        # ----------------------------------------------------
        INDEXING_STATUS["progress_percent"] = 75.0
        INDEXING_STATUS["message"] = "Generating topic names and mapping supervisors..."
        # ----------------------------------------------------
        
        # D. CLUSTERING
        print("   -> Re-Clustering...")
        num_clusters = 20
        kmeans = KMeans(n_clusters=num_clusters, random_state=42, n_init=10)
        kmeans.fit(embeddings)
        with open(CLUSTER_MODEL_PATH, 'wb') as f: pickle.dump(kmeans, f)
        df_texts['cluster_label'] = kmeans.labels_
        
# E. TOPIC NAMING (SMART NORMALIZATION + TF-IDF)
        print("   -> Generating Topics (Smart Normalization)...")
        
        # 1. Setup Stopwords (Updated dengan kata-kata 'yapping' baru)
        factory = StopWordRemoverFactory()
        stopwords_sastrawi = factory.get_stop_words()
        
        academic_stopwords = [
            # Kata Sambung & Umum Baru (Yapping)
            'banyak', 'perkembangan', 'mengalami', 'terjadi', 'sering', 'kerap', 'sekarang',
            'selama', 'masa', 'kini', 'saat', 'ini', 'itu', 'tersebut', 'berbagai',
            'merupakan', 'adalah', 'yaitu', 'yakni', 'antara', 'lain', 'sangat',
            'serta', 'ataupun', 'bagai', 'bagaimana', 'sebagai', 'banyak', 'sedikit',
            'berdasarkan', 'melalui', 'secara', 'suatu', 'sebuah', 'jadi', 'lintas', 
            'pula', 'pun', 'dapat', 'bisa', 'mampu', 'guna', 'agar', 'semakin', 'beberapa',
            
            # Kata Kerja Umum, kata sifat
            'menggunakan', 'digunakan', 'dilakukan', 'melakukan', 'membuat', 'membangun','kehidupan',
            'merancang', 'mengimplementasikan', 'penerapan', 'implementasi', 'pembuatan', 'masyarakat',
            'pembangunan', 'perancangan', 'pengembangan', 'analisis', 'menganalisis', 'orang',
            'menentukan', 'menghitung', 'mencari', 'diterapkan', 'dibuat', 'dikembangkan', 'satunama',
            'membantu', 'mengetahui', 'meningkatkan', 'diharapkan', 'perlu', 'berkembang',
            'menyelesaikan', 'permasalahan', 'solusi', 'hasil', 'proses', 'lalu', 'pesat',
            'menjadi', 'lebih', 'memiliki',  'satu', 'salah', 'benar', 'lama', 'manual',
            'baik', 'besar', 'tinggi', 'cepat', 'kuat', 'tepat', 'efektif', 'efisien','bidang' ,
            'mengatur', 'memastikan', 'menjamin', 'mengecek', 'mengevaluasi', 'manusia',
            'mengawasi', 'mengontrol', 'mengoptimalkan', 'mengumpulkan', 'mengolah', 'barang',
            'menyimpan', 'menampilkan', 'memberikan', 'menyiapkan', 'menyesuaikan', 'jenis',
            'mensinkronkan', 'menggabungkan', 'mengganti', 'menghapus', 'menambah', 'jalan',
            'satu', 'dua', 'tiga', 'utama', 'empat', 'lima', 'enam', 'tujuh', 'delapan', 'sembilan', 'sepuluh', 'puluh',
            
            # Kata Benda Umum Akademik
            'sistem', 'aplikasi', 'website', 'web', 'berbasis', 'program', 'fitur', 'perusahaan', 'organisasi',
            'metode', 'metodologi', 'data', 'informasi', 'teknologi', 'komputer', 'siswa', 'algoritma',
            'penelitian', 'skripsi', 'tugas', 'akhir', 'proposal', 'penulis', 'pengguna', 'yayasan',
            'latar', 'belakang', 'masalah', 'tujuan', 'manfaat', 'rumusan', 'batasan', 'kampus', 'bahasa',
            'universitas', 'kristen', 'duta', 'wacana', 'ukdw', 'yogyakarta', 'fakultas', 'prodi',
            'indonesia', 'tahun', 'waktu', 'jumlah', 'studi', 'kasus', 'dosen', 'mahasiswa'
        ]
        
        final_stopwords = list(set(stopwords_sastrawi + academic_stopwords))

        # 2. KAMUS NORMALISASI (Manual Mapping)
        # Mengubah kata kerja aktif/pasif menjadi Kata Benda Topik yang enak dibaca
        normalization_map = {
            # Citra & Pola
            "mengenali": "pengenalan",
            "dikenali": "pengenalan",
            "identifikasi": "identifikasi",
            "mengidentifikasi": "identifikasi",
            "klasifikasi": "klasifikasi",
            "mengklasifikasikan": "klasifikasi",
            "deteksi": "deteksi",
            "mendeteksi": "deteksi",
            "pendeteksian": "deteksi",
            
            # Jaringan & Keamanan
            "aman": "keamanan",
            "mengamankan": "keamanan",
            "pengamanan": "keamanan",
            "jaring": "jaringan",
            "terhubung": "koneksi",
            
            # Sistem Pakar / AI
            "diagnosa": "diagnosa",
            "mendiagnosa": "diagnosa",
            "prediksi": "prediksi",
            "memprediksi": "prediksi",
            "rekomendasi": "rekomendasi",
            "merekomendasikan": "rekomendasi",
            
            # Pembelajaran
            "belajar": "pembelajaran",
            "ajar": "pembelajaran",
            "edukasi": "pembelajaran",
            
            # Lainnya (Perbaikan kata aneh sisa stemming sebelumnya jika ada)
            "citra": "citra",
            "image": "citra",
            "mobile": "mobile",
            "android": "android"
        }

        cluster_names = {}
        
        for i in range(num_clusters):
            c_df = df_texts[df_texts['cluster_label'] == i]
            
            if len(c_df) > 1000: c_df = c_df.sample(1000, random_state=42)
            
            if c_df.empty:
                cluster_names[str(i)] = f"Topik {i}"
                continue
            
            # Ambil teks mentah
            raw_text = " ".join(c_df['text'].tolist())
            
            # Bersihkan
            clean_text = re.sub(r'[^a-zA-Z\s]', ' ', raw_text).lower()
            
            # 3. TERAPKAN NORMALISASI MANUAL
            words = clean_text.split()
            normalized_words = [normalization_map.get(w, w) for w in words]
            final_text = " ".join(normalized_words)

            try:
                # TF-IDF Tanpa Stemming Otomatis (Tapi sudah dinormalisasi manual)
                tfidf = TfidfVectorizer(
                    max_features=3, 
                    stop_words=final_stopwords,
                    ngram_range=(1, 2)
                )
                
                tfidf.fit_transform([final_text])
                feature_names = tfidf.get_feature_names_out()
                
                keys = [k.title() for k in feature_names]
                
                if not keys:
                    cluster_names[str(i)] = f"Topik {i}"
                else:
                    cluster_names[str(i)] = ", ".join(keys)
                    
            except Exception as e:
                # print(f"⚠️ Warning Cluster {i}: {e}")
                cluster_names[str(i)] = f"Topik {i}"
                
        # FINAL: Update Status
        INDEXING_STATUS["progress_percent"] = 100.0
        INDEXING_STATUS["message"] = "Index update complete. Hot reloading engine."
        # ----------------------------------------------------
        
        # F. MAPPING DOSEN
        print("   -> Mapping Dosen...")
        prop_cluster_map = df_texts.groupby('proposal_id')['cluster_label'].agg(
            lambda x: x.value_counts().index[0]
        ).reset_index()
        
        df_merged = pd.merge(prop_cluster_map, df_dosen, on='proposal_id')
        
        final_cluster_info = {}
        for i in range(num_clusters):
            g = df_merged[df_merged['cluster_label'] == i]
            
            # Hitung Top Dosen 1
            d1 = []
            if not g.empty and 'id_dosen_1' in g:
                vc1 = g['id_dosen_1'].value_counts()
                if not vc1.empty:
                    d1 = [int(x) for x in vc1.head(3).index.tolist()]
            
            # Hitung Top Dosen 2
            d2 = []
            if not g.empty and 'id_dosen_2' in g:
                vc2 = g['id_dosen_2'].value_counts()
                if not vc2.empty:
                    d2 = [int(x) for x in vc2.head(3).index.tolist()]
            
            final_cluster_info[str(i)] = {
                "name": cluster_names.get(str(i), ""),
                "dosen1": d1, 
                "dosen2": d2
            }
            
        with open(CLUSTER_INFO_PATH, 'w') as f: json.dump(final_cluster_info, f)
        
        # G. HOT RELOAD
        ENGINE["index"] = new_index
        ENGINE["meta"] = df_texts
        ENGINE["kmeans"] = kmeans
        ENGINE["cluster_info"] = final_cluster_info
        
        print("🚀 RE-INDEX SUKSES!")
        
    except Exception as e:
        # ----------------------------------------------------
        # ERROR: Set Status Error
        INDEXING_STATUS["is_running"] = False
        INDEXING_STATUS["message"] = f"Error: {str(e)[:100]}..."
        # ----------------------------------------------------
        import traceback
        traceback.print_exc()
        print(f"❌ ERROR: {e}")
        
    finally:
        # ----------------------------------------------------
        # END: Final cleanup
        INDEXING_STATUS["is_running"] = False
        INDEXING_STATUS["last_update_time"] = time.time()
        print("🚀 RE-INDEXING TASK FINISHED.")
        # ----------------------------------------------------

# ===================== 7. API ENDPOINTS =====================

app = FastAPI(title="ScripTI AI Engine")

@app.post("/similarity/check", response_model=SimilarityResponse)
def check_similarity(payload: ProposalPayload, top_k: int = 5):
    try:
        return process_similarity(payload, top_k)
    except Exception as e:
        print(f"ERROR: {e}")
        raise HTTPException(status_code=500, detail=str(e))
    
# Tambahkan method ini di bagian paling bawah file
@app.get("/system/indexing-status")
def get_indexing_status():
    """Endpoint yang di-query oleh Spring Boot/Frontend untuk mendapatkan progres."""
    global INDEXING_STATUS
    # Kembalikan status terkini
    return INDEXING_STATUS

@app.get("/system/clusters")
def get_all_clusters():
    """
    Mengembalikan seluruh data cluster (Nama Topik & ID Dosen)
    agar bisa ditampilkan di Admin Panel.
    """
    # Load dari memory ENGINE atau baca file JSON
    cluster_info = ENGINE.get("cluster_info", {})
    
    # Jika di memory kosong, coba baca file
    if not cluster_info:
        try:
            with open(CLUSTER_INFO_PATH, 'r') as f:
                cluster_info = json.load(f)
        except:
            return {"status": "error", "message": "Data cluster belum ada. Lakukan Re-index dulu."}
            
    return cluster_info
@app.post("/system/reindex")
def trigger_reindex(payload: List[ProposalRawData], background_tasks: BackgroundTasks):
    # Mode Asynchronous (Background)
    background_tasks.add_task(run_reindexing_task, payload)
    return {"status": "success", "message": "Re-indexing berjalan di latar belakang."}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)