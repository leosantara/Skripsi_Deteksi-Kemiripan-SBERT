package org.ukdw.dto.kolokium;

import lombok.Data;

@Data
public class SeminarUpdateDTO {
    private Integer id;
    private String status;
    private String nim;
    // Tambahkan field lainnya dan getter/setter
}
