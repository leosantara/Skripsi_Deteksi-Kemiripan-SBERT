package org.ukdw.dto.kolokium;

import lombok.Data;

import java.util.List;

@Data
public class DosenEvaluatorDTO {
    private Long sidangId;
    private Long ketuaId;
    private List<Long> anggotaIds;
}
