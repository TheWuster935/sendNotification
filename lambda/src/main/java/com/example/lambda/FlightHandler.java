package com.example.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;

import java.util.Map;

public class FlightHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final FlightService flightService = new FlightService();
    private final WebhookService webhookService = new WebhookService();
    private final Gson gson = new Gson();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        String flight = "WN2026";

        Map<String, String> queryParams = input.getQueryStringParameters();
        if (queryParams != null && queryParams.containsKey("flight")) {
            flight = queryParams.get("flight");
        }

        String apiKey = System.getenv("AVIATION_STACK_API");
        String webhookUrl = System.getenv("WEBHOOK_URL");

        Map<String, Object> flightData = flightService.getFlightData(flight, apiKey);
        webhookService.fireWebhook(flightData, webhookUrl);

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setHeaders(Map.of(
                "Content-Type", "application/json",
                "Access-Control-Allow-Origin", "*",
                "Access-Control-Allow-Methods", "GET,OPTIONS",
                "Access-Control-Allow-Headers", "Content-Type"
        ));
        response.setBody(gson.toJson(flightData));
        return response;
    }
}
