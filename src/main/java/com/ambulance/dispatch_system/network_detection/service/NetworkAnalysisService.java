package com.ambulance.dispatch_system.network_detection.service;

import com.ambulance.dispatch_system.common.entity.RoadNode;
import com.ambulance.dispatch_system.common.entity.enums.AmbulanceStatus;
import com.ambulance.dispatch_system.common.repository.AmbulanceRepository;
import com.ambulance.dispatch_system.common.repository.RoadEdgeRepository;
import com.ambulance.dispatch_system.common.repository.RoadNodeRepository;
import com.ambulance.dispatch_system.network_detection.optimization.DijkstraBlindSpotOptimizer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NetworkAnalysisService {

    private final RoadNodeRepository nodeRepository;
    private final RoadEdgeRepository edgeRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final DijkstraBlindSpotOptimizer blindSpotOptimizer;

    public NetworkAnalysisService(RoadNodeRepository nodeRepository,
                                  RoadEdgeRepository edgeRepository,
                                  AmbulanceRepository ambulanceRepository,
                                  DijkstraBlindSpotOptimizer blindSpotOptimizer) {
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.blindSpotOptimizer = blindSpotOptimizer;
    }

    public List<RoadNode> findBlindSpots(double thresholdMinutes) {
        var allNodes = nodeRepository.findAll();
        var allEdges = edgeRepository.findAll();

        Set<String> baseNodeNames = ambulanceRepository.findAll().stream()
                .filter(amb -> amb.getStatus() == AmbulanceStatus.AVAILABLE)
                .map(amb -> amb.getCurrentLocationNode())
                .filter(nodeName -> nodeName != null && !nodeName.isBlank())
                .collect(Collectors.toSet());

        return blindSpotOptimizer.computeBlindSpots(allNodes, allEdges, baseNodeNames, thresholdMinutes);
    }
}