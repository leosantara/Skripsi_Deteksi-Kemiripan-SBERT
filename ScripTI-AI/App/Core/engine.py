import faiss
import pandas as pd
import json
import pickle
from sentence_transformers import SentenceTransformer
from Config.config import PATHS

class AIEngine:
    def __init__(self):
        self.model = None
        self.index = None
        self.meta = None
        self.kmeans = None
        self.cluster_info = {}

    def load_resources(self):
        print("=== [CORE] LOADING AI RESOURCES... ===")
        
        # 1. Model
        try:
            self.model = SentenceTransformer(PATHS["MODEL_SBERT"])
            print("✅ Model Loaded.")
        except Exception as e:
            print(f"❌ Error Load Model: {e}")

        # 2. Index & Meta
        try:
            self.index = faiss.read_index(PATHS["INDEX"])
            self.meta = pd.read_pickle(PATHS["META"])
            print("✅ FAISS Index & Metadata Loaded.")
        except:
            print("⚠️ Index belum ada (Perlu Re-index).")

        # 3. Clustering
        try:
            with open(PATHS["CLUSTER_MODEL"], 'rb') as f:
                self.kmeans = pickle.load(f)
            with open(PATHS["CLUSTER_INFO"], 'r') as f:
                self.cluster_info = json.load(f)
            print("✅ Clustering Data Loaded.")
        except:
            print("⚠️ Cluster data belum ada.")

# Instance Global (Singleton)
ENGINE = AIEngine()