package com.ambulance.dispatch_system.network_detection.service;

import com.ambulance.dispatch_system.common.entity.RoadNode;
import com.ambulance.dispatch_system.common.repository.RoadEdgeRepository;
import com.ambulance.dispatch_system.common.repository.RoadNodeRepository;
import com.ambulance.dispatch_system.network_detection.optimization.DijkstraBlindSpotOptimizer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NetworkAnalysisService {

    private final RoadNodeRepository nodeRepository;
    private final RoadEdgeRepository edgeRepository;
    private final DijkstraBlindSpotOptimizer blindSpotOptimizer;

    public NetworkAnalysisService(RoadNodeRepository nodeRepository,
                                  RoadEdgeRepository edgeRepository,
                                  DijkstraBlindSpotOptimizer blindSpotOptimizer) {
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.blindSpotOptimizer = blindSpotOptimizer;
    }

    public List<RoadNode> findBlindSpots(double thresholdMinutes) {
        var allNodes = nodeRepository.findAll();
        var allEdges = edgeRepository.findAll();
        return blindSpotOptimizer.computeBlindSpots(allNodes, allEdges, thresholdMinutes);
    }
}