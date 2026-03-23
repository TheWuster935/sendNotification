package com.example.sendnotification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SendNotificationController {

    @Value("${api.gateway.base-url}")
    private String apiGatewayBaseUrl;

    @GetMapping("/")
    public String hello(Model model) {
        model.addAttribute("apiBaseUrl", apiGatewayBaseUrl);
        return "index";
    }
}
