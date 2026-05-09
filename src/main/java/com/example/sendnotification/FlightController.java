package com.example.sendnotification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
public class FlightController {

    @Value("${api.gateway.url}")
    private String apiGatewayUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    @GetMapping("/api/flight")
    public Map<String, Object> getFlightData(@RequestParam(defaultValue = "WN2026") String flight) {
        String url = apiGatewayUrl + "/flight?flight=" + flight;
        return restTemplate.getForObject(url, Map.class);
    }
}
