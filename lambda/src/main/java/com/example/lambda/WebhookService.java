package com.example.lambda;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class WebhookService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @SuppressWarnings("unchecked")
    public void fireWebhook(Map<String, Object> flightData, String webhookUrl) {
        String flightName = "Unknown";
        Object flightInfo = flightData.get("flight");
        if (flightInfo instanceof Map) {
            Object iata = ((Map<String, Object>) flightInfo).get("iata");
            if (iata != null) {
                flightName = iata.toString();
            }
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "flight_data_retrieved");
        payload.put("flightName", flightName);
        payload.put("data", flightData);

        if (webhookUrl == null || webhookUrl.isBlank()) {
            System.out.println("Webhook event fired (no URL configured): " + gson.toJson(payload));
            return;
        }

        try {
            String json = gson.toJson(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Webhook sent to " + webhookUrl);
        } catch (IOException | InterruptedException e) {
            System.err.println("Webhook failed: " + e.getMessage());
        }
    }
}
