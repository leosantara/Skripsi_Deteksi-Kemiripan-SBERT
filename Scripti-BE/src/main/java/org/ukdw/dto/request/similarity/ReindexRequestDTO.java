package org.ukdw.dto.request.similarity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReindexRequestDTO {
    @JsonProperty("proposal_id")
    private Long proposalId;
    private String judul;
    @JsonProperty("latar_belakang")
    private String latarBelakang;
    private String rumusan;
    private String tujuan;
    @JsonProperty("id_dosen_1")
    private Integer idDosen1;
    @JsonProperty("id_dosen_2")
    private Integer idDosen2;
}