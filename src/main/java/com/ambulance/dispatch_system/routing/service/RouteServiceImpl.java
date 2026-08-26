package com.ambulance.dispatch_system.routing.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ambulance.dispatch_system.common.entity.RoadEdge;
import com.ambulance.dispatch_system.common.entity.RoadNode;
import com.ambulance.dispatch_system.common.repository.RoadEdgeRepository;
import com.ambulance.dispatch_system.common.repository.RoadNodeRepository;
import com.ambulance.dispatch_system.routing.algorithm.AStarAlgorithm;
import com.ambulance.dispatch_system.routing.dto.RouteRequest;
import com.ambulance.dispatch_system.routing.dto.RouteResponse;

@Service
public class RouteServiceImpl implements RouteService {

    private final RoadNodeRepository roadNodeRepository;
    private final RoadEdgeRepository roadEdgeRepository;
    private final AStarAlgorithm aStarAlgorithm;

    public RouteServiceImpl(
            RoadNodeRepository roadNodeRepository,
            RoadEdgeRepository roadEdgeRepository) {

        this.roadNodeRepository = roadNodeRepository;
        this.roadEdgeRepository = roadEdgeRepository;
        this.aStarAlgorithm = new AStarAlgorithm();
    }

    @Override
    public RouteResponse findRoute(RouteRequest request) {

        return findRoute(
                request.getStartLocationId(),
                request.getDestinationLocationId()
        );
    }

    /**
     * Reusable routing method for other modules.
     *
     * Other modules can use Task 1's A* routing
     * without implementing their own routing algorithm.
     */
    @Override
    public RouteResponse findRoute(
            Long startLocationId,
            Long destinationLocationId) {

        RoadNode start = roadNodeRepository
                .findById(startLocationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Start location not found"));

        RoadNode destination = roadNodeRepository
                .findById(destinationLocationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Destination location not found"));

        List<RoadEdge> availableEdges =
                roadEdgeRepository.findByBlockedFalse();

        List<RoadNode> route =
                aStarAlgorithm.findShortestPath(
                        start,
                        destination,
                        availableEdges);

        if (route.isEmpty()) {
            throw new IllegalStateException(
                    "No available route found between the selected locations");
        }

        double totalTravelTime =
                calculateTravelTime(
                        route,
                        availableEdges);

        double totalDistance =
                calculateDistance(
                        route,
                        availableEdges);

        return new RouteResponse(
                "ASTAR",
                totalTravelTime,
                totalDistance,
                route
        );
    }

    @Override
    public RouteResponse findRoute(String startNodeName, String destinationNodeName) {
        RoadNode start = roadNodeRepository.findByName(startNodeName)
                .orElseThrow(() -> new IllegalArgumentException("Start location not found"));
        RoadNode destination = roadNodeRepository.findByName(destinationNodeName)
                .orElseThrow(() -> new IllegalArgumentException("Destination location not found"));
        
        return findRoute(start.getId(), destination.getId());
    }

    private double calculateTravelTime(
            List<RoadNode> route,
            List<RoadEdge> edges) {

        double total = 0;

        for (int i = 0; i < route.size() - 1; i++) {

            RoadNode from = route.get(i);
            RoadNode to = route.get(i + 1);

            RoadEdge edge = findEdge(
                    from,
                    to,
                    edges);

            if (edge != null) {
                total += edge.getTravelTimeMinutes();
            }
        }

        return total;
    }

    private double calculateDistance(
            List<RoadNode> route,
            List<RoadEdge> edges) {

        double total = 0;

        for (int i = 0; i < route.size() - 1; i++) {

            RoadNode from = route.get(i);
            RoadNode to = route.get(i + 1);

            RoadEdge edge = findEdge(
                    from,
                    to,
                    edges);

            if (edge != null) {
                total += edge.getDistanceKm();
            }
        }

        return total;
    }

    private RoadEdge findEdge(
            RoadNode from,
            RoadNode to,
            List<RoadEdge> edges) {

        for (RoadEdge edge : edges) {

            if (edge.getFromNode().getId().equals(from.getId())
                    && edge.getToNode().getId().equals(to.getId())) {

                return edge;
            }
        }

        return null;
    }
}