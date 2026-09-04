package com.ambulance.dispatch_system.resource_allocation.optimization;

import com.ambulance.dispatch_system.common.entity.Ambulance;
import com.ambulance.dispatch_system.common.entity.RoadNode;
import com.ambulance.dispatch_system.common.entity.enums.MedicalEquipment;
import com.ambulance.dispatch_system.routing.dto.RouteResponse;
import com.ambulance.dispatch_system.routing.exception.LocationNotFoundException;
import com.ambulance.dispatch_system.routing.exception.RouteNotFoundException;
import com.ambulance.dispatch_system.routing.service.RouteService;
import com.ambulance.dispatch_system.routing.service.RoutingSnapshot;
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

    public double calculateFitness(Ambulance ambulance, String patientNodeName, Set<MedicalEquipment> requiredEquipment) {
        return calculateFitness(ambulance, patientNodeName, requiredEquipment, routeService::findRoute);
    }

    /**
     * Scores one ambulance against an already-loaded {@link RoutingSnapshot} instead of going
     * through RouteService directly. Callers scoring a whole fleet in one dispatch decision
     * (see GreedyScheduler) should load a single snapshot up front and reuse it across every
     * candidate, rather than letting each call trigger its own fresh database fetch of the full
     * road network.
     */
    public double calculateFitness(Ambulance ambulance, String patientNodeName, Set<MedicalEquipment> requiredEquipment,
                                    RoutingSnapshot routingSnapshot) {

        // An ambulance with no recorded position has no vertex to route from, and a call
        // with no location has no destination.
        if (ambulance.getCurrentLocationNode() == null || patientNodeName == null) {
            return UNREACHABLE;
        }

        double travelTime;
        try {
            RouteResponse response = routingSnapshot.findRoute(ambulance.getCurrentLocationNode(), patientNodeName);
            travelTime = response.getTotalTravelTimeMinutes();
        } catch (LocationNotFoundException | RouteNotFoundException e) {
            // The routing module throws an exception if no route is found or node is invalid
            return UNREACHABLE;
        }

        double score = travelTime;

        // Penalize carrying equipment the call doesn't need - it's capacity wasted on this job
        // that a more specialized ambulance elsewhere might have needed.
        int extraEquipment = ambulance.getEquipment().size() - requiredEquipment.size();
        if (extraEquipment > 0) {
            score += (extraEquipment * 5.0);
        }

        return score;
    }

    /**
     * Loads a reusable road-network snapshot for scoring an entire fleet in one dispatch
     * decision. See {@link RouteService#loadSnapshot()}.
     */
    public RoutingSnapshot newRoutingSnapshot() {
        return routeService.loadSnapshot();
    }
}