package com.ambulance.dispatch_system.triage.service.impl;

import com.ambulance.dispatch_system.triage.entity.TriageAssessment;
import com.ambulance.dispatch_system.triage.model.dto.TriageRequestDTO;
import com.ambulance.dispatch_system.triage.model.dto.TriageResponseDTO;
import com.ambulance.dispatch_system.triage.model.enums.TriageCategory;
import com.ambulance.dispatch_system.triage.repository.TriageAssessmentRepository;
import com.ambulance.dispatch_system.triage.service.TriageService;
import com.ambulance.dispatch_system.triage.service.impl.algorithms.MTSDecisionTree;
import com.ambulance.dispatch_system.triage.service.impl.algorithms.WeightedScoringStrategy;
import com.ambulance.dispatch_system.triage.util.PriorityDispatchQueue;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TriageServiceImpl implements TriageService {

    private final MTSDecisionTree mtsDecisionTree;
    private final WeightedScoringStrategy scoringStrategy;
    private final TriageAssessmentRepository assessmentRepository;
    private final PriorityDispatchQueue dispatchQueue;

    public TriageServiceImpl(MTSDecisionTree mtsDecisionTree,
                             WeightedScoringStrategy scoringStrategy,
                             TriageAssessmentRepository assessmentRepository) {
        this.mtsDecisionTree = mtsDecisionTree;
        this.scoringStrategy = scoringStrategy;
        this.assessmentRepository = assessmentRepository;
        this.dispatchQueue = new PriorityDispatchQueue();
    }

    @PostConstruct
    public void initQueue() {
        dispatchQueue.clear();
        // Load active unresolved assessments into the priority queue on startup
        List<TriageAssessment> activeAssessments = assessmentRepository.findActiveQueue();
        for (TriageAssessment assessment : activeAssessments) {
            dispatchQueue.insert(assessment);
        }
    }

    @Override
    @Transactional
    public TriageResponseDTO evaluate(TriageRequestDTO request) {
        // 1. Run MTS Decision Tree -> Category
        TriageCategory category = mtsDecisionTree.evaluate(request);

        // 2. Run Weighted Scoring Strategy -> Tiebreaker Score
        double score = scoringStrategy.calculateScore(request);

        // 3. Create and persist TriageAssessment entity
        TriageAssessment assessment = new TriageAssessment();
        assessment.setBreathing(request.getBreathing());
        assessment.setPulseRate(request.getPulseRate());
        assessment.setAvpu(request.getAvpu());
        assessment.setOxygenSaturation(request.getOxygenSaturation());
        assessment.setSystolicBP(request.getSystolicBP());
        assessment.setPainScore(request.getPainScore());
        assessment.setTemperature(request.getTemperature());
        assessment.setAge(request.getAge());
        assessment.setHazardPresent(request.getHazardPresent());
        assessment.setSymptoms(request.getSymptoms());
        
        assessment.setAssignedCategory(category);
        assessment.setTieBreakerScore(score);
        
        TriageAssessment savedAssessment = assessmentRepository.save(assessment);

        // 4. Push onto PriorityDispatchQueue
        dispatchQueue.insert(savedAssessment);

        // Calculate queue position based on actual strictly greater elements in the heap
        int position = dispatchQueue.getRank(savedAssessment);

        return new TriageResponseDTO(
                savedAssessment.getAssignedCategory(),
                savedAssessment.getTieBreakerScore(),
                position,
                savedAssessment.getCreatedAt()
        );
    }

    @Override
    public List<TriageResponseDTO> getActiveQueue() {
        // Fetch non-destructive snapshot from heap
        List<TriageAssessment> activeAssessments = dispatchQueue.getSnapshot();
        
        int position = 1;
        List<TriageResponseDTO> response = new java.util.ArrayList<>();
        for (TriageAssessment ta : activeAssessments) {
            response.add(new TriageResponseDTO(
                ta.getAssignedCategory(),
                ta.getTieBreakerScore(),
                position++,
                ta.getCreatedAt()
            ));
        }
        return response;
    }
    
    @Override
    @Transactional
    public void markResolved(java.util.UUID id) {
        TriageAssessment assessment = assessmentRepository.findById(id).orElseThrow();
        assessment.setResolved(true);
        assessmentRepository.save(assessment);
        
        // KNOWN LIMITATION (For Report Chapter 6):
        // The dispatchQueue is an in-memory data structure. If the transaction fails 
        // and rolls back (e.g. during JPA flush at method exit), the database will 
        // correctly rollback 'resolved = false', but the heap will have already 
        // processed 'remove(id)' successfully. The heap and DB will be out of sync.
        // A robust solution would involve a transaction synchronization callback 
        // (TransactionSynchronizationManager) to only remove from the heap after commit.
        dispatchQueue.remove(id);
    }
}
