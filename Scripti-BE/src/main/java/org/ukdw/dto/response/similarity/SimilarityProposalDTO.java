package org.ukdw.dto.response.similarity;

import lombok.Data;

import java.util.List;

@Data
public class SimilarityProposalDTO {
    private Long proposalId;
    private double score;
    private String judul;

    // --- Statistik Baru dari Python ---
    private int totalSentences;
    private int similarCount;
    private int ignoredCount;
    // ----------------------------------

    private String latarBelakang;
    private String rumusanMasalah;
    private String tujuan;
    private String batasan;
    private String manfaat;

    private List<SimilarityMatchDTO> matches;
}