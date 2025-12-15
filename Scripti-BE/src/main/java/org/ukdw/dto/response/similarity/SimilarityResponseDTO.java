package org.ukdw.dto.response.similarity;

import lombok.Data;

import java.util.List;

@Data
public class SimilarityResponseDTO {
    private Long queryProposalId;
    private String modelName;
    private int topK;
    private List<SimilarityProposalDTO> results;

    // Tambahan baru
    private List<DosenRecommendationDTO> dosenRecommendations;
}