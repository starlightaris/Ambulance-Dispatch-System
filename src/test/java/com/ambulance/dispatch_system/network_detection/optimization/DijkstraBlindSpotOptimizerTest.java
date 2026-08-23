package com.ambulance.dispatch_system.network_detection.optimization;

import com.ambulance.dispatch_system.common.entity.RoadEdge;
import com.ambulance.dispatch_system.common.entity.RoadNode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies DijkstraBlindSpotOptimizer against a small, hand-traceable
 * graph so results can be checked by manual calculation for the
 * Chapter 8 (Experimental Performance Evaluation) writeup.
 *
 * Graph used:
 *
 *   Station --5--> Mid --20--> Far
 *
 * With "Station" as the only ambulance base:
 *   distance(Station) = 0
 *   distance(Mid)     = 5
 *   distance(Far)     = 25
 */
class DijkstraBlindSpotOptimizerTest {

    private RoadNode node(long id, String name) {
        RoadNode n = new RoadNode();
        n.setId(id);
        n.setName(name);
        n.setLatitude(0.0);
        n.setLongitude(0.0);
        return n;
    }

    private RoadEdge edge(RoadNode from, RoadNode to, double minutes) {
        RoadEdge e = new RoadEdge();
        e.setId(from.getId() * 100 + to.getId());
        e.setFromNode(from);
        e.setToNode(to);
        e.setDistanceKm(minutes / 2.0); // arbitrary, not used by the algorithm
        e.setTravelTimeMinutes(minutes);
        e.setBlocked(false);
        return e;
    }

    @Test
    void nodeBeyondThresholdIsFlaggedAsBlindSpot() {
        RoadNode station = node(1L, "Station");
        RoadNode mid = node(2L, "Mid");
        RoadNode far = node(3L, "Far");

        RoadEdge stationToMid = edge(station, mid, 5.0);
        RoadEdge midToFar = edge(mid, far, 20.0);

        var optimizer = new DijkstraBlindSpotOptimizer();
        List<RoadNode> blindSpots = optimizer.computeBlindSpots(
                List.of(station, mid, far),
                List.of(stationToMid, midToFar),
                Set.of("Station"),
                15.0 // threshold minutes
        );

        // Station = 0min, Mid = 5min -> both within 15min threshold
        // Far = 25min -> exceeds threshold, must be flagged
        assertEquals(1, blindSpots.size());
        assertEquals("Far", blindSpots.get(0).getName());
    }

    @Test
    void raisingThresholdRemovesBlindSpot() {
        RoadNode station = node(1L, "Station");
        RoadNode mid = node(2L, "Mid");
        RoadNode far = node(3L, "Far");

        RoadEdge stationToMid = edge(station, mid, 5.0);
        RoadEdge midToFar = edge(mid, far, 20.0);

        var optimizer = new DijkstraBlindSpotOptimizer();
        List<RoadNode> blindSpots = optimizer.computeBlindSpots(
                List.of(station, mid, far),
                List.of(stationToMid, midToFar),
                Set.of("Station"),
                30.0 // threshold raised above 25min
        );

        assertTrue(blindSpots.isEmpty());
    }

    @Test
    void blockedEdgeIsExcludedFromRouting() {
        RoadNode station = node(1L, "Station");
        RoadNode mid = node(2L, "Mid");

        RoadEdge blockedEdge = edge(station, mid, 5.0);
        blockedEdge.setBlocked(true);

        var optimizer = new DijkstraBlindSpotOptimizer();
        List<RoadNode> blindSpots = optimizer.computeBlindSpots(
                List.of(station, mid),
                List.of(blockedEdge),
                Set.of("Station"),
                10.0
        );

        // Mid is unreachable because the only edge to it is blocked,
        // so it must be reported as a blind spot regardless of threshold.
        assertEquals(1, blindSpots.size());
        assertEquals("Mid", blindSpots.get(0).getName());
    }
}