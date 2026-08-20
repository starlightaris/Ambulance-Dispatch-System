package com.ambulance.dispatch_system.network_detection.optimization;

import com.ambulance.dispatch_system.common.entity.RoadEdge;
import com.ambulance.dispatch_system.common.entity.RoadNode;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DijkstraBlindSpotOptimizer {

    public List<RoadNode> computeBlindSpots(List<RoadNode> allNodes, List<RoadEdge> allEdges, double thresholdMinutes) {
        Map<Long, List<RoadEdge>> adjList = new HashMap<>();
        for (RoadEdge edge : allEdges) {
            // Skip blocked roads
            if (!edge.isBlocked() && edge.getFromNode() != null) {
                adjList.computeIfAbsent(edge.getFromNode().getId(), k -> new ArrayList<>()).add(edge);
            }
        }

        Map<Long, Double> minTravelTimes = new HashMap<>();
        for (RoadNode node : allNodes) {
            minTravelTimes.put(node.getId(), Double.MAX_VALUE);
        }

        PriorityQueue<NodeDistancePair> pq = new PriorityQueue<>(Comparator.comparingDouble(p -> p.distance));

        for (RoadNode node : allNodes) {
            // Check if the node is an ambulance base/station by name
            if (isAmbulanceBase(node)) {
                minTravelTimes.put(node.getId(), 0.0);
                pq.add(new NodeDistancePair(node.getId(), 0.0));
            }
        }

        while (!pq.isEmpty()) {
            NodeDistancePair curr = pq.poll();
            if (curr.distance > minTravelTimes.get(curr.nodeId)) continue;

            for (RoadEdge edge : adjList.getOrDefault(curr.nodeId, Collections.emptyList())) {
                double newDist = curr.distance + edge.getTravelTimeMinutes();

                if (edge.getToNode() != null) {
                    Long neighborId = edge.getToNode().getId();

                    if (newDist < minTravelTimes.getOrDefault(neighborId, Double.MAX_VALUE)) {
                        minTravelTimes.put(neighborId, newDist);
                        pq.add(new NodeDistancePair(neighborId, newDist));
                    }
                }
            }
        }

        List<RoadNode> blindSpots = new ArrayList<>();
        for (RoadNode node : allNodes) {
            if (minTravelTimes.get(node.getId()) > thresholdMinutes) {
                blindSpots.add(node);
            }
        }

        return blindSpots;
    }

    private boolean isAmbulanceBase(RoadNode node) {
        if (node.getName() == null) return false;
        String name = node.getName().toLowerCase();
        return name.contains("base") || name.contains("station") || name.contains("hospital") || name.contains("depot");
    }

    private record NodeDistancePair(Long nodeId, double distance) {}
}