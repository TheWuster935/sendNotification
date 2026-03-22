package com.example.sendnotification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class FlightService {

    @Value("${aviationstack.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public Map<String, Object> getFlightData(String flightIata) {
        if (apiKey == null || apiKey.isBlank()) {
            return Map.of("error", "Aviation Stack API key is not configured. Set 'aviationstack.api.key' in application.properties.");
        }

        String url = "http://api.aviationstack.com/v1/flights?access_key=" + apiKey + "&flight_iata=" + flightIata;

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) {
                return Map.of("error", "No response from Aviation Stack API.");
            }

            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
            if (data == null || data.isEmpty()) {
                return Map.of("error", "No flight data found for " + flightIata + ".");
            }

            return data.get(0);
        } catch (Exception e) {
            return Map.of("error", "API call failed: " + e.getMessage());
        }
    }
}
