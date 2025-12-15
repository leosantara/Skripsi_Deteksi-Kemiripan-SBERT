import numpy as np
import faiss
import re
from Config.config import CONSTANTS
from Core.engine import ENGINE
from Services.preprocessing_service import PreprocessingService
from Services.dosen_recommendation_service import DosenRecommendationService
from DTO.request_dto import ProposalPayload
from DTO.response_dto import SimilarityResponse, SimilarityProposal, SimilarityMatch

class ProposalSimilarityService:
    
    @staticmethod
    def check_similarity(payload: ProposalPayload, top_k: int) -> SimilarityResponse:
        # 1. Cek Engine
        if ENGINE.index is None or ENGINE.meta.empty:
            return SimilarityResponse(queryProposalId=payload.proposal_id, modelName="Error-NoIndex", topK=0, results=[])

        # 2. Preprocessing
        sentences_data = PreprocessingService.preprocess_and_split_proposal(payload.dict())
        if not sentences_data:
            return SimilarityResponse(queryProposalId=payload.proposal_id, modelName="ScripTI-SBERT", topK=top_k, results=[])

        texts_q = [x['text'] for x in sentences_data]
        aspects_q = [x['aspect'] for x in sentences_data]
        num_q = len(texts_q)

        # 3. Encoding (SBERT)
        model = ENGINE.model
        # Auto prefix E5 logic
        if "e5" in str(model).lower():
             texts_input = ["query: " + t for t in texts_q]
        else:
             texts_input = texts_q
             
        emb_q = model.encode(texts_input, show_progress_bar=False)
        emb_q = np.array(emb_q).astype('float32')
        faiss.normalize_L2(emb_q)

        # 4. Search (FAISS)
        D, I = ENGINE.index.search(emb_q, k=50)

        # 5. Scoring Logic (Coverage)
        candidates_scores = {} # {pid: [scores]}
        
        for i in range(num_q):
            for rank, idx in enumerate(I[i]):
                if idx < 0: continue
                
                row = ENGINE.meta.iloc[idx]
                pid = int(row[CONSTANTS["PID_COL"]])
                if pid == payload.proposal_id: continue # Skip diri sendiri
                
                # Aspect Filter
                asp_db = row[CONSTANTS["ASPECT_COL"]]
                if not ProposalSimilarityService._aspects_compatible(aspects_q[i], asp_db): continue
                
                score = max(0.0, min(1.0, float(D[i][rank])))
                
                if pid not in candidates_scores:
                    candidates_scores[pid] = np.zeros(num_q)
                
                # Max Score per kalimat
                if score > candidates_scores[pid][i]:
                    candidates_scores[pid][i] = score

        # 6. Finalize Results
        final_results = []
        for pid, scores in candidates_scores.items():
            sim_count = np.sum(scores >= CONSTANTS["THRESHOLD_SIMILARITY"])
            coverage = float(sim_count) / float(num_q)
            
            if coverage <= 0.0: continue
            
            # Get Judul (Optimized)
            try:
                # Coba int dulu, kalau gagal string
                judul = ENGINE.meta[ENGINE.meta[CONSTANTS["PID_COL"]] == pid].iloc[0]['text']
            except:
                try:
                    judul = ENGINE.meta[ENGINE.meta[CONSTANTS["PID_COL"]] == str(pid)].iloc[0]['text']
                except:
                    judul = f"Proposal #{pid}"

            final_results.append(SimilarityProposal(
                proposalId=pid, score=coverage, judul=str(judul)[:200],
                totalSentences=num_q, similarCount=int(sim_count), ignoredCount=int(num_q - sim_count),
                matches=[]
            ))

        final_results.sort(key=lambda x: x.score, reverse=True)
        top_results = final_results[:top_k]

        # 7. Enrich Matches (Detailing)
        ProposalSimilarityService._enrich_matches(top_results, emb_q, texts_q, aspects_q)

        # 8. Recommendation
        recs = DosenRecommendationService.get_recommendations(emb_q)

        return SimilarityResponse(
            queryProposalId=payload.proposal_id,
            modelName="ScripTI-NusaBERT-Coverage",
            topK=top_k,
            results=top_results,
            dosenRecommendations=recs
        )

    @staticmethod
    def _aspects_compatible(a1, a2):
        if not a1 or not a2: return False
        def clean(s): return re.sub(r'[\s_]+', '', str(s).lower())
        a1, a2 = clean(a1), clean(a2)
        map_alias = {"rumusan": "rumusanmasalah", "rumusanmasalah": "rumusan"}
        if a1 == a2: return True
        if map_alias.get(a1) == a2: return True
        if map_alias.get(a2) == a1: return True
        return False

    @staticmethod
    def _enrich_matches(results, emb_q, texts_q, aspects_q):
        """
        Mengisi field 'matches' dengan detail kalimat yang mirip.
        Menggunakan re-encoding lokal agar akurat 100%.
        """
        model = ENGINE.model
        meta = ENGINE.meta
        num_q = len(texts_q)

        for res in results:
            pid = res.proposalId
            best_matches = []
            
            # Ambil data dari metadata
            rows_db = meta[meta[CONSTANTS["PID_COL"]] == str(pid)]
            if rows_db.empty:
                 rows_db = meta[meta[CONSTANTS["PID_COL"]] == pid]
            
            if rows_db.empty: continue
            
            texts_db = rows_db[CONSTANTS["SENT_COL"]].astype(str).tolist()
            aspects_db = rows_db[CONSTANTS["ASPECT_COL"]].astype(str).tolist()
            
            # Encode Ulang (Lokal)
            if "e5" in str(model).lower():
                texts_db_input = ["passage: " + t for t in texts_db]
            else:
                texts_db_input = texts_db
                
            emb_db_local = model.encode(texts_db_input, show_progress_bar=False)
            emb_db_local = np.array(emb_db_local).astype('float32')
            faiss.normalize_L2(emb_db_local)
            
            # Dot Product (Similarity Matrix)
            sim_matrix = np.dot(emb_q, emb_db_local.T)
            
            # Cari pasangan terbaik
            for i in range(num_q):
                best_idx = np.argmax(sim_matrix[i])
                best_score = float(sim_matrix[i][best_idx])
                
                if best_score < CONSTANTS["MIN_MATCH_FOR_UI"]: continue
                
                asp_db = aspects_db[best_idx]
                if not ProposalSimilarityService._aspects_compatible(aspects_q[i], asp_db): continue
                
                best_matches.append(SimilarityMatch(
                    aspectInput=aspects_q[i],
                    inputSentence=texts_q[i],
                    aspectDb=asp_db,
                    dbSentence=texts_db[best_idx],
                    similarity=best_score
                ))
            
            best_matches.sort(key=lambda x: x.similarity, reverse=True)
            res.matches = best_matches[:CONSTANTS["MAX_MATCHES_PER_PROPOSAL"]]