package com.mycampus.backend.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class RecaptchaService {

    private static final String RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
    private final RestTemplate restTemplate;

    @Value("${recaptcha.secret}")
    private String recaptchaSecret;

    public RecaptchaService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean validateRecaptcha(String token) {
        try {
            String url = RECAPTCHA_VERIFY_URL + "?secret=" + recaptchaSecret + "&response=" + token;
            
            Map<String, Object> response = restTemplate.postForObject(
                    url,
                    null,
                    Map.class
            );

            if (response != null) {
                Boolean success = (Boolean) response.get("success");
                log.info("reCAPTCHA validation result: {}", success);
                if (success != null && !success) {
                    log.warn("reCAPTCHA validation failed. Error codes: {}", response.get("error-codes"));
                }
                return success != null && success;
            }
        } catch (Exception e) {
            log.error("Error validating reCAPTCHA token", e);
        }
        return false;
    }
}
