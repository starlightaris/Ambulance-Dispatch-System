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

        RoadNode start = roadNodeRepository
                .findById(request.getStartLocationId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Start location not found"));

        RoadNode destination = roadNodeRepository
                .findById(request.getDestinationLocationId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Destination location not found"));

        List<RoadEdge> availableEdges =
                roadEdgeRepository.findByBlockedFalse();

        String algorithm = request.getAlgorithm();

        if (algorithm == null || algorithm.isBlank()) {
            algorithm = "ASTAR";
        }

        if (!algorithm.equalsIgnoreCase("ASTAR")) {
            throw new IllegalArgumentException(
                    "Currently supported algorithm: ASTAR");
        }

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
                calculateTravelTime(route, availableEdges);

        double totalDistance =
                calculateDistance(route, availableEdges);

        return new RouteResponse(
                "ASTAR",
                totalTravelTime,
                totalDistance,
                route
        );
    }

    private double calculateTravelTime(
            List<RoadNode> route,
            List<RoadEdge> edges) {

        double total = 0;

        for (int i = 0; i < route.size() - 1; i++) {

            RoadNode from = route.get(i);
            RoadNode to = route.get(i + 1);

            RoadEdge edge = findEdge(from, to, edges);

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

            RoadEdge edge = findEdge(from, to, edges);

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