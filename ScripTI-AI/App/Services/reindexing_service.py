import os
import json
import pickle
import pandas as pd
import numpy as np
import faiss
import re
from sklearn.cluster import KMeans
from sklearn.feature_extraction.text import TfidfVectorizer
from Sastrawi.StopWordRemover.StopWordRemoverFactory import StopWordRemoverFactory

# Import Modul Lokal
from Core.engine import ENGINE
from Config.config import PATHS, CONSTANTS
from Services.preprocessing_service import PreprocessingService
from Services.progress_bar_service import ProgressBarService

class ReindexingService:
    
    @staticmethod
    def run_reindexing(raw_data):
        # 1. Reset Status
        ProgressBarService.update(0, "Starting preprocessing and data conversion...", True)
        
        try:
            # ================= A. PREPROCESSING =================
            text_rows = []
            dosen_rows = []
            
            total_raw = len(raw_data)
            for idx, p in enumerate(raw_data):
                # Update progress setiap 10% data biar gak spam log
                if idx % (max(1, total_raw // 10)) == 0:
                     ProgressBarService.update(int((idx/total_raw)*10), f"Preprocessing {idx}/{total_raw}...")
                     
                dosen_rows.append({
                    "proposal_id": str(p.proposal_id),
                    "id_dosen_1": p.id_dosen_1,
                    "id_dosen_2": p.id_dosen_2,
                    "judul": p.judul
                })
                
                payload_pre = {
                    "proposal_id": p.proposal_id, 
                    "judul": p.judul,
                    "latar_belakang": p.latar_belakang, 
                    "rumusan": p.rumusan, 
                    "tujuan": p.tujuan
                }
                
                # Panggil PreprocessingService
                sentences = PreprocessingService.preprocess_and_split_proposal(payload_pre)
                text_rows.extend(sentences)
                
            print(f"   -> Total Proposal: {total_raw} | Total Kalimat: {len(text_rows)}")
            
            df_texts = pd.DataFrame(text_rows)
            df_dosen = pd.DataFrame(dosen_rows)
            
            # Pastikan tipe data string
            df_texts['text'] = df_texts['text'].astype(str)
            df_texts['proposal_id'] = df_texts['proposal_id'].astype(str)
            
            # Backup CSV
            df_texts.to_csv(PATHS["DATA_TUNING"], index=False, encoding='utf-8-sig')
            
            # ================= B. ENCODING =================
            ProgressBarService.update(15, "Encoding vectors (SBERT inference)...", True)
            
            model = ENGINE.model
            sentences_list = df_texts['text'].tolist()
            
            if "e5" in str(model).lower():
                sentences_input = ["query: " + s for s in sentences_list]
            else:
                sentences_input = sentences_list
                
            embeddings = model.encode(sentences_input, batch_size=64, show_progress_bar=True, convert_to_tensor=False)
            embeddings = np.array(embeddings).astype('float32')
            faiss.normalize_L2(embeddings)
            
            # ================= C. INDEXING =================
            ProgressBarService.update(40, "Building FAISS index and saving metadata...", True)
            
            d = embeddings.shape[1]
            new_index = faiss.IndexFlatIP(d)
            new_index.add(embeddings)
            
            faiss.write_index(new_index, PATHS["INDEX"])
            df_texts.to_pickle(PATHS["META"])
            
            # ================= D. CLUSTERING =================
            ProgressBarService.update(60, "Clustering sentences...", True)
            
            num_clusters = CONSTANTS["NUM_CLUSTERS"]
            kmeans = KMeans(n_clusters=num_clusters, random_state=42, n_init=10)
            kmeans.fit(embeddings)
            
            with open(PATHS["CLUSTER_MODEL"], 'wb') as f:
                pickle.dump(kmeans, f)
                
            df_texts['cluster_label'] = kmeans.labels_
            
            # ================= E. TOPIC NAMING (SMART NORMALIZATION) =================
            ProgressBarService.update(75, "Generating topic names (Smart Normalization)...", True)
            
            # 1. Setup Stopwords
            factory = StopWordRemoverFactory()
            stopwords_sastrawi = factory.get_stop_words()
            
            academic_stopwords = [
                'banyak', 'perkembangan', 'mengalami', 'terjadi', 'sering', 'kerap', 
                'selama', 'masa', 'kini', 'saat', 'ini', 'itu', 'tersebut', 'berbagai',
                'merupakan', 'adalah', 'yaitu', 'yakni', 'antara', 'lain', 'sangat',
                'serta', 'ataupun', 'bagai', 'bagaimana', 'sebagai', 'banyak', 'sedikit',
                'berdasarkan', 'melalui', 'secara', 'suatu', 'sebuah', 'jadi', 'lintas', 
                'pula', 'pun', 'dapat', 'bisa', 'mampu', 'guna', 'agar', 'semakin', 'beberapa',
                
                'menggunakan', 'digunakan', 'dilakukan', 'melakukan', 'membuat', 'membangun', 'kehidupan',
                'merancang', 'mengimplementasikan', 'penerapan', 'implementasi', 'pembuatan', 'masyarakat',
                'pembangunan', 'perancangan', 'pengembangan', 'analisis', 'menganalisis', 'orang',
                'menentukan', 'menghitung', 'mencari', 'diterapkan', 'dibuat', 'dikembangkan', 'satunama',
                'membantu', 'mengetahui', 'meningkatkan', 'diharapkan', 'perlu', 
                'menyelesaikan', 'permasalahan', 'solusi', 'hasil', 'proses', 'lalu', 'pesat',
                'menjadi', 'lebih', 'memiliki',  'satu', 'salah', 'benar', 'lama', 'manual',
                'baik', 'besar', 'tinggi', 'cepat', 'kuat', 'tepat', 'efektif', 'efisien', 'bidang',
                'mengatur', 'memastikan', 'menjamin', 'mengecek', 'mengevaluasi', 'manusia',
                'mengawasi', 'mengontrol', 'mengoptimalkan', 'mengumpulkan', 'mengolah', 'barang',
                'menyimpan', 'menampilkan', 'memberikan', 'menyiapkan', 'menyesuaikan', 'jenis',
                'mensinkronkan', 'menggabungkan', 'mengganti', 'menghapus', 'menambah', 'jalan',
                'satu', 'dua', 'tiga', 'utama', 'empat', 'lima', 'enam', 'tujuh', 'delapan', 'sembilan', 'sepuluh', 'puluh',
                
                'sistem', 'aplikasi', 'website', 'web', 'berbasis', 'program', 'fitur', 'perusahaan', 'organisasi',
                'metode', 'metodologi', 'data', 'informasi', 'teknologi', 'komputer', 'siswa', 'algoritma',
                'penelitian', 'skripsi', 'tugas', 'akhir', 'proposal', 'penulis', 'pengguna', 'yayasan',
                'latar', 'belakang', 'masalah', 'tujuan', 'manfaat', 'rumusan', 'batasan', 'kampus',
                'universitas', 'kristen', 'duta', 'wacana', 'ukdw', 'yogyakarta', 'fakultas', 'prodi',
                'indonesia', 'tahun', 'waktu', 'jumlah', 'studi', 'kasus', 'dosen', 'mahasiswa'
            ]
            
            final_stopwords = list(set(stopwords_sastrawi + academic_stopwords))

            # 2. Normalization Map
            normalization_map = {
                "mengenali": "pengenalan", "dikenali": "pengenalan", "identifikasi": "identifikasi",
                "mengidentifikasi": "identifikasi", "klasifikasi": "klasifikasi", "mengklasifikasikan": "klasifikasi",
                "deteksi": "deteksi", "mendeteksi": "deteksi", "pendeteksian": "deteksi",
                "diagnosa": "diagnosa", "mendiagnosa": "diagnosa", "prediksi": "prediksi",
                "memprediksi": "prediksi", "rekomendasi": "rekomendasi", "merekomendasikan": "rekomendasi",
                "belajar": "pembelajaran", "ajar": "pembelajaran", "edukasi": "pembelajaran",
                "citra": "citra", "image": "citra", "mobile": "mobile", "android": "android",
                "aman": "keamanan", "mengamankan": "keamanan", "pengamanan": "keamanan",
                "jaring": "jaringan", "terhubung": "koneksi"
            }

            cluster_names = {}
            
            for i in range(num_clusters):
                c_df = df_texts[df_texts['cluster_label'] == i]
                if len(c_df) > 1000: c_df = c_df.sample(1000, random_state=42)
                
                if c_df.empty:
                    cluster_names[str(i)] = f"Topik {i}"
                    continue
                
                # Ambil teks & Bersihkan
                raw_text = " ".join(c_df['text'].tolist())
                clean_text = re.sub(r'[^a-zA-Z\s]', ' ', raw_text).lower()
                
                # Terapkan Normalisasi Manual
                words = clean_text.split()
                normalized_words = [normalization_map.get(w, w) for w in words]
                final_text = " ".join(normalized_words)
                
                try:
                    # TF-IDF (Tanpa Stemming, Tanpa Min_DF)
                    tfidf = TfidfVectorizer(
                        max_features=5,
                        stop_words=final_stopwords,
                        ngram_range=(1, 2) 
                    )
                    
                    tfidf.fit_transform([final_text])
                    feature_names = tfidf.get_feature_names_out()
                    
                    keys = [k.title() for k in feature_names if len(k) > 3]
                    
                    if not keys:
                        cluster_names[str(i)] = f"Topik {i}"
                    else:
                        # Ambil 3 kata teratas
                        cluster_names[str(i)] = ", ".join(keys[:3])
                        
                except Exception as e:
                    # print(f"⚠️ Cluster {i} Error: {e}")
                    cluster_names[str(i)] = f"Topik Kelompok {i}"

            # ================= F. MAPPING DOSEN =================
            ProgressBarService.update(90, "Mapping supervisors...", True)
            
            prop_cluster_map = df_texts.groupby('proposal_id')['cluster_label'].agg(
                lambda x: x.value_counts().index[0]
            ).reset_index()
            
            df_merged = pd.merge(prop_cluster_map, df_dosen, on='proposal_id')
            
            final_cluster_info = {}
            for i in range(num_clusters):
                g = df_merged[df_merged['cluster_label'] == i]
                
                d1 = []
                if not g.empty and 'id_dosen_1' in g:
                    vc1 = g['id_dosen_1'].value_counts()
                    if not vc1.empty: d1 = [int(x) for x in vc1.head(3).index.tolist()]
                
                d2 = []
                if not g.empty and 'id_dosen_2' in g:
                    vc2 = g['id_dosen_2'].value_counts()
                    if not vc2.empty: d2 = [int(x) for x in vc2.head(3).index.tolist()]
                
                final_cluster_info[str(i)] = {
                    "name": cluster_names.get(str(i), ""),
                    "dosen1": d1, 
                    "dosen2": d2
                }
                
            with open(PATHS["CLUSTER_INFO"], 'w') as f: 
                json.dump(final_cluster_info, f)
            
            # ================= G. HOT RELOAD =================
            ProgressBarService.update(100, "Hot reloading engine...", True)
            
            # Update variable global via Engine Class
            ENGINE.index = new_index
            ENGINE.meta = df_texts
            ENGINE.kmeans = kmeans
            ENGINE.cluster_info = final_cluster_info
            
            print("🚀 RE-INDEX SUKSES!")
            
        except Exception as e:
            import traceback
            traceback.print_exc()
            print(f"❌ ERROR: {e}")
            ProgressBarService.update(0, f"Error: {str(e)[:100]}...", False)
            
        finally:
            ProgressBarService.update(100, "Finished.", False)