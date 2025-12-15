package org.ukdw.dto.response.similarity;

import lombok.Data;
import java.util.List;

@Data
public class ClusterDetailDTO {
    private String name;
    private List<Integer> dosen1; // ID dari Python
    private List<Integer> dosen2; // ID dari Python

    // Output ke Frontend (Nama)
    private List<String> dosen1Names;
    private List<String> dosen2Names;
}