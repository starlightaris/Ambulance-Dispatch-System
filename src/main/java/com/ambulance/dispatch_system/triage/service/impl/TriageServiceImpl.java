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
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Restores unresolved assessments into the in-memory priority queue
     * when the application starts.
     */
    @PostConstruct
    public void initQueue() {
        dispatchQueue.clear();

        // Load active unresolved assessments into the priority queue on startup.
        List<TriageAssessment> activeAssessments =
                assessmentRepository.findActiveQueue();

        for (TriageAssessment assessment : activeAssessments) {
            dispatchQueue.insert(assessment);
        }
    }

    @Override
    @Transactional
    public TriageResponseDTO evaluate(TriageRequestDTO request) {

        // 1. Determine the patient's triage category using the MTS decision tree.
        TriageCategory category = mtsDecisionTree.evaluate(request);

        // 2. Calculate the weighted score used to break ties within a category.
        double score = scoringStrategy.calculateScore(request);

        // 3. Convert the request DTO into a persistent assessment entity.
        TriageAssessment assessment = createAssessment(request, category, score);

        TriageAssessment savedAssessment = assessmentRepository.save(assessment);

        // 4. Add the saved assessment to the priority dispatch queue.
        dispatchQueue.insert(savedAssessment);

        // Determine the patient's current position in the queue.
        int position = dispatchQueue.getRank(savedAssessment);

        return new TriageResponseDTO(
                savedAssessment.getAssignedCategory(),
                savedAssessment.getTieBreakerScore(),
                position,
                savedAssessment.getCreatedAt()
        );
    }

    /**
     * Creates a triage assessment entity from the incoming request.
     *
     * @param request patient assessment data
     * @param category category calculated by the decision tree
     * @param score weighted tie-breaker score
     * @return populated triage assessment entity
     */
    private TriageAssessment createAssessment(
            TriageRequestDTO request,
            TriageCategory category,
            double score) {

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

        return assessment;
    }

    @Override
    public List<TriageResponseDTO> getActiveQueue() {

        // Fetch a non-destructive snapshot from the priority queue.
        List<TriageAssessment> activeAssessments = dispatchQueue.getSnapshot();

        int position = 1;
        List<TriageResponseDTO> response = new ArrayList<>();

        for (TriageAssessment assessment : activeAssessments) {
            response.add(new TriageResponseDTO(
                    assessment.getAssignedCategory(),
                    assessment.getTieBreakerScore(),
                    position++,
                    assessment.getCreatedAt()
            ));
        }

        return response;
    }

    @Override
    @Transactional
    public void markResolved(java.util.UUID id) {

        TriageAssessment assessment =
                assessmentRepository.findById(id).orElseThrow();

        assessment.setResolved(true);
        assessmentRepository.save(assessment);

        // KNOWN LIMITATION (For Report Chapter 6):
        // The dispatchQueue is an in-memory data structure. If the transaction
        // fails and rolls back (e.g. during JPA flush at method exit), the
        // database will correctly rollback 'resolved = false', but the heap
        // will have already processed 'remove(id)' successfully. The heap and
        // DB will be out of sync.
        //
        // A robust solution would involve a transaction synchronization
        // callback (TransactionSynchronizationManager) to only remove the
        // assessment from the heap after a successful transaction commit.
        dispatchQueue.remove(id);
    }
}