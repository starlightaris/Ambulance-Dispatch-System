package com.ambulance.dispatch_system.triage.entity;

import com.ambulance.dispatch_system.triage.model.enums.ConsciousnessLevel;
import com.ambulance.dispatch_system.triage.model.enums.TriageCategory;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a patient's triage assessment.
 *
 * Stores the patient's vital signs, symptoms, assigned triage category,
 * and queue status used by the ambulance dispatch system.
 */
@Entity
@Table(name = "triage_assessments")
public class TriageAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Basic patient vital signs and assessment information
    private Boolean breathing;
    private Integer pulseRate;

    @Enumerated(EnumType.STRING)
    private ConsciousnessLevel avpu;

    private Integer oxygenSaturation;
    private Integer systolicBP;
    private Integer painScore;
    private Double temperature;
    private Integer age;
    private Boolean hazardPresent;

    // Stores multiple symptoms associated with this assessment
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "triage_assessment_symptoms",
            joinColumns = @JoinColumn(name = "assessment_id")
    )
    @Column(name = "symptom")
    private List<String> symptoms;

    @Enumerated(EnumType.STRING)
    private TriageCategory assignedCategory;

    private Double tieBreakerScore;
    private LocalDateTime createdAt;

    // Indicates whether this assessment is still active in the queue
    private Boolean resolved;

    // Numeric severity value used for prioritising assessments
    private Integer severityRank;

    /**
     * Automatically initializes default values before persisting or updating
     * the assessment.
     *
     * The creation timestamp is assigned only when the entity is first
     * created. Unresolved assessments default to false, while the severity
     * rank is synchronized with the assigned triage category.
     */
    @PrePersist
    @PreUpdate
    protected void onPersistOrUpdate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (resolved == null) {
            resolved = false;
        }

        if (assignedCategory != null) {
            severityRank = assignedCategory.getSeverity();
        }
    }

    /**
     * Default constructor required by JPA.
     */
    public TriageAssessment() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Boolean getBreathing() {
        return breathing;
    }

    public void setBreathing(Boolean breathing) {
        this.breathing = breathing;
    }

    public Integer getPulseRate() {
        return pulseRate;
    }

    public void setPulseRate(Integer pulseRate) {
        this.pulseRate = pulseRate;
    }

    public ConsciousnessLevel getAvpu() {
        return avpu;
    }

    public void setAvpu(ConsciousnessLevel avpu) {
        this.avpu = avpu;
    }

    public Integer getOxygenSaturation() {
        return oxygenSaturation;
    }

    public void setOxygenSaturation(Integer oxygenSaturation) {
        this.oxygenSaturation = oxygenSaturation;
    }

    public Integer getSystolicBP() {
        return systolicBP;
    }

    public void setSystolicBP(Integer systolicBP) {
        this.systolicBP = systolicBP;
    }

    public Integer getPainScore() {
        return painScore;
    }

    public void setPainScore(Integer painScore) {
        this.painScore = painScore;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Boolean getHazardPresent() {
        return hazardPresent;
    }

    public void setHazardPresent(Boolean hazardPresent) {
        this.hazardPresent = hazardPresent;
    }

    public List<String> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms;
    }

    public TriageCategory getAssignedCategory() {
        return assignedCategory;
    }

    public void setAssignedCategory(TriageCategory assignedCategory) {
        this.assignedCategory = assignedCategory;
    }

    public Double getTieBreakerScore() {
        return tieBreakerScore;
    }

    public void setTieBreakerScore(Double tieBreakerScore) {
        this.tieBreakerScore = tieBreakerScore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getResolved() {
        return resolved;
    }

    public void setResolved(Boolean resolved) {
        this.resolved = resolved;
    }

    public Integer getSeverityRank() {
        return severityRank;
    }

    public void setSeverityRank(Integer severityRank) {
        this.severityRank = severityRank;
    }
}