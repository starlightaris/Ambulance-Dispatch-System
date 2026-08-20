package com.ambulance.dispatch_system.resource_allocation.optimization;

import com.ambulance.dispatch_system.common.entity.Ambulance;
import com.ambulance.dispatch_system.common.entity.enums.AmbulanceStatus;
import com.ambulance.dispatch_system.common.entity.enums.MedicalEquipment;
import com.ambulance.dispatch_system.common.repository.AmbulanceRepository;
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
        List<Ambulance> availableAmbulances = ambulanceRepository.findByStatus(AmbulanceStatus.AVAILABLE);

        return availableAmbulances.stream()
                .filter(amb -> amb.getEquipment().containsAll(requiredEquipment))
                .min(Comparator.comparingDouble(amb -> 
                        fitnessEvaluator.calculateFitness(amb, patientNode, requiredEquipment)));
    }
}