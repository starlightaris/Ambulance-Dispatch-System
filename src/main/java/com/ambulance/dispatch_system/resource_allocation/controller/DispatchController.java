package com.ambulance.dispatch_system.resource_allocation.controller;

import com.ambulance.dispatch_system.resource_allocation.service.DispatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dispatch")
public class DispatchController {

    private final DispatchService dispatchService;

    public DispatchController(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    /**
     * Endpoint to trigger the resource allocation for a specific emergency call.
     * URL Example: POST http://localhost:8080/api/dispatch/allocate/1
     *
     * Errors (e.g. an unknown callId) are handled centrally by GlobalExceptionHandler.
     */
    @PostMapping("/allocate/{callId}")
    public ResponseEntity<String> allocateAmbulance(@PathVariable Long callId) {
        String resultMessage = dispatchService.handleEmergencyDispatch(callId);
        return ResponseEntity.ok(resultMessage);
    }
}