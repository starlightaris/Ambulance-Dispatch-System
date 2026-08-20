package com.ambulance.dispatch_system.resource_allocation.service;

import com.ambulance.dispatch_system.common.entity.Ambulance;
import com.ambulance.dispatch_system.common.entity.Call;
import com.ambulance.dispatch_system.common.entity.enums.AmbulanceStatus;
import com.ambulance.dispatch_system.common.entity.enums.CallStatus;
import com.ambulance.dispatch_system.common.repository.AmbulanceRepository;
import com.ambulance.dispatch_system.common.repository.CallRepository;
import com.ambulance.dispatch_system.optimization.greedy.GreedyAllocatorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class DispatchService {

    private final CallRepository callRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final GreedyAllocatorService greedyAllocator;

    public DispatchService(CallRepository callRepository, AmbulanceRepository ambulanceRepository, GreedyAllocatorService greedyAllocator) {
        this.callRepository = callRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.greedyAllocator = greedyAllocator;
    }

    @Transactional
    public String handleEmergencyDispatch(Long callId) {
        // 1. Find the emergency call
        Call call = callRepository.findById(callId)
                .orElseThrow(() -> new RuntimeException("Call not found"));

        // 2. Find the best ambulance using our Greedy algorithm
        Optional<Ambulance> bestAmbulanceOpt = greedyAllocator.allocateBestAmbulance(
                call.getLocationNode(), 
                call.getRequiredEquipment()
        );

        if (bestAmbulanceOpt.isEmpty()) {
            return "No suitable ambulance available at this time.";
        }

        // 3. Assign and update statuses
        Ambulance bestAmbulance = bestAmbulanceOpt.get();
        
        call.setAssignedAmbulance(bestAmbulance);
        call.setStatus(CallStatus.DISPATCHED); // Assuming you have this status
        
        bestAmbulance.setStatus(AmbulanceStatus.BUSY); // Assuming you have this status

        // 4. Save changes to database
        ambulanceRepository.save(bestAmbulance);
        callRepository.save(call);

        return "Ambulance " + bestAmbulance.getVehicleNumber() + " dispatched successfully.";
    }
}