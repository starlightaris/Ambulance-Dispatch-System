package com.ambulance.dispatch_system.triage.service.impl.algorithms;

import com.ambulance.dispatch_system.triage.model.dto.TriageRequestDTO;
import org.springframework.stereotype.Component;

/**
 * Calculates a tie-breaker score to rank patients within the same Triage Category.
 * 
 * Time Complexity: O(1)
 * Justification: The scoring evaluates a fixed number of physiological parameters
 * using simple arithmetic operations, bounded independently of input size.
 */
@Component
public class WeightedScoringStrategy {

    // Configurable weight constants
    private static final double SPO2_DEFICIT_WEIGHT = 2.0; // Per 1% below 100
    private static final double PAIN_SCORE_WEIGHT = 1.5;   // Per point of pain
    private static final double AGE_RISK_BONUS = 5.0;      // Flat bonus for >65 or <5
    private static final double HAZARD_BONUS = 10.0;       // Flat bonus if hazard is present

    public double calculateScore(TriageRequestDTO request) {
        double score = 0.0;

        // SpO2 deficit (100 - actual)
        int spo2Deficit = Math.max(0, 100 - request.getOxygenSaturation());
        score += (spo2Deficit * SPO2_DEFICIT_WEIGHT);

        // Pain score contribution
        score += (request.getPainScore() * PAIN_SCORE_WEIGHT);

        // Age risk contribution
        if (request.getAge() > 65 || request.getAge() < 5) {
            score += AGE_RISK_BONUS;
        }

        // Hazard contribution
        if (Boolean.TRUE.equals(request.getHazardPresent())) {
            score += HAZARD_BONUS;
        }

        return score;
    }
}
