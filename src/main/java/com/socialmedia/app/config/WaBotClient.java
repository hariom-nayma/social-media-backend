package com.socialmedia.app.config;

import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class WaBotClient {
    private final RestTemplate rest = new RestTemplate();
    private final String botUrl = "http://localhost:3000/api/otp/send-otp";
    private final String apiKey = System.getenv("BOT_API_KEY");

    public String sendOtp(String phone) {
        var body = Map.of("phone", phone, "via", "whatsapp");
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-bot-api-key", apiKey);
        HttpEntity<Map<String, String>> req = new HttpEntity<>(body, headers);
        ResponseEntity<Map> resp = rest.postForEntity(botUrl, req, Map.class);
        return resp.getBody().get("id").toString();
    }
}

