package com.portfolio.api.provider.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.portfolio.api.config.OFBProviderProperties;
import com.portfolio.api.provider.OFBAuthProvider;
import com.portfolio.api.service.external.JWEDecryptionService;
import com.portfolio.api.service.external.OFBOAuth2Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Demo implementation of OFB authentication provider.
 *
 * Uses pre-configured client IDs mapped to test customers in the mock server.
 * Tokens are generated LAZILY on first request for each customer.
 */
@Slf4j
@Service
@Profile("dev")
@RequiredArgsConstructor
public class DemoOFBAuthProvider implements OFBAuthProvider {

    private final OFBOAuth2Client oAuth2Client;
    private final OFBProviderProperties properties;
    private final JWEDecryptionService jweDecryptionService;
    private final ObjectMapper objectMapper;

    // Customer ID mapping from config
    private static final Map<String, String> CUSTOMER_TO_CLIENT_ID = Map.of(
            "cliente-101", "portfolio-api-conservative",
            "cliente-102", "portfolio-api-moderate",
            "cliente-103", "portfolio-api-aggressive"
    );

    // Token cache: customerId → TokenData
    private final Map<String, TokenData> tokenCache = new ConcurrentHashMap<>();

    @Override
    public String authenticateCustomer(String customerId) throws Exception {
        // Check if we have a valid cached token
        TokenData cached = tokenCache.get(customerId);
        if (cached != null && Instant.now().isBefore(cached.expiry)) {
            log.debug("Using cached token for customer: {}", customerId);
            return cached.accessToken;
        }

        // Generate new token lazily
        log.info("Generating new token for customer: {}", customerId);
        String clientId = CUSTOMER_TO_CLIENT_ID.get(customerId);
        if (clientId == null) {
            throw new IllegalArgumentException("Unknown customer ID: " + customerId);
        }

        String token = generateTokenForClient(clientId);

        // Cache with TTL
        Instant expiry = Instant.now().plusSeconds(properties.getToken().getCacheTtlSeconds());
        tokenCache.put(customerId, new TokenData(token, expiry));

        return token;
    }

    private String generateTokenForClient(String clientId) throws Exception {
        log.debug("Starting OAuth2 PAR flow for client: {}", clientId);

        // Step 1: PAR
        String requestUri = pushAuthorizationRequest(clientId);

        // Step 2: Authorize
        String authCode = getAuthorizationCode(requestUri, clientId);

        // Step 3: Token Exchange
        TokenResponse tokenResponse = exchangeCodeForTokens(authCode, clientId);

        // Step 4: Validate ID token
        JWTClaimsSet idTokenClaims = jweDecryptionService.decryptAndValidate(tokenResponse.idToken);
        log.debug("ID token validated for subject: {}", idTokenClaims.getSubject());

        log.info("OAuth2 PAR flow completed for client: {}", clientId);
        return tokenResponse.accessToken;
    }

    private String pushAuthorizationRequest(String clientId) throws Exception {
        MultiValueMap<String, String> parRequest = new LinkedMultiValueMap<>();
        parRequest.add("client_id", clientId);
        parRequest.add("redirect_uri", properties.getRedirectUri());
        parRequest.add("scope", properties.getScope());
        parRequest.add("response_type", "code");
        parRequest.add("state", UUID.randomUUID().toString());
        parRequest.add("nonce", UUID.randomUUID().toString());

        String responseBody = oAuth2Client.pushAuthorizationRequest(parRequest);
        JsonNode parResponse = objectMapper.readTree(responseBody);
        return parResponse.get("request_uri").asText();
    }

    private String getAuthorizationCode(String requestUri, String clientId) throws Exception {
        String responseBody = oAuth2Client.authorize(requestUri, clientId);
        JsonNode authResponse = objectMapper.readTree(responseBody);
        return authResponse.get("code").asText();
    }

    private TokenResponse exchangeCodeForTokens(String authCode, String clientId) throws Exception {
        MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
        tokenRequest.add("grant_type", "authorization_code");
        tokenRequest.add("code", authCode);
        tokenRequest.add("redirect_uri", properties.getRedirectUri());
        tokenRequest.add("client_id", clientId);

        String responseBody = oAuth2Client.exchangeToken(tokenRequest);
        JsonNode tokenResponse = objectMapper.readTree(responseBody);

        String accessToken = tokenResponse.get("access_token").asText();
        String idToken = tokenResponse.get("id_token").asText();

        return new TokenResponse(accessToken, idToken);
    }

    private record TokenData(String accessToken, Instant expiry) {}
    private record TokenResponse(String accessToken, String idToken) {}
}
