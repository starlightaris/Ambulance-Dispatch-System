package com.ambulance.dispatch_system.network_detection.service;

import com.ambulance.dispatch_system.common.repository.RoadNodeRepository;
import com.ambulance.dispatch_system.network_detection.dto.CoverageStatsDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds a coverage-vs-threshold curve by re-running the teammate's
 * existing blind-spot analysis (NetworkAnalysisService) at several
 * threshold values. This reuses his algorithm as-is via composition —
 * his file is never modified, only called.
 */
@Service
public class NetworkCoverageStatsService {

    private final NetworkAnalysisService analysisService;
    private final RoadNodeRepository nodeRepository;

    public NetworkCoverageStatsService(NetworkAnalysisService analysisService,
                                        RoadNodeRepository nodeRepository) {
        this.analysisService = analysisService;
        this.nodeRepository = nodeRepository;
    }

    public List<CoverageStatsDto> getCoverageCurve(List<Double> thresholds) {
        long totalNodes = nodeRepository.count();

        return thresholds.stream()
                .map(threshold -> {
                    int blindCount = analysisService.findBlindSpots(threshold).size();
                    double coveragePct = totalNodes > 0
                            ? ((totalNodes - blindCount) / (double) totalNodes) * 100.0
                            : 0.0;
                    return new CoverageStatsDto(threshold, (int) totalNodes, blindCount, coveragePct);
                })
                .collect(Collectors.toList());
    }
}