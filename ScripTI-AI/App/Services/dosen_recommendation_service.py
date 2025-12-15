import numpy as np
from Core.engine import ENGINE
from DTO.response_dto import DosenRecommendation

class DosenRecommendationService:
    
    @staticmethod
    def get_recommendations(emb_q: np.ndarray) -> list[DosenRecommendation]:
        kmeans = ENGINE.kmeans
        cluster_info = ENGINE.cluster_info
        
        if not kmeans or not cluster_info:
            return []

        # Prediksi Cluster
        sentence_labels = kmeans.predict(emb_q)
        unique, counts = np.unique(sentence_labels, return_counts=True)
        total = len(sentence_labels)
        
        recs = []
        sorted_indices = np.argsort(-counts)
        
        # Ambil Top 2 Cluster
        for i in sorted_indices[:2]:
            cluster_id = int(unique[i])
            ratio = counts[i] / total
            
            if ratio < 0.15: continue
            
            info = cluster_info.get(str(cluster_id), {})
            topic_name = info.get("name", f"Topik {cluster_id}")
            
            recs.append(DosenRecommendation(
                clusterId=cluster_id,
                topicLabel=f"Topik: {topic_name} ({int(ratio*100)}%)",
                ratio=float(ratio),
                recommendedDosen1Ids=info.get("dosen1", []),
                recommendedDosen2Ids=info.get("dosen2", [])
            ))
            
        return recs