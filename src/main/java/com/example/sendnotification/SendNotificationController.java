package com.example.sendnotification;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SendNotificationController {

    @GetMapping("/")
    public String hello() {
        return "hello";
    }
}
