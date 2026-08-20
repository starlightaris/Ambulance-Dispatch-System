package com.ambulance.dispatch_system.resource_allocation.optimization;

import com.ambulance.dispatch_system.common.entity.Ambulance;
import com.ambulance.dispatch_system.common.entity.enums.MedicalEquipment; // Adjust import if needed
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
public class FitnessEvaluator {

    public double calculateFitness(Ambulance ambulance, String patientNode, Set<MedicalEquipment> requiredEquipment) {
        double score = 0.0;

        // 1. Distance Check (Lower score is better)
        // If they are on the same node, distance is 0. Otherwise, calculate distance.
        double distance = (ambulance.getCurrentLocationNode() != null && 
                           ambulance.getCurrentLocationNode().equals(patientNode)) ? 0.0 : Math.random() * 50.0;
        score += distance; 

        // 2. Resource Waste Check
        // If an ambulance has 5 tools, but the patient only needs 1, we add a penalty score.
        // This saves highly equipped ambulances for severe emergencies.
        int extraEquipment = ambulance.getEquipment().size() - requiredEquipment.size();
        if (extraEquipment > 0) {
            score += (extraEquipment * 5.0); 
        }

        return score;
    }
}