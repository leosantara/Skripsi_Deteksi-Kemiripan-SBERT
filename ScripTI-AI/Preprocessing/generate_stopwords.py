import pandas as pd
from collections import Counter
import re
from Sastrawi.StopWordRemover.StopWordRemoverFactory import StopWordRemoverFactory

# 1. KONFIGURASI
INPUT_CSV = "./dataTuning.csv"  # File sumber data kalimat
TOP_N_STOPWORDS = 100         # Berapa banyak kata teratas yang mau dianggap stopwords?

# 2. LOAD DATA
print("Membaca dataTuning.csv...")
try:
    df = pd.read_csv(INPUT_CSV, encoding='utf-8-sig')
except:
    df = pd.read_csv(INPUT_CSV) # Fallback kalau error encoding

# Pastikan kolom text string
texts = df['text'].astype(str).tolist()

# 3. PREPROCESSING SEDERHANA
print("Menghitung frekuensi kata...")
stop_factory = StopWordRemoverFactory()
default_stopwords = set(stop_factory.get_stop_words()) # Ambil stopword bawaan Sastrawi

all_words = []

for text in texts:
    # Bersihkan tanda baca dan lowercase
    clean_text = re.sub(r'[^\w\s]', '', text.lower())
    words = clean_text.split()
    
    for w in words:
        # Hanya ambil kata yang BUKAN angka dan BUKAN stopword bawaan
        if w not in default_stopwords and not w.isdigit() and len(w) > 2:
            all_words.append(w)

# 4. HITUNG FREKUENSI
counter = Counter(all_words)
most_common = counter.most_common(TOP_N_STOPWORDS)

print("\n--- KATA PALING SERING MUNCUL (CANDIDATE STOPWORDS) ---")
custom_stopwords = []
for word, count in most_common:
    print(f"{word}: {count}")
    custom_stopwords.append(word)

print("\n" + "="*50)
print("COPY LIST INI KE similarity_service.py")
print("="*50)
print(f"academic_stopwords = {custom_stopwords}")