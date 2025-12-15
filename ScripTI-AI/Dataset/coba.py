import google.generativeai as genai
import json
import typing_extensions as typing

# =================KONFIGURASI=================
# Masukkan API KEY Gemini kamu di bawah ini
API_KEY = "AIzaSyBTpIRX_QHvCPgb6pQQOOvns10JYR-Av-0"

# Nama movie yang ingin dicari kemiripannya
namaMovie = "Spiderman"
# =============================================

# Kita definisikan bentuk data (Schema) yang kita mau
# Ini bertindak sebagai "cetakan" agar AI tidak melenceng
class MovieInfo(typing.TypedDict):
    judul_film: str
    imdb_rating: float
    image_url: str

def cari_film_serupa(movie_title):
    # Konfigurasi API Key
    try:
        genai.configure(api_key=API_KEY)
    except Exception as e:
        print(f"Error konfigurasi API Key: {e}")
        return

    # Menggunakan model Gemini
    # PERUBAHAN PENTING: Kita tambahkan 'response_schema'
    # Ini memaksa output menjadi List dari MovieInfo
    model = genai.GenerativeModel(
        'gemini-2.5-flash',
        generation_config={
            "response_mime_type": "application/json",
            "response_schema": list[MovieInfo] 
        }
    )

    # Prompt bisa lebih sederhana karena struktur sudah diatur di config
    prompt = f"""
    Bertindaklah sebagai ahli film.
    Carikan 5 film yang sangat mirip atau relevan dengan film '{movie_title}'.
    Untuk image_url, berikan link poster film yang valid (jika ada).
    """

    print(f"Sedang mencari film yang mirip dengan '{movie_title}' ke Gemini...")
    
    try:
        # Menembak ke API
        response = model.generate_content(prompt)
        
        # Mengambil text hasil response
        json_result = response.text
        
        # Parsing string JSON ke object Python
        data_film = json.loads(json_result)
        
        return data_film

    except Exception as e:
        # Jika error parsing JSON (sangat jarang terjadi dengan schema), tampilkan raw text
        return {"error": f"Gagal mengambil data: {str(e)}"}

if __name__ == "__main__":
    if API_KEY == "GANTI_DENGAN_API_KEY_KAMU_DISINI":
        print("PERINGATAN: Harap masukkan API KEY kamu di dalam file script baris ke-7.")
    else:
        # Jalankan fungsi
        hasil = cari_film_serupa(namaMovie)
        
        # Print hasil
        print("\n=== HASIL JSON DARI GEMINI (SUDAH DIPERBAIKI) ===")
        print(json.dumps(hasil, indent=4))