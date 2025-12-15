import os

BASE_DIR = r"D:\Media\Kuliah\Skripsi\ScripTI\Versi 4\Index_and_Clusters"

PATHS = {
    "INDEX": os.path.join(BASE_DIR, "proposal_vectors.index"),
    "META": os.path.join(BASE_DIR, "proposal_metadata.pkl"),
    "CLUSTER_MODEL": os.path.join(BASE_DIR, "topic_cluster_model.pkl"),
    "CLUSTER_MAP": os.path.join(BASE_DIR, "proposal_clusters.csv"),
    "CLUSTER_INFO": os.path.join(BASE_DIR, "cluster_info_complete.json"),
    "DATA_TUNING": os.path.join(BASE_DIR, "dataTuning_updated.csv"),
    "MODEL_SBERT": r"D:\Media\Kuliah\Skripsi\ScripTI\Versi 4\Model\Trained_SBERT\finetuned_all-nusabert-base_v1"
}

CONSTANTS = {
    "PID_COL": "proposal_id",
    "ASPECT_COL": "aspect",
    "SENT_COL": "text",
    "THRESHOLD_SIMILARITY": 0.50,
    "MIN_MATCH_FOR_UI": 0.40,
    "MAX_MATCHES_PER_PROPOSAL": 20,
    "NUM_CLUSTERS": 20
}