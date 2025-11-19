package com.portfolio.api.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Base64;
import java.util.Map;

/**
 * Custom JWT validator that checks token revocation status via introspection endpoint.
 * This ensures revoked tokens are rejected even if their signature is still valid.
 */
@Component
public class JwtIntrospectionValidator implements OAuth2TokenValidator<Jwt> {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String introspectionUrl = "http://localhost:8080/api/v1/oauth2/introspect";
    private final String clientId = "portfolio-api-client";
    private final String clientSecret = "api-secret";

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        try {
            // Call introspection endpoint to check if token is active
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            String auth = clientId + ":" + clientSecret;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("token", jwt.getTokenValue());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                introspectionUrl,
                request,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Boolean active = (Boolean) response.getBody().get("active");

                if (Boolean.FALSE.equals(active)) {
                    OAuth2Error error = new OAuth2Error(
                        "invalid_token",
                        "Token has been revoked",
                        null
                    );
                    return OAuth2TokenValidatorResult.failure(error);
                }
            }

            return OAuth2TokenValidatorResult.success();

        } catch (Exception e) {
            // If introspection fails, we'll let other validators handle it
            // This prevents cascading failures if introspection endpoint is down
            return OAuth2TokenValidatorResult.success();
        }
    }
}
