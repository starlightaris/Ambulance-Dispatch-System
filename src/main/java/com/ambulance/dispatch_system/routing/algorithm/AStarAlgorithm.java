package com.ambulance.dispatch_system.routing.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import com.ambulance.dispatch_system.common.entity.RoadEdge;
import com.ambulance.dispatch_system.common.entity.RoadNode;

public class AStarAlgorithm {

    public List<RoadNode> findShortestPath(
            RoadNode start,
            RoadNode destination,
            List<RoadEdge> edges) {

        Map<Long, List<RoadEdge>> graph = buildGraph(edges);

        Map<Long, Double> gScore = new HashMap<>();
        Map<Long, Double> fScore = new HashMap<>();
        Map<Long, RoadNode> previous = new HashMap<>();

        PriorityQueue<RoadNode> openSet =
                new PriorityQueue<>(
                        Comparator.comparingDouble(
                                node -> fScore.getOrDefault(
                                        node.getId(),
                                        Double.MAX_VALUE)
                        )
                );

        // Starting point = 0 minutes travelled
        gScore.put(start.getId(), 0.0);

        fScore.put(
                start.getId(),
                heuristic(start, destination)
        );

        openSet.add(start);

        while (!openSet.isEmpty()) {

            RoadNode current = openSet.poll();

            if (current.getId().equals(destination.getId())) {
                return reconstructPath(previous, current);
            }

            for (RoadEdge edge : graph.getOrDefault(
                    current.getId(),
                    Collections.emptyList())) {

                RoadNode neighbour = edge.getToNode();

                // Actual travel time accumulated so far
                double tentativeGScore =
                        gScore.getOrDefault(
                                current.getId(),
                                Double.MAX_VALUE)
                                + edge.getTravelTimeMinutes();

                if (tentativeGScore <
                        gScore.getOrDefault(
                                neighbour.getId(),
                                Double.MAX_VALUE)) {

                    previous.put(
                            neighbour.getId(),
                            current);

                    gScore.put(
                            neighbour.getId(),
                            tentativeGScore);

                    // f(n) = g(n) + h(n)
                    fScore.put(
                            neighbour.getId(),
                            tentativeGScore
                                    + heuristic(
                                            neighbour,
                                            destination));

                    openSet.add(neighbour);
                }
            }
        }

        return Collections.emptyList();
    }

    private Map<Long, List<RoadEdge>> buildGraph(
            List<RoadEdge> edges) {

        Map<Long, List<RoadEdge>> graph =
                new HashMap<>();

        for (RoadEdge edge : edges) {

            // Ignore blocked roads
            if (edge.isBlocked()) {
                continue;
            }

            graph.computeIfAbsent(
                    edge.getFromNode().getId(),
                    key -> new ArrayList<>())
                    .add(edge);
        }

        return graph;
    }

    /*
     * Heuristic:
     * Estimates the remaining travel time (minutes)
     * using the Haversine distance formula.
     */
    private double heuristic(
            RoadNode current,
            RoadNode destination) {

        double earthRadiusKm = 6371.0;

        double lat1 = Math.toRadians(current.getLatitude());
        double lat2 = Math.toRadians(destination.getLatitude());

        double latitudeDifference =
                Math.toRadians(
                        destination.getLatitude()
                                - current.getLatitude());

        double longitudeDifference =
                Math.toRadians(
                        destination.getLongitude()
                                - current.getLongitude());

        double a =
                Math.sin(latitudeDifference / 2)
                        * Math.sin(latitudeDifference / 2)
                        + Math.cos(lat1)
                        * Math.cos(lat2)
                        * Math.sin(longitudeDifference / 2)
                        * Math.sin(longitudeDifference / 2);

        double c =
                2 * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1 - a));

        double distanceKm = earthRadiusKm * c;

        // Assume ambulance average speed = 40 km/h
        double averageSpeedKmPerHour = 40.0;

        // Return estimated travel time in minutes
        return (distanceKm / averageSpeedKmPerHour) * 60.0;
    }

    private List<RoadNode> reconstructPath(
            Map<Long, RoadNode> previous,
            RoadNode current) {

        LinkedList<RoadNode> path =
                new LinkedList<>();

        while (current != null) {

            path.addFirst(current);

            current = previous.get(current.getId());
        }

        return path;
    }
}