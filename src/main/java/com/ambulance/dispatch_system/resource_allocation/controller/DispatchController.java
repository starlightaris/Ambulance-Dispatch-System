package com.ambulance.dispatch_system.resource_allocation.controller;

import com.ambulance.dispatch_system.resource_allocation.service.DispatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/calls")
public class DispatchController {

    private final DispatchService dispatchService;

    public DispatchController(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    /**
     * Endpoint to trigger the resource allocation for a specific emergency call.
     * URL Example: POST http://localhost:8080/api/v1/calls/1/dispatch
     *
     * Errors (e.g. an unknown call id) are handled centrally by GlobalExceptionHandler.
     */
    @PostMapping("/{id}/dispatch")
    public ResponseEntity<String> allocateAmbulance(@PathVariable Long id) {
        String resultMessage = dispatchService.handleEmergencyDispatch(id);
        return ResponseEntity.ok(resultMessage);
    }
}