package com.ambulance.dispatch_system.resource_allocation.optimization;

import com.ambulance.dispatch_system.common.entity.Ambulance;
import com.ambulance.dispatch_system.common.entity.RoadEdge;
import com.ambulance.dispatch_system.common.entity.RoadNode;
import com.ambulance.dispatch_system.common.entity.enums.MedicalEquipment;
import com.ambulance.dispatch_system.network_detection.optimization.DijkstraBlindSpotOptimizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class FitnessEvaluatorTest {

    private DijkstraBlindSpotOptimizer dijkstraOptimizer;
    private FitnessEvaluator fitnessEvaluator;

    @BeforeEach
    void setUp() {
        dijkstraOptimizer = Mockito.mock(DijkstraBlindSpotOptimizer.class);
        fitnessEvaluator = new FitnessEvaluator(dijkstraOptimizer);
    }

    @Test
    void testCalculateFitness_ZeroDistance_NoExtraEquipment() {
        Ambulance ambulance = new Ambulance();
        ambulance.setCurrentLocationNode("NodeA");
        ambulance.setEquipment(Set.of(MedicalEquipment.DEFIBRILLATOR));

        when(dijkstraOptimizer.calculateShortestTravelTime(any(), any(), any(), any())).thenReturn(0.0);

        double score = fitnessEvaluator.calculateFitness(ambulance, "NodeA", 
                Set.of(MedicalEquipment.DEFIBRILLATOR), Collections.emptyList(), Collections.emptyList());

        assertEquals(0.0, score);
    }

    @Test
    void testCalculateFitness_WithDistanceAndExtraEquipment() {
        Ambulance ambulance = new Ambulance();
        ambulance.setCurrentLocationNode("NodeA");
        ambulance.setEquipment(Set.of(MedicalEquipment.DEFIBRILLATOR, MedicalEquipment.VENTILATOR));

        when(dijkstraOptimizer.calculateShortestTravelTime(any(), any(), any(), any())).thenReturn(15.5);

        // 1 extra equipment = 5.0 penalty + 15.5 distance = 20.5
        double score = fitnessEvaluator.calculateFitness(ambulance, "NodeB", 
                Set.of(MedicalEquipment.DEFIBRILLATOR), Collections.emptyList(), Collections.emptyList());

        assertEquals(20.5, score);
    }
}
