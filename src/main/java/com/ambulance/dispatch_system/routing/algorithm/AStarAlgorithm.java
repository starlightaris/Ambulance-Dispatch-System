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
                                node -> fScore.getOrDefault(node.getId(), Double.MAX_VALUE)
                        )
                );

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

                double tentativeGScore =
                        gScore.getOrDefault(
                                current.getId(),
                                Double.MAX_VALUE
                        ) + edge.getTravelTimeMinutes();

                if (tentativeGScore <
                        gScore.getOrDefault(
                                neighbour.getId(),
                                Double.MAX_VALUE)) {

                    previous.put(neighbour.getId(), current);

                    gScore.put(
                            neighbour.getId(),
                            tentativeGScore
                    );

                    fScore.put(
                            neighbour.getId(),
                            tentativeGScore
                                    + heuristic(neighbour, destination)
                    );

                    openSet.add(neighbour);
                }
            }
        }

        return Collections.emptyList();
    }

    private Map<Long, List<RoadEdge>> buildGraph(
            List<RoadEdge> edges) {

        Map<Long, List<RoadEdge>> graph = new HashMap<>();

        for (RoadEdge edge : edges) {

            if (edge.isBlocked()) {
                continue;
            }

            graph.computeIfAbsent(
                    edge.getFromNode().getId(),
                    key -> new ArrayList<>()
            ).add(edge);
        }

        return graph;
    }

    private double heuristic(
            RoadNode current,
            RoadNode destination) {

        double latitudeDifference =
                current.getLatitude()
                        - destination.getLatitude();

        double longitudeDifference =
                current.getLongitude()
                        - destination.getLongitude();

        return Math.sqrt(
                latitudeDifference * latitudeDifference
                        + longitudeDifference * longitudeDifference
        );
    }

    private List<RoadNode> reconstructPath(
            Map<Long, RoadNode> previous,
            RoadNode current) {

        LinkedList<RoadNode> path = new LinkedList<>();

        while (current != null) {

            path.addFirst(current);

            current = previous.get(current.getId());
        }

        return path;
    }
}