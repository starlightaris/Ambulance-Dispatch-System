package com.ambulance.dispatch_system.resource_allocation.optimization;

import com.ambulance.dispatch_system.common.entity.Ambulance;
import com.ambulance.dispatch_system.common.entity.enums.AmbulanceStatus;
import com.ambulance.dispatch_system.common.entity.enums.MedicalEquipment;
import com.ambulance.dispatch_system.common.repository.AmbulanceRepository;
import com.ambulance.dispatch_system.common.repository.RoadNodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class GreedySchedulerTest {

    private AmbulanceRepository ambulanceRepository;
    private FitnessEvaluator fitnessEvaluator;
    private RoadNodeRepository roadNodeRepository;
    private GreedyScheduler greedyScheduler;

    @BeforeEach
    void setUp() {
        // 1. Create mock (fake) versions of all 4 dependencies
        ambulanceRepository = Mockito.mock(AmbulanceRepository.class);
        fitnessEvaluator = Mockito.mock(FitnessEvaluator.class);
        roadNodeRepository = Mockito.mock(RoadNodeRepository.class);
        
        // 2. Inject all 3 into your scheduler
        greedyScheduler = new GreedyScheduler(
                ambulanceRepository, fitnessEvaluator, roadNodeRepository
        );
    }

    @Test
    void testFindBestAmbulance_Success() {
        // Setup Fake Ambulances
        Ambulance amb1 = new Ambulance();
        amb1.setVehicleNumber("AMB-01");
        amb1.setEquipment(Set.of(MedicalEquipment.ECG_MONITOR));

        Ambulance amb2 = new Ambulance();
        amb2.setVehicleNumber("AMB-02");
        amb2.setEquipment(Set.of(MedicalEquipment.ECG_MONITOR, MedicalEquipment.DEFIBRILLATOR));

        // Tell the mock databases what to return
        when(ambulanceRepository.findByStatus(AmbulanceStatus.AVAILABLE)).thenReturn(List.of(amb1, amb2));
        when(roadNodeRepository.findAll()).thenReturn(List.of()); // Fake empty map
        
        // Tell the mock evaluator to give AMB-01 a better score. 
        // Note: When using Mockito, we must use eq() and anyList() to handle the new arguments.
        when(fitnessEvaluator.calculateFitness(
                eq(amb1), eq("Node_A"), eq(Set.of(MedicalEquipment.ECG_MONITOR)), anyList())
        ).thenReturn(10.0);
        
        when(fitnessEvaluator.calculateFitness(
                eq(amb2), eq("Node_A"), eq(Set.of(MedicalEquipment.ECG_MONITOR)), anyList())
        ).thenReturn(25.0);

        // Run the method
        Optional<Ambulance> result = greedyScheduler.findBestAmbulance("Node_A", Set.of(MedicalEquipment.ECG_MONITOR));

        // Verify the result (AMB-01 should win)
        assertTrue(result.isPresent());
        assertEquals("AMB-01", result.get().getVehicleNumber());
    }
}