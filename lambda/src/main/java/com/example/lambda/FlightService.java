package com.example.lambda;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FlightService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public Map<String, Object> getFlightData(String flightIata, String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return Map.of("error", "Aviation Stack API key is not configured.");
        }

        String url = "http://api.aviationstack.com/v1/flights?access_key=" + apiKey + "&flight_iata=" + flightIata;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.body() == null || response.body().isBlank()) {
                return Map.of("error", "No response from Aviation Stack API.");
            }

            Map<String, Object> responseMap = gson.fromJson(response.body(),
                    new TypeToken<Map<String, Object>>() {}.getType());

            Object dataObj = responseMap.get("data");
            if (dataObj == null) {
                return Map.of("error", "No flight data found for " + flightIata + ".");
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) dataObj;
            if (data.isEmpty()) {
                return Map.of("error", "No flight data found for " + flightIata + ".");
            }

            return data.get(0);
        } catch (IOException | InterruptedException e) {
            return Map.of("error", "API call failed: " + e.getMessage());
        }
    }
}
