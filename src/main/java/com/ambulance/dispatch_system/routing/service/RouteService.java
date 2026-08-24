package com.ambulance.dispatch_system.routing.service;

import com.ambulance.dispatch_system.routing.dto.RouteRequest;
import com.ambulance.dispatch_system.routing.dto.RouteResponse;

public interface RouteService {

    RouteResponse findRoute(RouteRequest request);
}
