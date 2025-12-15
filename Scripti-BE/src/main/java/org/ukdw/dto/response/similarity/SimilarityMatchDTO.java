package org.ukdw.dto.response.similarity;

import lombok.Data;

@Data
public class SimilarityMatchDTO {
    private String aspectInput;
    private String inputSentence;
    private String aspectDb;
    private String dbSentence;
    private double similarity;


}