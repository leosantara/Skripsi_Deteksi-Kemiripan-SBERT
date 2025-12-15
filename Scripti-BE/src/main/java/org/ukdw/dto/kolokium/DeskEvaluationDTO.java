package org.ukdw.dto.kolokium;

import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
public class DeskEvaluationDTO {
    private LocalDate tanggalValid;
    private String catatan;
    private Date modified;
    private Integer userPenginputDEId;
    private String status;
    private Integer dosen1Id;
    private Integer dosen2Id;
    private Integer proposalId;
    private String judulLama;
    private String judulBaru;
    private String mahasiswaNim;
    private String uploadRevisi;
    private Integer dosenEvaluatorId;
    private Integer groupEvaluatorId;
}