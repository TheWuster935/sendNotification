package com.example.sendnotification;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping("/api/flight")
    public Map<String, Object> getFlightData(@RequestParam(defaultValue = "WN2026") String flight) {
        return flightService.getFlightData(flight);
    }
}
