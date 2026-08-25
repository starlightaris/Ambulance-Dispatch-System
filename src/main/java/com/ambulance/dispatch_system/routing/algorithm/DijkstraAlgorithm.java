package com.ambulance.dispatch_system.routing.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.springframework.stereotype.Component;

import com.ambulance.dispatch_system.common.entity.RoadEdge;
import com.ambulance.dispatch_system.common.entity.RoadNode;

@Component
public class DijkstraAlgorithm {

    /**
     * Single-source Dijkstra between two nodes to calculate shortest travel time.
     *
     * @return travel time in minutes, 0.0 if start == end, or Double.MAX_VALUE if
     *         either node is null or no path exists.
     */
    public double calculateShortestTravelTime(RoadNode startNode, RoadNode endNode, List<RoadEdge> allEdges) {
        if (startNode == null || endNode == null || startNode.getId() == null || endNode.getId() == null) {
            return Double.MAX_VALUE;
        }
        if (startNode.getId().equals(endNode.getId())) {
            return 0.0;
        }

        Map<Long, List<RoadEdge>> adjList = new HashMap<>();
        if (allEdges != null) {
            for (RoadEdge edge : allEdges) {
                if (edge != null && !edge.isBlocked() && edge.getFromNode() != null && edge.getFromNode().getId() != null) {
                    adjList.computeIfAbsent(edge.getFromNode().getId(), k -> new ArrayList<>()).add(edge);
                }
            }
        }

        Map<Long, Double> minTravelTimes = new HashMap<>();
        PriorityQueue<NodeDistancePair> pq = new PriorityQueue<>(Comparator.comparingDouble(p -> p.distance()));
        
        minTravelTimes.put(startNode.getId(), 0.0);
        pq.add(new NodeDistancePair(startNode.getId(), 0.0));

        while (!pq.isEmpty()) {
            NodeDistancePair curr = pq.poll();

            if (curr.nodeId().equals(endNode.getId())) {
                return curr.distance();
            }

            Double currentTime = minTravelTimes.get(curr.nodeId());
            if (currentTime == null || curr.distance() > currentTime) continue;

            for (RoadEdge edge : adjList.getOrDefault(curr.nodeId(), Collections.emptyList())) {
                if (edge.getToNode() != null && edge.getToNode().getId() != null) {
                    Long neighborId = edge.getToNode().getId();
                    double newDist = curr.distance() + edge.getTravelTimeMinutes();
                    
                    if (newDist < minTravelTimes.getOrDefault(neighborId, Double.MAX_VALUE)) {
                        minTravelTimes.put(neighborId, newDist);
                        pq.add(new NodeDistancePair(neighborId, newDist));
                    }
                }
            }
        }

        return Double.MAX_VALUE;
    }

    private record NodeDistancePair(Long nodeId, double distance) {}
}
