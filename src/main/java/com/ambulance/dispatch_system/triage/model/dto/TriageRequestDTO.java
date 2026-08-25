package com.ambulance.dispatch_system.triage.model.dto;

import com.ambulance.dispatch_system.triage.model.enums.ConsciousnessLevel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class TriageRequestDTO {

    @NotNull
    private Boolean breathing;

    @NotNull
    @Min(0)
    @Max(300)
    private Integer pulseRate;

    @NotNull
    private ConsciousnessLevel avpu;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer oxygenSaturation;

    @NotNull
    @Min(0)
    @Max(300)
    private Integer systolicBP;

    @NotNull
    @Min(0)
    @Max(10)
    private Integer painScore;

    @NotNull
    @Min(20)
    @Max(45)
    private Double temperature;

    @NotNull
    @Min(0)
    @Max(130)
    private Integer age;

    @NotNull
    private Boolean hazardPresent;

    private List<String> symptoms;

    public TriageRequestDTO() {}

    public TriageRequestDTO(Boolean breathing, Integer pulseRate, ConsciousnessLevel avpu, 
                            Integer oxygenSaturation, Integer systolicBP, Integer painScore, 
                            Double temperature, Integer age, Boolean hazardPresent, List<String> symptoms) {
        this.breathing = breathing;
        this.pulseRate = pulseRate;
        this.avpu = avpu;
        this.oxygenSaturation = oxygenSaturation;
        this.systolicBP = systolicBP;
        this.painScore = painScore;
        this.temperature = temperature;
        this.age = age;
        this.hazardPresent = hazardPresent;
        this.symptoms = symptoms;
    }

    public Boolean getBreathing() { return breathing; }
    public void setBreathing(Boolean breathing) { this.breathing = breathing; }

    public Integer getPulseRate() { return pulseRate; }
    public void setPulseRate(Integer pulseRate) { this.pulseRate = pulseRate; }

    public ConsciousnessLevel getAvpu() { return avpu; }
    public void setAvpu(ConsciousnessLevel avpu) { this.avpu = avpu; }

    public Integer getOxygenSaturation() { return oxygenSaturation; }
    public void setOxygenSaturation(Integer oxygenSaturation) { this.oxygenSaturation = oxygenSaturation; }

    public Integer getSystolicBP() { return systolicBP; }
    public void setSystolicBP(Integer systolicBP) { this.systolicBP = systolicBP; }

    public Integer getPainScore() { return painScore; }
    public void setPainScore(Integer painScore) { this.painScore = painScore; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Boolean getHazardPresent() { return hazardPresent; }
    public void setHazardPresent(Boolean hazardPresent) { this.hazardPresent = hazardPresent; }

    public List<String> getSymptoms() { return symptoms; }
    public void setSymptoms(List<String> symptoms) { this.symptoms = symptoms; }
}
