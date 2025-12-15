import re

class KalimatBreakService:
    
    @staticmethod
    def breakKalimat(daftarParagraf):
        # ======================= UTIL: Masking & Unmasking URL =======================
        # Kita anggap data paragraf aslinya aman (tidak terpecah), jadi regex URL yang "normal" cukup.
        _URL_PATTERN = re.compile(
            r'(?i)\b(?:https?://|www\.)[^\s<>()]+'  # http(s)://... atau www....
        )

        # ====== PRE-HEAL ringan untuk domain terpisah (ClickTale. Com, dll.) ======
        def pre_heal_domains(text: str) -> str:
            if not text:
                return text
            # “example. com” / “ClickTale. Com” → “example.com” / “ClickTale.Com”
            TLD = r'(?:com|net|org|edu|gov|mil|id|co\.id|ac\.id|go\.id|sch\.id|my\.id|io|ai)'
            text = re.sub(rf'\b([A-Za-z0-9-]{{2,}})\.\s*({TLD})\b', r'\1.\2', text, flags=re.I)
            # “www . example . com” → “www.example.com” (jaga-jaga)
            text = re.sub(r'(?i)\bwww\s*\.\s*', 'www.', text)
            text = re.sub(r'(?i)\b([A-Za-z0-9-]{2,})\s*\.\s*', r'\1.', text)  # hanya saat terlihat rantai domain
            return text


        def mask_links(text: str, daftarLink: list) -> str:
            """
            Deteksi URL, simpan ke daftarLink, lalu ganti di teks menjadi token $daftarLink{idx}.
            Trailing punctuation seperti '.', ',', ')', ';', ':' akan dipertahankan di luar token.
            """
            if not text:
                return text

            def _repl(m: re.Match) -> str:
                raw = m.group(0)
                # Pisahkan trailing punctuations yang tak termasuk URL
                trail = ''
                while raw and raw[-1] in '.,;:)]':
                    trail = raw[-1] + trail
                    raw = raw[:-1]
                idx = len(daftarLink)
                daftarLink.append(raw)
                return f"$daftarLink{idx}{trail}"

            return _URL_PATTERN.sub(_repl, text)

        def unmask_links(text: str, daftarLink: list) -> str:
            """Kembalikan token $daftarLink{idx} menjadi URL aslinya."""
            if not text:
                return text
            return re.sub(
                r'\$daftarLink(\d+)',
                lambda m: daftarLink[int(m.group(1))] if int(m.group(1)) < len(daftarLink) else m.group(0),
                text
            )

        # ======================= Normalisasi whitespace ringan =======================
        def normalize_ws(s: str) -> str:
            if not s:
                return s
            ELLIPS = "<ELLIPS_TOKEN>"
            s = s.replace("...", "…").replace("…", ELLIPS)

            # kompres pengulangan tanda baca umum
            s = re.sub(r'(?<=\d)\.\s+(?=\d)', '.', s)
            s = re.sub(r'\.(?:\s*\.)+', '.', s)
            s = re.sub(r'([!?])\1+', r'\1', s)
            s = re.sub(r'([,;:])\s*\1+', r'\1', s)

            # whitespace
            s = s.replace("\u00A0", " ")
            s = re.sub(r"[ \t\r\f\v]+", " ", s)

            # spasi sebelum penutup
            s = re.sub(r"\s+([,.;?!)\]\}\"'])", r"\1", s)
            # spasi setelah tanda baca umum (kecuali diikuti penutup/petik)
            s = re.sub(r"(?<!\d)([,;?!\.])(?!\d|[)\]]|['\"]) (?=\S)", r"\1 ", s)

            # titik ribuan/desimal yang sering kepisah
            s = re.sub(r"(?<=\d)\s*\.\s*(?=\d{3}\b)", ".", s)
            s = re.sub(r"(?<=\d)\s*\.\s*(?=\d)", ".", s)

            s = re.sub(r"\s{2,}", " ", s)
            s = s.replace(ELLIPS, "…")
            return s.strip()

        # ===== Fallback clean_html =====
        def clean_html(text, keep_linebreaks=True):
            if text is None:
                return ""
            t = re.sub(r"(?is)<br\s*/?>", "\n", text) if keep_linebreaks else text
            t = re.sub(r"(?is)<[^>]+>", " ", t)
            return re.sub(r"\s{2,}", " ", t).strip()

        # -------------------- Proteksi titik yang bukan akhir kalimat -----------------
        PROTECTED  = "<DOT>"
        PCOLON     = "<COLON>"
        _END_PRIORITY = {"?": 3, "!": 2, ".": 1, "…": 1}

        def protect_dynamic(text: str) -> str:
            s = text

            # Gelar medis Sp.X
            s = re.sub(r'\bSp\s*\.\s*([A-Z]{1,3})\b', lambda m: 'Sp' + PROTECTED + m.group(1), s)

            # Gelar di depan nama (dr., drs., ir.)
            s = re.sub(r'\b(dr|drs|ir)\s*\.\s*(?=[A-Z])', lambda m: m.group(1) + PROTECTED + ' ', s, flags=re.I)

            # enumerasi "1." / "a."
            s = re.sub(r"(?:(?<=^)|(?<=[\s\"'(\[\{<:;?!\.]))(\d{1,3})\.(?=\s*\S)", r"\1" + PROTECTED, s)
            s = re.sub(r"(?:(?<=^)|(?<=[\s\"'(\[\{<:;?!\.]))([A-Za-z])\.(?=\s*\S)", r"\1" + PROTECTED, s)
            # juga varian ")"
            s = re.sub(r"(?:(?<=^)|(?<=[\s\"'(\[\{<:;?!\.]))(\d{1,3})\)(?=\s*\S)", r"\1)", s)
            s = re.sub(r"(?:(?<=^)|(?<=[\s\"'(\[\{<:;?!\.]))([A-Za-z])\)(?=\s*\S)", r"\1)", s)

            # ellipsis → lindungi titiknya
            s = re.sub(r"\.\.\.", lambda m: m.group(0).replace(".", PROTECTED), s)

            # Prof. dr.
            s = re.sub(r"\bProf\.\s*dr\.", lambda m: m.group(0).replace(".", PROTECTED), s)

            # Multi-dot abbr (S.Kom., Ph.D., d.s.b.)
            s = re.sub(r"\b(?:[A-Za-z]{1,4}\.){2,}(?=[\s,;:)]|$)", lambda m: m.group(0).replace(".", PROTECTED), s)

            # whitelist lowercase abbr
            LOWER_ABBR_EXT = ("dll", "dsb", "dst", "dkk", "al", "ibid", "op", "cit", "vs", "hlm", "hal")
            s = re.sub(rf"\b(?:{'|'.join(LOWER_ABBR_EXT)})\.(?=\s)", lambda m: m.group(0).replace(".", PROTECTED), s, flags=re.I)

            # TitleCase short abbr (No., Jl., Prof., St.)
            s = re.sub(r"\b([A-Z][a-z]{0,3})\.(?=\s*[a-z0-9(])", r"\1" + PROTECTED, s)
            s = re.sub(r"\b([A-Z][a-z]{0,3})\.(?=\s*[A-Z][a-z])", r"\1" + PROTECTED, s)
            s = re.sub(r'\b(St)\.(?=\s)', r'\1' + PROTECTED, s)

            # inisial berurutan (R. A.)
            s = re.sub(r"\b([A-Z])\.(?=\s*[A-Z])", r"\1" + PROTECTED, s)

            # Dotted ALLCAP suffix: Node.JS, Socket.IO
            s = re.sub(r"(?<=\b[A-Za-z])\.(?=[A-Z]{2,5}\b)", PROTECTED, s)

            # Kode regulasi/nomor: KA.401 dst.
            s = re.sub(r'\b([A-Z]{1,4})\.(\d+)\b', lambda m: m.group(0).replace('.', PROTECTED), s)

            # rantai kode bertitik (AHU-0016081.AH.01.01)
            s = re.sub(r"\b([A-Z0-9-]+(?:\.[A-Z0-9-]+){1,})\b",
                    lambda m: m.group(1).replace(".", PROTECTED), s)

            # desimal 1.23
            s = re.sub(r"(?<=\d)\.(?=\d)", PROTECTED, s)

            # titik sebelum tahun di dalam kurung (…., 2014)
            s = re.sub(r"\(([^)]*)\.(?=\s*\d{4}\))", lambda m: m.group(0).replace(".", PROTECTED), s)

            # pola (*.JPG)
            s = re.sub(r"\(\*\.[A-Za-z0-9]{1,5}\)", lambda m: m.group(0).replace(".", PROTECTED), s)

            # 'et al.' dan 'dkk.'
            s = re.sub(r'\bet\.\s*al\.', lambda m: m.group(0).replace('.', PROTECTED), s, flags=re.I)
            s = re.sub(r'\bdkk\.', lambda m: m.group(0).replace('.', PROTECTED), s, flags=re.I)

            # Titik antara kapital dan digit (KA.401)
            s = re.sub(r'(?<=\b[A-Z])\.(?=\d)', PROTECTED, s)

            # *** Tambahan kecil untuk "24 / 7" → "24/7" ***
            s = re.sub(r'(\d+)\s*/\s*(\d+)', r'\1/\2', s)

            # 1) Lindungi titik DI DALAM kurung pada pola "(Nama, 2016)." tapi biarkan titik sesudah kurung
            s = re.sub(
                r'\(([^()]*?,\s*\d{4}[a-z]?[^()]*)\)\.',
                lambda m: '(' + m.group(1).replace('.', PROTECTED) + ').',
                s
            )

            # Derajat/gelar populer
            s = re.sub(r'\bPh\s*\.\s*D\b', lambda m: m.group(0).replace('.', PROTECTED), s)
            s = re.sub(r'\bM\s*\.\s*[Ss]c\b\.?', lambda m: m.group(0).replace('.', PROTECTED), s)
            s = re.sub(r'\bS\s*\.\s*Kom\b\.?', lambda m: m.group(0).replace('.', PROTECTED), s)
            s = re.sub(r'\bD\s*\.\s*I\s*\.\s*C\b\.?', lambda m: m.group(0).replace('.', PROTECTED), s)

            # Ir.Nizam → Ir.<DOT> Nizam
            s = re.sub(r'\b(Ir)\s*\.(?=[A-Z])', r'\1' + PROTECTED + ' ', s)

            # 2) Varian titik setelah kurung di akhir string
            s = re.sub(
                r'\(([^()]*?,\s*\d{4}[a-z]?[^()]*)\)\.\s*$',
                lambda m: '(' + m.group(1).replace('.', PROTECTED) + ').',
                s
            )

            # (*.EXT) varian ber-spasi
            s = re.sub(r"\(\s*\*\s*\.\s*[A-Za-z0-9]{1,5}\s*\)", lambda m: m.group(0).replace(".", PROTECTED), s)

            # Nama.Inisial → lindungi
            s = re.sub(r'\b([A-Z][a-z]+)\.(?=[A-Z]\b)', lambda m: m.group(1) + PROTECTED, s)

            # KAPITAL.DIGIT varian spasi
            s = re.sub(r'\b([A-Z]{1,4})\s*\.\s*(\d+)\b', lambda m: m.group(1) + PROTECTED + m.group(2), s)

            # RANTAI KODE DENGAN SPASI
            s = re.sub(
                r'\b([A-Z]{1,5})\s*\.\s*(\d+)/([A-Z]{1,5})\s*\.\s*(\d+)/([A-Z]{2,6})/(\d{4})\b',
                lambda m: (m.group(1) + PROTECTED + m.group(2) + '/' +
                        m.group(3) + PROTECTED + m.group(4) + '/' +
                        m.group(5) + '/' + m.group(6)),
                s
            )

            return s

        def unprotect(text: str) -> str:
            return text.replace(PROTECTED, ".").replace(PCOLON, ":")

        # ====== Anti-glue post-pass (tanpa logic URL) ======
        def anti_glue_overmerged(seg: str):
            if not seg or seg.rstrip().endswith(":"):
                return [seg]

            t = seg.strip()
            if t.count('(') != t.count(')'):
                return [seg]
            if re.search(r'\.\s+\((?=[^)]*\d{4})', t):
                return [seg]
            if re.match(r'^\(\s*[^()]*\d{4}[^()]*\)\.?$', t):
                return [seg]

            prot = protect_dynamic(t)
            # FORCE_END setelah "). " bila diikuti kata sitasi umum
            prot = re.sub(r'\)\.\s+(?=(?:diakses|diunduh|accessed|retrieved)\b)', '). <FORCE_END> ', prot, flags=re.I)
            split_re = re.compile(r'(?:(?<=[\.!?…])|\<FORCE_END\>)\s+(?=(?:[A-Z0-9""])|(?!\s)\()')

            parts = split_re.split(prot)
            parts = [unprotect(p).strip() for p in parts if p and p.strip()]
            parts = [p.replace('<FORCE_END>', '').strip() for p in parts if p and p.strip()]

            if len(parts) >= 2 and any(" " in p for p in parts):
                return parts
            return [seg]

        # -------------------- Split per kalimat (robust, tanpa URL-rules) --------------------
        def _choose_ender(cluster: str) -> str:
            best = "."
            best_score = 0
            for ch in cluster:
                score = _END_PRIORITY.get(ch, 0)
                if score > best_score:
                    best, best_score = ch, score
            return best

        def preclean_minimal(s: str) -> str:
            if not s:
                return s
            ELLIPS = "<ELLIPS_TOKEN>"
            s = s.replace("...", "…").replace("…", ELLIPS)
            s = re.sub(r'(?<=\d)\.\s+(?=\d)', '.', s)
            s = re.sub(r'\.(?:\s*\.)+', '.', s)
            s = re.sub(r'([!?])\1+', r'\1', s)
            s = re.sub(r'([,;:])\s*\1+', r'\1', s)
            # buang penutup kurung yatim
            s = re.sub(r'([\.!?])\s*[\)\]\}]+(\s+)(?=[A-Z(\"\'"])', r'\1\2', s)
            s = re.sub(r'([\.!?])\s*[\)\]\}]+\s*$', r'\1', s)
            s = s.replace(ELLIPS, "…")
            return s

        def split_sentences(text: str):
            cleaned = clean_html(text, keep_linebreaks=True)
            cleaned = preclean_minimal(cleaned)
            s = protect_dynamic(cleaned).strip()

            END   = r"[\.?!;:…]"
            CLOSE = r"[\"')\]\}]"
            OPEN  = r"[\"'(\[\{<]"
            BUL   = r"[-–—•·*]+"
            sep = re.compile(rf"({END}+)(?:{CLOSE}+)?(?:\s*{BUL})?(?=(?:\s+|$|{OPEN}|[A-Z0-9]|{BUL}))")

            # pola sitasi
            citation_paren_re = re.compile(r"""^\(\s*[^()]*\d{4}[a-z]?[^()]*\)\.?$""", re.I | re.VERBOSE)
            citation_author_year_re = re.compile(r"""^[A-Z][A-Za-z .'\-]+?,\s*(?:[A-Z]\.\s*)+
                                                    (?:,(?:\s*&|\s*and|\s*)[A-Z][A-Za-z .'\-]+?,\s*(?:[A-Z]\.\s*)+)*
                                                    (?:,\s*et\s+al\.)?\s*\(\d{4}[a-z]?\)\.?$""",
                                                re.I | re.VERBOSE)
            surname_only = re.compile(r'^[A-Z][A-Za-z.\- ]+\.\s*$', re.I)
            surname_with_year_paren = re.compile(r"""^[A-Z][A-Za-z .'\-]+?\.\s*\(\d{4}[a-z]?[^)]*\)\.?$""",
                                                re.I | re.VERBOSE)

            # split awal
            parts, last = [], 0
            for m in sep.finditer(s):
                seg = s[last:m.end()].strip()
                if seg:
                    parts.append(seg)
                last = m.end()
            tail = s[last:].strip()
            if tail:
                parts.append(tail)

            # tidy + unprotect
            parts = [re.sub(rf"^\s*{BUL}\s*", "", p) for p in parts]
            parts = [re.sub(r'^[\'\"]+', "", p) for p in parts]
            parts = [re.sub(r'[\'\"]+$', "", p) for p in parts]
            parts = [unprotect(p).strip() for p in parts if p.strip()]

            # GLUE: token enumerasi yang yatim
            glued, i = [], 0
            enum_token_re = re.compile(r'^[\s"\'(\[]([0-9]{1,3}|[A-Za-z])\.\s*$')
            while i < len(parts):
                cur = parts[i]
                if enum_token_re.match(cur) and i + 1 < len(parts):
                    token = enum_token_re.match(cur).group(1) + "."
                    glued.append(f"{token} {parts[i+1].lstrip()}")
                    i += 2
                else:
                    glued.append(cur)
                    i += 1

            # GLUE: segmen berakhir ":" → satukan hanya dgn segmen berikutnya
            merged, buf = [], ""
            for seg in glued:
                if buf:
                    merged.append((buf + " " + seg).strip()); buf = ""
                elif seg.rstrip().endswith(":"):
                    buf = seg
                else:
                    merged.append(seg)
            if buf:
                merged.append(buf.strip())
            final = merged

            # GLUE: sitasi multi‐segmen sampai ')' muncul
            combined, buf = [], None
            for seg in final:
                t = seg.strip()
                if buf is not None:
                    buf = f"{buf} {t}"
                    if re.search(r'\)\.?$', t):
                        combined.append(buf.strip()); buf = None
                    continue
                if re.match(r'^\(', t) and not re.search(r'\)\.?$', t):
                    buf = t
                elif re.search(r'\(\s*$', t) or re.search(r'\bhlm\.\s*$', t, re.I):
                    buf = t
                else:
                    combined.append(seg)
            if buf:
                combined.append(buf.strip())
            final = combined

            # GLUE: "Ocepek." + "(2013: 1-5)."
            glued_name_year = []
            for seg in final:
                if glued_name_year and surname_only.match(glued_name_year[-1]) and citation_paren_re.match(seg.strip()):
                    glued_name_year[-1] = f"{glued_name_year[-1].rstrip()} {seg.strip()}"
                else:
                    glued_name_year.append(seg)
            final = glued_name_year

            # GLUE: sitasi utuh tempel ke sebelumnya
            glued_citations = []
            for seg in final:
                _t = seg.strip()
                if glued_citations and (
                    citation_paren_re.match(_t) or
                    citation_author_year_re.match(_t) or
                    surname_with_year_paren.match(_t)
                ):
                    glued_citations[-1] = f"{glued_citations[-1].rstrip()} {seg}".strip()
                else:
                    glued_citations.append(seg)
            final = glued_citations

            # orphan close seg
            orphan_close_seg = re.compile(r'^[\)\]\}]+\.?$')
            final2 = []
            for seg in final:
                if final2 and orphan_close_seg.match(seg):
                    continue
                final2.append(seg)
            final = final2

            # "15)." tempel ke sebelumnya
            glued4 = []
            for seg in final:
                if glued4 and re.match(r"^\d+\)\.?$", seg):
                    glued4[-1] = glued4[-1].rstrip() + " " + seg
                else:
                    glued4.append(seg)
            final = glued4

            # angka desimal/enumerasi yatim "0." "9)." "57)."
            orphan_num_dot = re.compile(r'^\d+[\.\)]\.?\s*$')
            glued_num = []
            for seg in final:
                t = seg.strip()
                if glued_num and orphan_num_dot.match(t):
                    prev = glued_num[-1].rstrip()
                    if not prev or prev[-1] not in '.!?':
                        glued_num[-1] = (prev + " " + t).strip()
                    else:
                        glued_num.append(seg)
                else:
                    glued_num.append(seg)
            final = glued_num

            # POST-PASS: pecah segmen yang masih memuat >1 kalimat
            post = []
            for seg in final:
                post.extend(anti_glue_overmerged(seg))
            final = [normalize_ws(x) for x in post]
            return final

        # ============================= Pipeline utama ================================
        # daftarParagraf seperti:
        # daftarParagraf = [
        #   [idProposal, "Judul", "Isi"],
        #   [idProposal, "LatarBelakang", "Isi"],
        #   [idProposal, "Rumusan", "Isi"],
        #   [idProposal, "Tujuan", "Isi"],
        #   ...
        # ]

        daftarLink = []  # bank URL global
        daftarKalimatProposal = []

        def _tag_lower(t: str) -> str:
            t = (t or "").strip()
            mapping = {
                "Judul": "judul",
                "LatarBelakang": "latar_belakang",
                "Rumusan": "rumusan",
                "Tujuan": "tujuan"
            }
            return mapping.get(t, t.lower())

        for pid, tag, isi in daftarParagraf:
            tag_norm = _tag_lower(tag)

            if tag_norm == "judul":
                # Judul langsung 1 baris (tetap mask/unmask untuk konsistensi)
                healed = pre_heal_domains(isi or "")
                masked = mask_links(healed, daftarLink)
                kal = normalize_ws(masked)
                kal = unmask_links(kal, daftarLink)
                daftarKalimatProposal.append([pid, kal, "judul"])
                continue

            # Untuk paragraf lain: mask → split → unmask per kalimat
            healed = pre_heal_domains(isi or "")
            masked = mask_links(healed, daftarLink)
            for kal in split_sentences(masked):
                kal = normalize_ws(kal)
                kal = unmask_links(kal, daftarLink)
                if kal:
                    daftarKalimatProposal.append([pid, kal, tag_norm])

        # ============ POST-FLATTEN: pecah lagi item over-merged =============
        daftarKalimatProposal_fix = []
        for pid, kal, tag in daftarKalimatProposal:
            parts = anti_glue_overmerged(kal)
            if len(parts) > 1 and any(" " in p for p in parts):
                for p in parts:
                    p = normalize_ws(p)
                    if p:
                        daftarKalimatProposal_fix.append([pid, p, tag])
            else:
                daftarKalimatProposal_fix.append([pid, normalize_ws(kal), tag])
        daftarKalimatProposal = daftarKalimatProposal_fix

        # (Opsional) Recovery kata nempel ekstrem (bukan URL terkait)
        def _recover_overmerged_words(t: str) -> str:
            if re.search(r'[A-Za-z]{30,}', t):
                t = re.sub(r'(?<=[a-z])(?=[A-Z])', ' ', t)
                t = re.sub(r'([a-z]{5,})([a-z]{5,})', r'\1 \2', t)
            return t

        # =================== POST-PASS LINTAS ITEM: re-glue cerdas ===================
        def cross_glue_items(items):
            """
            Lintas-item (pid+tag sama) penggabungan:
            - URL-only & (URL) & angka+URL → tempel ke sebelumnya.
            - Ekor sitasi umum ( … (2016). ), sumber angka (24/7 … (2010).), paren tanpa tahun → tempel ke sebelumnya.
            - “Risti?” + “(2014, …) mengatakan …” (segmen berikut diawali kurung+year lalu lanjut teks) → tempel.
            - Brand. + Com/Net/Org/Id … → jadikan Brand.Com/Net/… (tanpa spasi nyasar).
            - Frasa judul pendek (≤3 kata) / typo ':.', ';' → tempel ke berikutnya.
            - Tail “diakses/accessed/retrieved …” → tempel ke sebelumnya.
            - Singkatan badan/instansi & alamat: UD., CV., PT., TBK., RS., PD., MT., KH. → zip kiri/kanan.
            - Node. JS / Socket. IO → tempel.
            - Rangkaian setelah ':' (daftar/pertanyaan beruntun) → gulung.
            """
            if not items:
                return items

            # --- Pola umum ---
            url_only        = re.compile(r'^\s*\(?\s*(?:https?://|www\.)\S+\s*\)?\.?\s*$', re.I)
            num_then_url    = re.compile(r'^\s*\d+\s+(?:https?://|www\.)\S+\s*$', re.I)
            paren_url_only  = re.compile(r'^\s*\(\s*(?:https?://|www\.)\S+\s*\)\.?\s*$', re.I)

            # sitasi klasik "(... 2016 ...)." satu baris penuh
            tail_citation_paren = re.compile(r'^\s*\([^()]*\d{4}[a-z]?\s*[^()]*\)\.?\s*$', re.I)
            # "Author ... (2016)." (awalan huruf)
            tail_author_year    = re.compile(r'^[A-Z][^()]{0,200}\(\s*\d{4}[a-z]?\s*\)\.?\s*$', re.I)
            # *** BARU: "24/7 Wall St. (2010)." (awalan angka/karakter non-huruf) ***
            tail_source_year_num= re.compile(r'^[0-9][^()]{0,200}\(\s*\d{4}[a-z]?\s*\)\.?\s*$', re.I)
            # *** BARU: paren TANPA tahun (anggap sitasi) ***
            tail_paren_no_year  = re.compile(r'^\s*\([^()]{1,120}\)\.?\s*$', re.I)
            # *** BARU: baris MULAI dengan (tahun …) LALU lanjut teks (untuk kasus "Risti? (2014, hlm. 4) mengatakan …") ***
            starts_paren_year_then_text = re.compile(r'^\s*\(\s*\d{4}[^\)]*\)\s+\S', re.I)

            # prev akhiri "et al." / "dkk."
            prev_etal_end = re.compile(r'(?:et\s+al|dkk)\.\s*$', re.I)

            # singkatan korporat/alamat
            corp_abbr_only   = re.compile(r'^\s*(?:UD|CV|PT|TBK|RS|PD|MT|KH)\.\s*$', re.I)
            ends_with_corp   = re.compile(r'(?:UD|CV|PT|TBK|RS|PD|MT|KH)\.\s*$', re.I)
            short_name_line  = re.compile(r'^[A-Z][\w&.\'\- ]{1,40}\.\s*$', re.U)
            starts_with_name = re.compile(r'^\s*[\'"“”‘’(]*[A-Z][A-Za-z0-9&.\'\-]*(?:\s+[A-Z][A-Za-z0-9&.\'\-]*)*', re.U)

            # domain/TLD
            tld = r'(?:com|net|org|io|ai|id|co\.id|ac\.id|go\.id|sch\.id|my\.id)'
            domainish_end    = re.compile(rf'\b[A-Za-z0-9-]{{2,}}\.{tld}\.\s*$', re.I)
            tld_only_start   = re.compile(rf'^{tld}\b', re.I)
            # *** BARU: prev "Brand." + next "Com/Net/..." ***
            prev_word_dot    = re.compile(r'\b([A-Za-z][A-Za-z0-9-]{1,})\.\s*$')

            # daftar/pertanyaan pendek
            short_q = re.compile(r'^(?:per\s+\w+|\w.{0,80}\?)$', re.I)

            # (IPK)., (CPU), (UX).
            lone_paren_abbr  = re.compile(r'^\s*\([A-Za-z]{2,8}\)\.?\s*$')

            # Node. JS / Socket. IO
            word_dot_prev    = re.compile(r'.*\b[A-Za-z]{2,}\.\s*$')
            next_allcaps     = re.compile(r'^[A-Z]{1,4}\b')

            # *** BARU: tail "diakses/accessed/retrieved" ***
            tail_access_note = re.compile(r'\b(diakses|accessed|retrieved)\b', re.I)

            def _unbalanced_paren_or_quote(s: str) -> bool:
                s = s.strip()
                if s.count('(') != s.count(')'):
                    return True
                for left, right in [("'", "'"), ('"', '"'), ('“','”'), ('‘','’')]:
                    if s.count(left) != s.count(right):
                        return True
                return False

            def _is_very_short_opening(s: str) -> bool:
                w = s.strip().split()
                return len(w) <= 3 and s.endswith('.')

            # *** BARU: frasa pendek capitalized tanpa tanda akhir (≤3 kata) → kandidat tempel ***
            def _is_short_titleish(s: str) -> bool:
                s = s.strip()
                if re.search(r'[.!?]$', s):
                    return False
                w = s.split()
                return 1 <= len(w) <= 3 and w[0][:1].isupper()

            out = []
            i = 0
            n = len(items)

            while i < n:
                pid, txt, tag = items[i]
                t = normalize_ws(txt)

                has_prev = bool(out) and out[-1][0] == pid and out[-1][2] == tag
                prev_txt = out[-1][1] if has_prev else None

                has_next = (i + 1 < n) and (items[i+1][0] == pid) and (items[i+1][2] == tag)
                next_txt = normalize_ws(items[i+1][1]) if has_next else None

                # 0) URL-only / (URL) / angka+URL
                if (url_only.match(t) or paren_url_only.match(t) or num_then_url.match(t)) and has_prev:
                    out[-1][1] = normalize_ws(prev_txt.rstrip() + " " + t)
                    i += 1
                    continue

                # 1) Pembuka sangat pendek ATAU typo ':.', ';' → gabung ke berikutnya
                if has_next and (_is_very_short_opening(t) or t.rstrip().endswith(':.') or t.rstrip().endswith(';')):
                    out.append([pid, normalize_ws(t.rstrip() + " " + next_txt.lstrip()), tag])
                    i += 2
                    continue

                # 1b) *** BARU: frasa pendek 'title-ish' (mis. "Kepuasan (satisfaction)") → tempel ke sebelumnya
                if has_prev and _is_short_titleish(t):
                    out[-1][1] = normalize_ws(prev_txt.rstrip() + " " + t)
                    i += 1
                    continue

                # 2) "Risti?" + "(2014, hlm. 4) mengatakan …" → jika next mulai "(YYYY ... ) <teks>"
                if has_next and t.rstrip().endswith('?') and starts_paren_year_then_text.match(next_txt):
                    out.append([pid, normalize_ws(t + " " + next_txt), tag])
                    i += 2
                    continue
                
                # 2b) Prev berakhir "et al." / "dkk." + next mulai "(YYYY) ...": gabung
                if has_prev and prev_etal_end.search(prev_txt) and starts_paren_year_then_text.match(t):
                    out[-1][1] = normalize_ws(prev_txt.rstrip() + " " + t)
                    i += 1
                    continue


                # 3) Ekor sitasi luas (huruf / angka / paren tanpa tahun) → tempel ke sebelumnya
                if has_prev and (tail_citation_paren.match(t) or tail_author_year.match(t) or tail_source_year_num.match(t) or tail_paren_no_year.match(t)):
                    out[-1][1] = normalize_ws(prev_txt.rstrip() + " " + t)
                    i += 1
                    continue

                # 3b) *** BARU: baris mengandung 'diakses/accessed/retrieved' → tempel ke sebelumnya
                if has_prev and tail_access_note.search(t):
                    out[-1][1] = normalize_ws(prev_txt.rstrip() + " " + t)
                    i += 1
                    continue

                # 4) UD./CV./PT./TBK./RS./PD./MT./KH. berdiri sendiri → zip kiri+kanan
                if corp_abbr_only.match(t) and has_prev:
                    merged = prev_txt.rstrip() + " " + t
                    consumed = 1
                    if has_next:
                        merged += " " + next_txt
                        consumed = 2
                    out[-1][1] = normalize_ws(merged)
                    i += consumed
                    continue

                # 5) Prev berakhir singkatan → tempel lanjutan nama/alamat/abbr dalam paren
                if has_prev and ends_with_corp.search(prev_txt) and (starts_with_name.match(t) or short_name_line.match(t) or lone_paren_abbr.match(t)):
                    out[-1][1] = normalize_ws(prev_txt.rstrip() + " " + t)
                    i += 1
                    continue

                # 6) Node. JS / Socket. IO
                if has_prev and word_dot_prev.match(prev_txt) and next_allcaps.match(t):
                    out[-1][1] = normalize_ws(prev_txt.rstrip() + " " + t)
                    i += 1
                    continue

                # 7) Prev "Brand." + next "Com/Net/Org/Id ..." → jadikan "Brand.Com ..."
                if has_prev and prev_word_dot.search(prev_txt) and tld_only_start.match(t):
                    out[-1][1] = normalize_ws(re.sub(r'\.\s+$', '.', prev_txt) + t)
                    i += 1
                    continue

                # 8) Lone paren abbreviation (IPK). → tempel ke sebelumnya
                if has_prev and lone_paren_abbr.match(t):
                    out[-1][1] = normalize_ws(prev_txt.rstrip() + " " + t)
                    i += 1
                    continue

                # 9) Rangkaian setelah ':' → gulung (daftar/pertanyaan/“Diakses : …”, “Rayon 2 : …” dst.)
                if t.rstrip().endswith(':') and has_next:
                    buf = t
                    j = i + 1
                    while j < n and items[j][0] == pid and items[j][2] == tag:
                        nxt = normalize_ws(items[j][1])
                        if (short_q.match(nxt)
                            or nxt[0:1].islower()
                            or nxt.rstrip().endswith(':')
                            or re.search(r'^(Diakses\s*:|InfoKomputer\s*:|Rayon\s*\d+\s*:|\d+\s*\w*)', nxt, re.I)):
                            buf = normalize_ws(buf + " " + nxt); j += 1; continue
                        if not re.search(r'[.!?]$', buf):
                            buf = normalize_ws(buf + " " + nxt); j += 1; continue
                        break
                    out.append([pid, buf, tag])
                    i = j
                    continue

                # 10) Teks tak seimbang kurung/petik → rekatkan agar seimbang
                if _unbalanced_paren_or_quote(t):
                    if has_prev:
                        out[-1][1] = normalize_ws(prev_txt.rstrip() + " " + t)
                        i += 1
                        continue
                    elif has_next and not _unbalanced_paren_or_quote(next_txt):
                        out.append([pid, normalize_ws(t + " " + next_txt), tag])
                        i += 2
                        continue

                # default
                out.append([pid, t, tag])
                i += 1

            return out


        daftarKalimatProposal = [
            [pid, _recover_overmerged_words(kal), tag]
            for pid, kal, tag in daftarKalimatProposal
        ]

        daftarKalimatProposal = cross_glue_items(daftarKalimatProposal)

        # Cari kalimat pendek
        listKalimatPendek = [k for k in daftarKalimatProposal if len(k[1].split()) <= 5]

        # daftarLink sekarang berisi semua URL unik yang dimasking selama proses

        return daftarKalimatProposal
