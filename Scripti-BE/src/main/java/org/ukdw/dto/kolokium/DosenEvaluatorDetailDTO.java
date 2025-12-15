package org.ukdw.dto.kolokium;

import lombok.Data;
import org.ukdw.entity.GroupEvaluatorEntity;
import org.ukdw.entity.PeriodeKolokiumEntity;

import java.util.List;

@Data
public class DosenEvaluatorDetailDTO {
    private Long sidangId;
    private List<String> kelompok;
    private PeriodeKolokiumEntity periodeKolokium;
    private StatusDosenEvaluatorDTO ketua;
    private List<StatusDosenEvaluatorDTO> anggota;
}
