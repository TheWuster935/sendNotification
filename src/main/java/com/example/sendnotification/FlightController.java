package com.example.sendnotification;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class FlightController {

    private final FlightService flightService;
    private final WebhookService webhookService;

    public FlightController(FlightService flightService, WebhookService webhookService) {
        this.flightService = flightService;
        this.webhookService = webhookService;
    }

    @GetMapping("/api/flight")
    public Map<String, Object> getFlightData(@RequestParam(defaultValue = "WN2026") String flight) {
        Map<String, Object> flightData = flightService.getFlightData(flight);
        webhookService.fireWebhook(flightData);
        return flightData;
    }
}
