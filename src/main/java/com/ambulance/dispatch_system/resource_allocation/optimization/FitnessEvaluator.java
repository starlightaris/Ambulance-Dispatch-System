package com.ambulance.dispatch_system.resource_allocation.optimization;

import com.ambulance.dispatch_system.common.entity.Ambulance;
import com.ambulance.dispatch_system.common.entity.RoadEdge;
import com.ambulance.dispatch_system.common.entity.RoadNode;
import com.ambulance.dispatch_system.common.entity.enums.MedicalEquipment;
import com.ambulance.dispatch_system.network_detection.optimization.DijkstraBlindSpotOptimizer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class FitnessEvaluator {

    private final DijkstraBlindSpotOptimizer dijkstraOptimizer;

    public FitnessEvaluator(DijkstraBlindSpotOptimizer dijkstraOptimizer) {
        this.dijkstraOptimizer = dijkstraOptimizer;
    }

    public double calculateFitness(Ambulance ambulance, String patientNode, Set<MedicalEquipment> requiredEquipment,
                                   List<RoadNode> allNodes, List<RoadEdge> allEdges) {
        double score = 0.0;

        // 1. Real Distance Check using the network module
        double distance = dijkstraOptimizer.calculateShortestTravelTime(
                ambulance.getCurrentLocationNode(), patientNode, allNodes, allEdges);
        score += distance;

        // 2. Resource Waste Check
        int extraEquipment = ambulance.getEquipment().size() - requiredEquipment.size();
        if (extraEquipment > 0) {
            score += (extraEquipment * 5.0);
        }

        return score;
    }
}