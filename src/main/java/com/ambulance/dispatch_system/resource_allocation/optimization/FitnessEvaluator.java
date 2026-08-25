package com.ambulance.dispatch_system.resource_allocation.optimization;

import com.ambulance.dispatch_system.common.entity.Ambulance;
import com.ambulance.dispatch_system.common.entity.RoadNode;
import com.ambulance.dispatch_system.common.entity.enums.MedicalEquipment;
import com.ambulance.dispatch_system.routing.dto.RouteResponse;
import com.ambulance.dispatch_system.routing.service.RouteService;
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

    private final RouteService routeService;

    public FitnessEvaluator(RouteService routeService) {
        this.routeService = routeService;
    }

    public double calculateFitness(Ambulance ambulance, String patientNodeName, Set<MedicalEquipment> requiredEquipment,
                                   List<RoadNode> allNodes) {

        // An ambulance with no recorded position has no vertex to route from, and a call
        // with no location has no destination.
        if (ambulance.getCurrentLocationNode() == null || patientNodeName == null) {
            return UNREACHABLE;
        }

        Long ambulanceNodeId = null;
        Long patientNodeId = null;

        for (RoadNode node : allNodes) {
            if (node.getName().equals(ambulance.getCurrentLocationNode())) {
                ambulanceNodeId = node.getId();
            }
            if (node.getName().equals(patientNodeName)) {
                patientNodeId = node.getId();
            }
        }

        if (ambulanceNodeId == null || patientNodeId == null) {
            return UNREACHABLE;
        }

        double travelTime;
        try {
            // 1. Real Distance Check using the routing module
            RouteResponse response = routeService.findRoute(ambulanceNodeId, patientNodeId);
            travelTime = response.getTotalTravelTimeMinutes();
        } catch (IllegalStateException | IllegalArgumentException e) {
            // The routing module throws an exception if no route is found or node is invalid
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