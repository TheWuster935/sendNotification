package com.example.sendnotification.lambda;

import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class WebhookService {

    private final String webhookUrl;
    private final HttpClient httpClient;
    private final Gson gson;

    public WebhookService(String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    @SuppressWarnings("unchecked")
    public void fireWebhook(Map<String, Object> flightData) {
        String flightName = "Unknown";
        Map<String, Object> flightInfo = (Map<String, Object>) flightData.get("flight");
        if (flightInfo != null && flightInfo.get("iata") != null) {
            flightName = flightInfo.get("iata").toString();
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "flight_data_retrieved");
        payload.put("flightName", flightName);
        payload.put("data", flightData);

        if (webhookUrl == null || webhookUrl.isBlank()) {
            System.out.println("Webhook event fired (no URL configured): " + payload);
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
        } catch (Exception e) {
            System.err.println("Webhook failed: " + e.getMessage());
        }
    }
}
