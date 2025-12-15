package org.ukdw.dto.kolokium;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class StatusDosenEvaluatorDTO {
    private Long dosenId;
    private String dosenNama;
    private String status;
}
