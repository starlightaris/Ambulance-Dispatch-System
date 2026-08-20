package com.ambulance.dispatch_system.resource_allocation.service;

import com.ambulance.dispatch_system.common.entity.Ambulance;
import com.ambulance.dispatch_system.common.entity.Call;
import com.ambulance.dispatch_system.common.entity.enums.AmbulanceStatus;
import com.ambulance.dispatch_system.common.entity.enums.CallStatus;
import com.ambulance.dispatch_system.common.repository.AmbulanceRepository;
import com.ambulance.dispatch_system.common.repository.CallRepository;
import com.ambulance.dispatch_system.resource_allocation.optimization.GreedyScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class DispatchService {

    private final CallRepository callRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final GreedyScheduler greedyScheduler;

    public DispatchService(CallRepository callRepository, 
                           AmbulanceRepository ambulanceRepository, 
                           GreedyScheduler greedyScheduler) {
        this.callRepository = callRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.greedyScheduler = greedyScheduler;
    }

    @Transactional
    public String handleEmergencyDispatch(Long callId) {
        Call call = callRepository.findById(callId)
                .orElseThrow(() -> new RuntimeException("Call not found with ID: " + callId));

        Optional<Ambulance> bestAmbulanceOpt = greedyScheduler.findBestAmbulance(
                call.getLocationNode(), 
                call.getRequiredEquipment()
        );

        if (bestAmbulanceOpt.isEmpty()) {
            return "No suitable ambulance available at this time.";
        }

        Ambulance bestAmbulance = bestAmbulanceOpt.get();
        
        call.setAssignedAmbulance(bestAmbulance);
        call.setStatus(CallStatus.DISPATCHED); 
        
        // Updated to use your new enum value
        bestAmbulance.setStatus(AmbulanceStatus.DISPATCHED);

        ambulanceRepository.save(bestAmbulance);
        callRepository.save(call);

        return "Ambulance " + bestAmbulance.getVehicleNumber() + " dispatched successfully.";
    }
}