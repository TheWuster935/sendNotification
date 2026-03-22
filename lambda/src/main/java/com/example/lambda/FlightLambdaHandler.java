package com.example.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlightLambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final String apiKey;
    private final String webhookUrl;

    public FlightLambdaHandler() {
        this.apiKey = System.getenv("AVIATIONSTACK_API_KEY");
        this.webhookUrl = System.getenv("WEBHOOK_URL");
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        String flight = "WN2026";
        Map<String, String> queryParams = input.getQueryStringParameters();
        if (queryParams != null && queryParams.containsKey("flight")) {
            flight = queryParams.get("flight");
        }

        Map<String, Object> flightData = getFlightData(flight);
        fireWebhook(flightData);

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "GET,OPTIONS");
        headers.put("Access-Control-Allow-Headers", "Content-Type");
        response.setHeaders(headers);

        try {
            response.setStatusCode(200);
            response.setBody(objectMapper.writeValueAsString(flightData));
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setBody("{\"error\":\"Failed to serialize response\"}");
        }

        return response;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getFlightData(String flightIata) {
        if (apiKey == null || apiKey.isBlank()) {
            return Map.of("error", "Aviation Stack API key is not configured.");
        }

        String url = "http://api.aviationstack.com/v1/flights?access_key=" + apiKey + "&flight_iata=" + flightIata;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            Map<String, Object> responseBody = objectMapper.readValue(
                    httpResponse.body(), new TypeReference<Map<String, Object>>() {});

            if (responseBody == null) {
                return Map.of("error", "No response from Aviation Stack API.");
            }

            List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
            if (data == null || data.isEmpty()) {
                return Map.of("error", "No flight data found for " + flightIata + ".");
            }

            return data.get(0);
        } catch (Exception e) {
            return Map.of("error", "API call failed: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void fireWebhook(Map<String, Object> flightData) {
        String flightName = "Unknown";
        Object flightObj = flightData.get("flight");
        if (flightObj instanceof Map) {
            Map<String, Object> flightInfo = (Map<String, Object>) flightObj;
            if (flightInfo.get("iata") != null) {
                flightName = flightInfo.get("iata").toString();
            }
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "flight_data_retrieved");
        payload.put("flightName", flightName);
        payload.put("data", flightData);

        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            // Log silently; webhook failure should not break the response
        }
    }
}
