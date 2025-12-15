import re
import html
import unicodedata as ud  # kalau tidak dipakai boleh dihapus
from KalimatBreak import breakKalimat


def _fix_em_dot_rule(text: str) -> str:
    """
    Ubah pola:  <word></em>. <word>
    sesuai aturan kapitalisasi yang diminta.
    """
    # word = huruf awal (Unicode letter) lalu susulan \w/- (biar aman utk bahasa Indo/Inggris)
    pat = re.compile(
        r'(?P<left>\b[^\W\d_][\w\-]*)\s*</em>\.\s+(?P<right>[^\W\d_][\w\-]*)',
        re.UNICODE
    )

    out = []
    last = 0
    for m in pat.finditer(text):
        lw = m.group('left')
        rw = m.group('right')

        l_upper = lw[0].isupper()
        l_lower = lw[0].islower()
        r_upper = rw[0].isupper()
        r_lower = rw[0].islower()

        # default: boundary kalimat
        sep = ". "
        new_lw = lw

        if l_lower and r_lower:
            sep = ", "
            new_lw = lw
        elif l_upper and r_upper:
            sep = ". "
            new_lw = lw
        elif l_upper and r_lower:
            sep = ", "
            # decapitalize kata sebelum
            new_lw = lw[0].lower() + lw[1:]
        elif l_lower and r_upper:
            sep = ". "
            new_lw = lw

        # tambahkan bagian sebelum match
        out.append(text[last:m.start('left')])
        # susun pengganti
        out.append(new_lw + sep + rw)
        last = m.end('right')

    out.append(text[last:])
    return "".join(out)


def removeHTMLString(data: str) -> str:
    """
    Membersihkan tag HTML & entitas spesifik ScripTI + normalisasi ringan.
    """
    if data is None:
        data = ""

    # Kompleks Tag HTML
    data = data.replace(
        '<p class=\"MsoNormal\" style=\"margin-left:.25in;text-align:justify;text-indent:\r\n.25in;line-height:150%\"><span style=\"font-family:times new roman,serif; font-size:12.0pt; line-height:150%\">',
        ""
    )
    data = data.replace("<o:p></o:p>", "")
    data = data.replace(
        "<p class=\"MsoNormal\" style=\"margin-left:.25in;text-align:justify;text-indent:\r\n.25in;line-height:150%\">",
        ""
    )
    data = data.replace(
        "</p>\r\n\r\n<p><!--[if supportFields]><b style=\'mso-bidi-font-weight:normal\'><span lang=ZH-CN\r\nstyle=\'font-size:12.0pt;line-height:107%;font-family:\"Times New Roman\",serif;\r\nmso-fareast-font-family:Cambria;mso-ansi-language:ZH-CN;mso-fareast-language:\r\nZH-CN;mso-bidi-language:AR-SA\'><span style=\'mso-element:field-end\'></span></span></b><![endif]--></p>\r\n",
        ""
    )
    data = data.replace(
        "<p><!--[if supportFields]><b style=\'mso-bidi-font-weight:\r\nnormal\'><span lang=ZH-CN style=\'font-size:12.0pt;line-height:107%;font-family:\r\n\"Times New Roman\",serif;mso-fareast-font-family:Cambria\'><span\r\nstyle=\'mso-element:field-begin\'></span></span></b><b style=\'mso-bidi-font-weight:\r\nnormal\'><span style=\'font-size:12.0pt;line-height:107%;font-family:\"Times New Roman\",serif;\r\nmso-fareast-font-family:Cambria;mso-ansi-language:EN-US\'><span\r\nstyle=\'mso-spacerun:yes\'> </span>BIBLIOGRAPHY<span style=\'mso-spacerun:yes\'> \r\n</span>\\l 1033 </span></b><b style=\'mso-bidi-font-weight:normal\'><span\r\nlang=ZH-CN style=\'font-size:12.0pt;line-height:107%;font-family:\"Times New Roman\",serif;\r\nmso-fareast-font-family:Cambria\'><span style=\'mso-element:field-separator\'></span></span></b><![endif]-->",
        ""
    )
    data = data.replace(
        "<!--[if supportFields]><b style=\'mso-bidi-font-weight:\r\nnormal\'><span style=\'font-size:12.0pt;line-height:150%;font-family:\"Times New Roman\",serif;\r\nmso-fareast-font-family:\"Times New Roman\";color:black\'><span style=\'mso-element:\r\nfield-begin;mso-field-lock:yes\'></span>",
        ""
    )
    data = data.replace(
        "<span style=\'mso-element:field-separator\'></span></span></b><![endif]-->",
        ""
    )
    data = data.replace(
        "</p>\r\n\r\n<p><!--[if supportFields]><b\r\nstyle=\'mso-bidi-font-weight:normal\'><span style=\'font-size:12.0pt;line-height:\r\n150%;font-family:\"Times New Roman\",serif;mso-fareast-font-family:\"Times New Roman\";\r\ncolor:black\'><span style=\'mso-element:field-end\'></span></span></b><![endif]--><strong>&nbsp;</strong></p>\r\n",
        ""
    )
    data = data.replace(
        "INSERT INTO `proposals` (`id`, `nim`, `periode_id`, `judul`, `latar_belakang`, `rumusan`, `batasan`, `tujuan`, `referensi`, `status`, `user_id`, `modified`, `filename`, `filedir`, `mime_type`, `manfaat`, `revisi_dari`, `dosen_id`, `upload_revisi`) VALUES",
        ""
    )
    data = data.replace(":</p>\r\n\r\n<p>", ": ")
    data = data.replace(")</p>\r\n\r\n<p>", "). ")
    data = data.replace(".</p>\r\n\r\n<p>", ". ")
    data = data.replace("</p>\r\n\r\n<p>", " ")
    data = data.replace(":\r\na", ": ")

    # Variasi Tag P
    data = data.replace("<p>?</p>", "")
    data = data.replace("1.</p>", "")
    data = data.replace(".?</p>", ".")
    data = data.replace(".</p>", ".")
    data = data.replace(")</p>", ").")
    data = data.replace('</p>', "")
    data = data.replace("</p>", "")
    data = data.replace("<p>", "")
    data = data.replace("./p>", ".")

    # Variasi Entitas
    data = data.replace("&#39;", "`")
    data = data.replace("&hellip;..", "")
    data = data.replace("&nbsp;", " ")
    data = data.replace("&iuml;", "ï")
    data = data.replace("&igrave;;", "ì")
    data = data.replace("&uuml;", "ü")
    data = data.replace("&ouml;", "ö")
    data = data.replace("&iacute;", "í")
    data = data.replace("&egrave;", "è")
    data = data.replace("&eacute;", "é")
    data = data.replace("&agrave;", "à")
    data = data.replace("v&gt;", "")

    # Variasi gagal encoding
    data = data.replace("p?ny?n", "pinyin")
    data = data.replace("h?nz?", "hanzi")

    # Variasi strong
    data = data.replace("<strong>", "")
    data = data.replace("/strong", "")

    # Variasi ol li
    data = data.replace("<ol><li><em>", "")
    data = data.replace("<li><em>", ", ")
    data = data.replace(".</li>\r\n", ", ")
    data = data.replace("</li>", "")
    data = data.replace("<li>", " ")
    data = data.replace("<ul>", "")
    data = data.replace("</ul>", "")
    data = data.replace("<ul", "")
    data = data.replace("</ul", "")
    data = data.replace("<ol>", "")
    data = data.replace("</ol>", "")
    data = data.replace("<>", "")

    # Variasi tab
    data = data.replace("\t1", "")
    data = data.replace("\t2", "")
    data = data.replace("\t", "")

    # Variasi lainnya
    data = data.replace("\r", "")
    data = data.replace("</span>", "")
    data = data.replace("<span>", "")
    data = data.replace("<br />", "")
    data = data.replace("<br/>", "")
    data = data.replace("</em>", "")
    data = data.replace("<em>", "")
    data = data.replace("\n", "")
    data = data.replace("/b", "")

    # Aturan custom
    data = _fix_em_dot_rule(data)

    # Hilangkan penanda [number] (mis. [1], [23])
    data = re.sub(r'\[\s*\d+\s*\]\s*', '', data)

    # Ubah ".c." → ". c."
    data = re.sub(r'\.\s*([A-Za-z])\s*\.', r'. \1.', data)

    # Gabungkan tanda baca berulang "???", "..??", dll menjadi satu
    data = re.sub(r'([.!?])(?:\s*[.!?])+(?=\s|$)', r'\1', data)

    # Hilangkan sisa tag HTML generik
    data = re.sub(r'<[^>]+>', '', data)

    # Decode entitas HTML
    data = html.unescape(data)

    return data


def NormalizeDataInput(payload):
    """
    Normalisasi payload proposal baru menjadi daftar paragraf:
    [
      [proposal_id, "Judul", judul_bersih],
      [proposal_id, "LatarBelakang", latar_belakang_bersih],
      [proposal_id, "Rumusan", rumusan_bersih],
      [proposal_id, "Tujuan", tujuan_bersih],
    ]
    Bisa menerima dict atau Pydantic BaseModel.
    """
    # dukung dict atau BaseModel
    if hasattr(payload, "dict"):
        payload = payload.dict()

    proposal_id = payload.get("proposal_id")

    data_to_process = [
        ("judul", "Judul"),
        ("latar_belakang", "LatarBelakang"),
        ("rumusan", "Rumusan"),
        ("tujuan", "Tujuan"),
        # kalau nanti mau ikutkan batasan/manfaat, tinggal tambah di sini:
        # ("batasan", "Batasan"),
        # ("manfaat", "Manfaat"),
    ]

    paragraf = []
    for key, label in data_to_process:
        raw_value = payload.get(key, "") or ""
        cleaned = removeHTMLString(raw_value)
        paragraf.append([proposal_id, label, cleaned])

    return paragraf


def _normalize_ws_final(s: str) -> str:
    """
    Normalisasi whitespace ringan (spasi ganda, leading/trailing).
    """
    s = re.sub(r"\s+", " ", str(s)).strip()
    return s


def _fix_name_typos_final(s: str) -> str:
    """
    Perbaiki typo nama bertipe 'Bove? e' → 'Bove e'
    """
    return re.sub(r'([A-Za-z])\?\s*([A-Za-z])', r'\1\2', s)


def preprocess_and_split_proposal(payload):
    """
    Main entry yang dipanggil dari FastAPI:
    - payload: dict / ProposalPayload
    - return: list of dict:
        {
          "sentence_id": int,
          "proposal_id": str,
          "aspect": str,
          "text": str
        }
    """
    # 1) Normalisasi & bersihkan HTML → list [pid, Label, IsiBersih]
    daftarParagraf = NormalizeDataInput(payload)

    # 2) Pecah jadi kalimat dengan KalimatBreak
    #    diasumsikan breakKalimat mengembalikan:
    #    [ [idProposal, isiKalimat, aspek], ... ]
    daftarKalimatProposal = breakKalimat(daftarParagraf)

    # 3) Susun output final untuk API
    result = []
    for idx, (pid, txt, asp) in enumerate(daftarKalimatProposal, start=1):
        txt_bersih = _fix_name_typos_final(_normalize_ws_final(txt))
        result.append(
            {
                "sentence_id": idx,
                "proposal_id": str(pid),
                "aspect": str(asp).lower(),
                "text": txt_bersih.lower(),
            }
        )

    return result

print("TEs")