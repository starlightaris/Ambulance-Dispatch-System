package com.ambulance.dispatch_system.resource_allocation.controller;

import com.ambulance.dispatch_system.common.entity.Ambulance;
import com.ambulance.dispatch_system.common.entity.Call;
import com.ambulance.dispatch_system.resource_allocation.exception.CallNotFoundException;
import com.ambulance.dispatch_system.resource_allocation.service.DispatchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DispatchControllerTest {

    @Mock
    private DispatchService dispatchService;

    @InjectMocks
    private DispatchController dispatchController;

    @Test
    void allocateAmbulance_Success_ReturnsOk() {
        Long callId = 1L;
        String successMessage = "Ambulance AMB-001 dispatched successfully.";
        when(dispatchService.handleEmergencyDispatch(callId)).thenReturn(successMessage);

        ResponseEntity<String> response = dispatchController.allocateAmbulance(callId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(successMessage, response.getBody());
    }

    @Test
    void allocateAmbulance_CallNotFound_PropagatesToGlobalExceptionHandler() {
        Long callId = 1L;
        when(dispatchService.handleEmergencyDispatch(callId)).thenThrow(new CallNotFoundException(callId));

        // The controller no longer catches this itself - GlobalExceptionHandler maps
        // CallNotFoundException (a BaseException) to the 404 response.
        assertThrows(CallNotFoundException.class, () -> dispatchController.allocateAmbulance(callId));
    }

    @Test
    void getPendingEmergencies_ReturnsList() {
        List<Call> pendingCalls = List.of(new Call());
        when(dispatchService.getPendingCalls()).thenReturn(pendingCalls);

        ResponseEntity<?> response = dispatchController.getPendingEmergencies();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pendingCalls, response.getBody());
    }

    @Test
    void getAmbulances_ReturnsList() {
        List<Ambulance> ambulances = List.of(new Ambulance());
        when(dispatchService.getAllAmbulances()).thenReturn(ambulances);

        ResponseEntity<?> response = dispatchController.getAmbulances();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ambulances, response.getBody());
    }
}
