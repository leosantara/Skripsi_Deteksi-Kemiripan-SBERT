package org.ukdw.dto.kolokium;

import lombok.Data;

import java.util.List;

@Data
public class JadwalSeminarDosenDTO {
    private Long seminarId;
    private Long ketuaId;
    private List<Long> anggotaIds;
}
