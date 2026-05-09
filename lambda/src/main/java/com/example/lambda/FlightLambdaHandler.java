package com.example.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class FlightLambdaHandler
        implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final FlightService flightService;
    private final WebhookService webhookService;
    private final ObjectMapper objectMapper;

    public FlightLambdaHandler() {
        String apiKey = System.getenv("AVIATION_STACK_API");
        String webhookUrl = System.getenv("WEBHOOK_URL");
        this.flightService = new FlightService(apiKey);
        this.webhookService = new WebhookService(webhookUrl);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        String flight = "WN2026";

        Map<String, String> queryParams = event.getQueryStringParameters();
        if (queryParams != null && queryParams.containsKey("flight")) {
            flight = queryParams.get("flight");
        }

        Map<String, Object> flightData = flightService.getFlightData(flight);
        webhookService.fireWebhook(flightData);

        try {
            String body = objectMapper.writeValueAsString(flightData);
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(200)
                    .withHeaders(Map.of(
                            "Content-Type", "application/json",
                            "Access-Control-Allow-Origin", "*"))
                    .withBody(body)
                    .build();
        } catch (Exception e) {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(500)
                    .withHeaders(Map.of("Content-Type", "application/json"))
                    .withBody("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}
