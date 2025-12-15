package org.ukdw.dto.kolokium;

import lombok.Data;

@Data
public class StatusJadwalSeminarDosenDTO {
    private Long dosenId;
    private String dosenNama;
    private String status;

    // Constructor, Getter, Setter bisa dihasilkan otomatis oleh Lombok melalui anotasi @Data
}
