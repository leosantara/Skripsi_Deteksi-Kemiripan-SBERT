package org.ukdw.dto.request.similarity;

import lombok.Data;

@Data
public class ProposalDraftDTO {
    // Tidak butuh ID Proposal karena ini data baru/draft
    private String nim;

    private String judul;
    private String latarBelakang;
    private String rumusan; // Sesuaikan dengan key di Vue ('rumusan')
    private String batasan;
    private String tujuan;
    private String manfaat;
    private String referensi;

    // Dosen ID usulan (opsional, tidak dipakai Python tapi dikirim Vue)
    private Integer dosenId;
}