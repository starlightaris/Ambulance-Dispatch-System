package com.ambulance.dispatch_system.triage.service;

import com.ambulance.dispatch_system.triage.model.dto.TriageRequestDTO;
import com.ambulance.dispatch_system.triage.model.dto.TriageResponseDTO;

import java.util.List;

public interface TriageService {
    
    /**
     * Evaluates a triage request, assigns a category and score, 
     * persists the assessment, and places it in the priority queue.
     * 
     * @param request The patient's physiological parameters.
     * @return TriageResponseDTO containing the category, score, and queue position.
     */
    TriageResponseDTO evaluate(TriageRequestDTO request);

    /**
     * Retrieves the current active dispatch queue ordered by priority.
     * 
     * @return List of TriageResponseDTOs representing the queue.
     */
    List<TriageResponseDTO> getActiveQueue();
}
