package com.ambulance.dispatch_system.resource_allocation.optimization;

import com.ambulance.dispatch_system.common.entity.Ambulance;
import com.ambulance.dispatch_system.common.entity.enums.AmbulanceStatus;
import com.ambulance.dispatch_system.common.entity.enums.MedicalEquipment; // Adjust import if needed
import com.ambulance.dispatch_system.common.repository.AmbulanceRepository;
import com.ambulance.dispatch_system.optimization.fitness.FitnessEvaluator;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class GreedyScheduler {

    private final AmbulanceRepository ambulanceRepository;
    private final FitnessEvaluator fitnessEvaluator;

    public GreedyScheduler(AmbulanceRepository ambulanceRepository, FitnessEvaluator fitnessEvaluator) {
        this.ambulanceRepository = ambulanceRepository;
        this.fitnessEvaluator = fitnessEvaluator;
    }

    public Optional<Ambulance> findBestAmbulance(String patientNode, Set<MedicalEquipment> requiredEquipment) {
        
        // DATA STRUCTURE 1: 'List' is used to hold the database results
        List<Ambulance> availableAmbulances = ambulanceRepository.findByStatus(AmbulanceStatus.AVAILABLE);

        return availableAmbulances.stream()
                // DATA STRUCTURE 2: 'Set' is used inside containsAll() for very fast checking
                .filter(amb -> amb.getEquipment().containsAll(requiredEquipment))
                
                // DATA STRUCTURE 3: 'Stream min()' handles the sorting to instantly find the lowest score
                .min(Comparator.comparingDouble(amb -> 
                        fitnessEvaluator.calculateFitness(amb, patientNode, requiredEquipment)));
    }
}