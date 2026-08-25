package com.ambulance.dispatch_system.resource_allocation.optimization;

import com.ambulance.dispatch_system.common.entity.Ambulance;
import com.ambulance.dispatch_system.common.entity.RoadNode;
import com.ambulance.dispatch_system.common.entity.enums.AmbulanceStatus;
import com.ambulance.dispatch_system.common.entity.enums.MedicalEquipment;
import com.ambulance.dispatch_system.common.repository.AmbulanceRepository;
import com.ambulance.dispatch_system.common.repository.RoadNodeRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class GreedyScheduler {

    private final AmbulanceRepository ambulanceRepository;
    private final FitnessEvaluator fitnessEvaluator;
    private final RoadNodeRepository roadNodeRepository;

    public GreedyScheduler(AmbulanceRepository ambulanceRepository, FitnessEvaluator fitnessEvaluator,
                           RoadNodeRepository roadNodeRepository) {
        this.ambulanceRepository = ambulanceRepository;
        this.fitnessEvaluator = fitnessEvaluator;
        this.roadNodeRepository = roadNodeRepository;
    }

    public Optional<Ambulance> findBestAmbulance(String patientNode, Set<MedicalEquipment> requiredEquipment) {
        List<Ambulance> availableAmbulances = ambulanceRepository.findByStatus(AmbulanceStatus.AVAILABLE);

        // Fetch graph data only once to save database performance
        List<RoadNode> allNodes = roadNodeRepository.findAll();

        // Score each candidate exactly once. Comparing with Comparator.comparingDouble over
        // calculateFitness would re-run the shortest-path search on both sides of every
        // comparison, so a fleet of n ambulances cost roughly 2n graph traversals.
        return availableAmbulances.stream()
                .filter(amb -> amb.getEquipment().containsAll(requiredEquipment))
                .map(amb -> new ScoredAmbulance(amb, fitnessEvaluator.calculateFitness(
                        amb, patientNode, requiredEquipment, allNodes)))
                .filter(scored -> scored.score() < FitnessEvaluator.UNREACHABLE)
                .min(Comparator.comparingDouble(ScoredAmbulance::score))
                .map(ScoredAmbulance::ambulance);
    }

    /** An ambulance paired with its already-computed fitness score. */
    private record ScoredAmbulance(Ambulance ambulance, double score) {}
}