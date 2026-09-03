package com.ambulance.dispatch_system.routing.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ambulance.dispatch_system.routing.dto.RouteRequest;
import com.ambulance.dispatch_system.routing.dto.RouteResponse;
import com.ambulance.dispatch_system.routing.service.RouteService;

@RestController
@RequestMapping("/api/v1/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @PostMapping
    public ResponseEntity<RouteResponse> findRoute(
            @RequestBody RouteRequest request) {

        RouteResponse response = routeService.findRoute(request);

        return ResponseEntity.ok(response);
    }
}
