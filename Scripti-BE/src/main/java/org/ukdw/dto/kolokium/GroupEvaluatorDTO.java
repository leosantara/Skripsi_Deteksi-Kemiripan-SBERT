package org.ukdw.dto.kolokium;

import lombok.Data;

import java.util.Date;

@Data
public class GroupEvaluatorDTO {
    private Long id;
    private String kelompok;
    private Date waktu;
    private Long periodeId;
}

