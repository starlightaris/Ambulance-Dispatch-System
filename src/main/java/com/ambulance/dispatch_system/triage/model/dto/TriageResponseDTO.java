package com.ambulance.dispatch_system.triage.model.dto;

import com.ambulance.dispatch_system.triage.model.enums.TriageCategory;
import java.time.LocalDateTime;

public class TriageResponseDTO {

    private TriageCategory category;
    private Double score;
    private Integer queuePosition;
    private LocalDateTime timestamp;

    public TriageResponseDTO() {}

    public TriageResponseDTO(TriageCategory category, Double score, Integer queuePosition, LocalDateTime timestamp) {
        this.category = category;
        this.score = score;
        this.queuePosition = queuePosition;
        this.timestamp = timestamp;
    }

    public TriageCategory getCategory() { return category; }
    public void setCategory(TriageCategory category) { this.category = category; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public Integer getQueuePosition() { return queuePosition; }
    public void setQueuePosition(Integer queuePosition) { this.queuePosition = queuePosition; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
