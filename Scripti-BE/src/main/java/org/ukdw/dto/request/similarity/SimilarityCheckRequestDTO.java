package org.ukdw.dto.request.similarity;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SimilarityCheckRequestDTO {
    // Gunakan JsonProperty agar sesuai dengan snake_case di Python
    @JsonProperty("proposal_id")
    private Long proposalId;

    private String judul;

    @JsonProperty("latar_belakang")
    private String latarBelakang;

    private String rumusan; // Python pakai 'rumusan' bukan 'rumusanMasalah'
    private String tujuan;
}