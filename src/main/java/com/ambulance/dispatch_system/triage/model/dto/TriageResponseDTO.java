package com.ambulance.dispatch_system.triage.model.dto;

import com.ambulance.dispatch_system.triage.model.enums.TriageCategory;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object used to return the result of a triage assessment.
 *
 * Contains the assigned triage category, calculated score, position
 * in the active queue, and the time at which the result was generated.
 */
public class TriageResponseDTO {

    private UUID id;
    private TriageCategory category;
    private Double score;
    private Integer queuePosition;
    private LocalDateTime timestamp;

    /**
     * Default constructor required for DTO serialization and deserialization.
     */
    public TriageResponseDTO() {
    }

    /**
     * Creates a response containing the calculated triage information.
     *
     * @param id assessment identifier
     * @param category assigned triage category
     * @param score calculated triage score
     * @param queuePosition patient's position in the triage queue
     * @param timestamp time when the triage result was generated
     */
    public TriageResponseDTO(
            UUID id,
            TriageCategory category,
            Double score,
            Integer queuePosition,
            LocalDateTime timestamp) {

        this.id = id;
        this.category = category;
        this.score = score;
        this.queuePosition = queuePosition;
        this.timestamp = timestamp;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public TriageCategory getCategory() {
        return category;
    }

    public void setCategory(TriageCategory category) {
        this.category = category;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Integer getQueuePosition() {
        return queuePosition;
    }

    public void setQueuePosition(Integer queuePosition) {
        this.queuePosition = queuePosition;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
