package com.ambulance.dispatch_system.resource_allocation.optimization;

import com.ambulance.dispatch_system.common.entity.Ambulance;
import com.ambulance.dispatch_system.common.entity.RoadEdge;
import com.ambulance.dispatch_system.common.entity.RoadNode;
import com.ambulance.dispatch_system.common.entity.enums.MedicalEquipment;
import com.ambulance.dispatch_system.network_detection.optimization.DijkstraBlindSpotOptimizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Exercises FitnessEvaluator against a real road graph and a real Dijkstra run,
 * rather than a stubbed travel time. Issue #9 was about the distance being
 * fabricated, so these cases only mean something if nothing is mocked out.
 *
 * Graph used throughout (travel time in minutes):
 *
 *     NodeA --3.0--> NodeB --4.0--> NodeC        NodeD (no edges)
 *       \-----------1.0 (blocked)-----^
 *
 * So the only usable NodeA -> NodeC route costs 7.0.
 */
class FitnessEvaluatorRoutingTest {

    private static final double DELTA = 1e-9;
    private static final Set<MedicalEquipment> NEEDS_ECG = Set.of(MedicalEquipment.ECG_MONITOR);

    private RoadNode nodeA, nodeB, nodeC, nodeD;
    private List<RoadNode> allNodes;
    private List<RoadEdge> allEdges;
    private FitnessEvaluator evaluator;

    @BeforeEach
    void setUp() {
        nodeA = node(1L, "NodeA");
        nodeB = node(2L, "NodeB");
        nodeC = node(3L, "NodeC");
        nodeD = node(4L, "NodeD");
        allNodes = List.of(nodeA, nodeB, nodeC, nodeD);
        allEdges = List.of(
                edge(nodeA, nodeB, 3.0, false),
                edge(nodeB, nodeC, 4.0, false),
                edge(nodeA, nodeC, 1.0, true));
        evaluator = new FitnessEvaluator(new DijkstraBlindSpotOptimizer());
    }

    @Test
    void usesTheRealShortestPathThroughTheRoadGraph() {
        double score = evaluator.calculateFitness(
                ambulance("AMB-01", "NodeA", NEEDS_ECG), "NodeC", NEEDS_ECG, allNodes, allEdges);

        // 3.0 (A->B) + 4.0 (B->C), not the blocked 1.0 shortcut and not a random number.
        assertEquals(7.0, score, DELTA);
    }

    @Test
    void takesTheDirectRouteOnceItIsNoLongerBlocked() {
        List<RoadEdge> unblocked = List.of(
                edge(nodeA, nodeB, 3.0, false),
                edge(nodeB, nodeC, 4.0, false),
                edge(nodeA, nodeC, 1.0, false));

        double score = evaluator.calculateFitness(
                ambulance("AMB-01", "NodeA", NEEDS_ECG), "NodeC", NEEDS_ECG, allNodes, unblocked);

        assertEquals(1.0, score, DELTA);
    }

    @Test
    void addsTheEquipmentPenaltyOnTopOfRealTravelTime() {
        Ambulance overEquipped = ambulance("AMB-02", "NodeA",
                Set.of(MedicalEquipment.ECG_MONITOR, MedicalEquipment.DEFIBRILLATOR));

        double score = evaluator.calculateFitness(overEquipped, "NodeC", NEEDS_ECG, allNodes, allEdges);

        // 7.0 travel + one unused item * 5.0
        assertEquals(12.0, score, DELTA);
    }

    @Test
    void anAmbulanceAlreadyAtThePatientCostsNothing() {
        double score = evaluator.calculateFitness(
                ambulance("AMB-03", "NodeC", NEEDS_ECG), "NodeC", NEEDS_ECG, allNodes, allEdges);

        assertEquals(0.0, score, DELTA);
    }

    @Test
    void ambulanceWithNoKnownLocationIsUnreachable() {
        double score = evaluator.calculateFitness(
                ambulance("AMB-GHOST", null, NEEDS_ECG), "NodeC", NEEDS_ECG, allNodes, allEdges);

        assertEquals(FitnessEvaluator.UNREACHABLE, score,
                "a vehicle with no recorded position must not score as if it were already there");
    }

    @Test
    void callWithNoLocationIsUnreachable() {
        double score = evaluator.calculateFitness(
                ambulance("AMB-01", "NodeA", NEEDS_ECG), null, NEEDS_ECG, allNodes, allEdges);

        assertEquals(FitnessEvaluator.UNREACHABLE, score);
    }

    @Test
    void unknownPatientNodeIsUnreachable() {
        double score = evaluator.calculateFitness(
                ambulance("AMB-01", "NodeA", NEEDS_ECG), "NoSuchNode", NEEDS_ECG, allNodes, allEdges);

        assertEquals(FitnessEvaluator.UNREACHABLE, score);
    }

    @Test
    void patientOnADisconnectedNodeIsUnreachable() {
        double score = evaluator.calculateFitness(
                ambulance("AMB-01", "NodeA", NEEDS_ECG), "NodeD", NEEDS_ECG, allNodes, allEdges);

        assertEquals(FitnessEvaluator.UNREACHABLE, score);
    }

    private RoadNode node(Long id, String name) {
        RoadNode n = new RoadNode();
        n.setId(id);
        n.setName(name);
        return n;
    }

    private RoadEdge edge(RoadNode from, RoadNode to, double minutes, boolean blocked) {
        RoadEdge e = new RoadEdge();
        e.setFromNode(from);
        e.setToNode(to);
        e.setTravelTimeMinutes(minutes);
        e.setBlocked(blocked);
        return e;
    }

    private Ambulance ambulance(String vehicleNumber, String location, Set<MedicalEquipment> equipment) {
        Ambulance a = new Ambulance();
        a.setVehicleNumber(vehicleNumber);
        a.setCurrentLocationNode(location);
        a.setEquipment(equipment);
        return a;
    }
}
