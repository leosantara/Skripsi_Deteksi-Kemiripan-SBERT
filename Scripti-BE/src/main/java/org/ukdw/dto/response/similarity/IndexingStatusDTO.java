package org.ukdw.dto.response.similarity;

import lombok.Data;

@Data
public class IndexingStatusDTO {
    private boolean isRunning;
    private double progressPercent;
    private String message;
    private long lastUpdateTime;
    private long startTime;
}