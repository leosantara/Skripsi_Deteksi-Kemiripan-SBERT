package org.ukdw.dto.kolokium;


import lombok.Data;

@Data
public class ProposalByPeriodeAndStatusRequestDTO {

    private Long periodeId;

    private String status;
}