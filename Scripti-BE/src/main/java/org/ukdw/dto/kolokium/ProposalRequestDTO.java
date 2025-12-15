package org.ukdw.dto.kolokium;

import lombok.Data;

@Data
public class ProposalRequestDTO {
    private Long id;
    private String nim;  // Untuk mahasiswa
    private String nidn; // Untuk dosen
}
