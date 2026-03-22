package com.example.sendnotification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class WebhookService {

    private static final Logger logger = LoggerFactory.getLogger(WebhookService.class);

    @Value("${webhook.url:}")
    private String webhookUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void fireWebhook(Map<String, Object> flightData) {
        Map<String, Object> payload = Map.of(
                "event", "flight_data_retrieved",
                "data", flightData
        );

        if (webhookUrl == null || webhookUrl.isBlank()) {
            logger.info("Webhook event fired (no URL configured): {}", payload);
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(webhookUrl, request, String.class);
            logger.info("Webhook sent to {}", webhookUrl);
        } catch (Exception e) {
            logger.error("Webhook failed: {}", e.getMessage());
        }
    }
}
