package org.ukdw.dto.response.similarity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class DosenRecommendationDTO {
    private int clusterId;
    private String topicLabel;
    private double ratio;

    // --- DARI PYTHON (Input) ---
    @JsonProperty("recommendedDosen1Ids")
    private List<Integer> recommendedDosen1Ids;

    @JsonProperty("recommendedDosen2Ids")
    private List<Integer> recommendedDosen2Ids;

    // --- UNTUK VUE (Output setelah Enrichment) ---
    private List<String> dosen1Names;
    private List<String> dosen2Names;
}