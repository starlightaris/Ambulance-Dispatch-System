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

    /**
     * Score given to an ambulance that cannot reach the patient at all. Callers must
     * filter these out instead of comparing them, because every unreachable ambulance
     * scores identically.
     */
    public static final double UNREACHABLE = Double.POSITIVE_INFINITY;

    private final DijkstraBlindSpotOptimizer dijkstraOptimizer;

    public FitnessEvaluator(DijkstraBlindSpotOptimizer dijkstraOptimizer) {
        this.dijkstraOptimizer = dijkstraOptimizer;
    }

    public double calculateFitness(Ambulance ambulance, String patientNode, Set<MedicalEquipment> requiredEquipment,
                                   List<RoadNode> allNodes, List<RoadEdge> allEdges) {

        // An ambulance with no recorded position has no vertex to route from, and a call
        // with no location has no destination. The optimizer reports both as 0.0, which
        // would make them look like they are already at the patient and win every time.
        if (ambulance.getCurrentLocationNode() == null || patientNode == null) {
            return UNREACHABLE;
        }

        // 1. Real Distance Check using the network module
        double travelTime = dijkstraOptimizer.calculateShortestTravelTime(
                ambulance.getCurrentLocationNode(), patientNode, allNodes, allEdges);

        // The optimizer reports "no route" as Double.MAX_VALUE (unknown node name, or every
        // path blocked). Adding the equipment penalty to MAX_VALUE leaves it unchanged, so
        // unreachable ambulances would silently tie and be picked by list order.
        if (travelTime >= Double.MAX_VALUE) {
            return UNREACHABLE;
        }

        double score = travelTime;

        // 2. Resource Waste Check
        int extraEquipment = ambulance.getEquipment().size() - requiredEquipment.size();
        if (extraEquipment > 0) {
            score += (extraEquipment * 5.0);
        }

        return score;
    }
}
