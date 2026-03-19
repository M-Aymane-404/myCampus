package com.mycampus.backend.metier.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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
        if (!StringUtils.hasText(token)) {
            return false;
        }

        try {
            MultiValueMap<String, String> corps = new LinkedMultiValueMap<>();
            corps.add("secret", recaptchaSecret);
            corps.add("response", token);

            Map<String, Object> response = restTemplate.postForObject(
                    RECAPTCHA_VERIFY_URL,
                    corps,
                    Map.class
            );

            if (response != null) {
                Boolean success = (Boolean) response.get("success");
                if (success != null && !success) {
                    log.warn("Echec de validation reCAPTCHA.");
                }
                return success != null && success;
            }
        } catch (RestClientException e) {
            log.error("Erreur lors de la validation reCAPTCHA", e);
        }
        return false;
    }
}
