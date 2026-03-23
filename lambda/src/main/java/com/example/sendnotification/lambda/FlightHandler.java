package com.example.sendnotification.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;

import java.util.Map;

public class FlightHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final FlightService flightService;
    private final WebhookService webhookService;
    private final Gson gson;

    public FlightHandler() {
        String apiKey = System.getenv("AVIATION_STACK_API");
        String webhookUrl = System.getenv("WEBHOOK_URL");
        this.flightService = new FlightService(apiKey != null ? apiKey : "");
        this.webhookService = new WebhookService(webhookUrl != null ? webhookUrl : "");
        this.gson = new Gson();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        String flight = "WN2026";

        Map<String, String> queryParams = input.getQueryStringParameters();
        if (queryParams != null && queryParams.containsKey("flight")) {
            flight = queryParams.get("flight");
        }

        Map<String, Object> flightData = flightService.getFlightData(flight);
        webhookService.fireWebhook(flightData);

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
