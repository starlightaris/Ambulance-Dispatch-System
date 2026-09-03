package com.ambulance.dispatch_system.resource_allocation.controller;

import com.ambulance.dispatch_system.resource_allocation.service.DispatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dispatch")
public class DispatchController {

    private final DispatchService dispatchService;

    // This connects your controller to your service
    public DispatchController(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    /**
     * Endpoint to trigger the resource allocation for a specific emergency call.
     * URL Example: POST http://localhost:8080/api/dispatch/allocate/1
     */
    @PostMapping("/allocate/{callId}")
    public ResponseEntity<String> allocateAmbulance(@PathVariable Long callId) {
        try {
            // Run the algorithm and get the success or failure message
            String resultMessage = dispatchService.handleEmergencyDispatch(callId);

            // Send a "200 OK" response back to the user with the message
            return ResponseEntity.ok(resultMessage);

        } catch (RuntimeException e) {
            // If the callId is not found, send a "400 Bad Request" error
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Fetch pending emergencies
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingEmergencies() {
        return ResponseEntity.ok(dispatchService.getPendingCalls());
    }

    // Fetch ambulances
    @GetMapping("/ambulances")
    public ResponseEntity<?> getAmbulances() {
        return ResponseEntity.ok(dispatchService.getAllAmbulances());
    }
}