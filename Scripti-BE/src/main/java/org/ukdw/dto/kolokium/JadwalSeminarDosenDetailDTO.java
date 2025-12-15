package org.ukdw.dto.kolokium;

import lombok.Data;

import java.util.List;

@Data
public class JadwalSeminarDosenDetailDTO {
    private Long jadwalSeminarId;
    private StatusJadwalSeminarDosenDTO ketua;
    private List<StatusJadwalSeminarDosenDTO> anggota;
}
