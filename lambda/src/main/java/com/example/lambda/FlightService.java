package com.example.lambda;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class FlightService {

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public FlightService(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getFlightData(String flightIata) {
        if (apiKey == null || apiKey.isBlank()) {
            return Map.of("error", "Aviation Stack API key is not configured.");
        }

        String url = "http://api.aviationstack.com/v1/flights?access_key=" + apiKey
                + "&flight_iata=" + flightIata;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            Map<String, Object> body = objectMapper.readValue(response.body(),
                    new TypeReference<>() {});

            List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
            if (data == null || data.isEmpty()) {
                return Map.of("error", "No flight data found for " + flightIata + ".");
            }

            return data.get(0);
        } catch (Exception e) {
            return Map.of("error", "API call failed: " + e.getMessage());
        }
    }
}
