package com.portfolio.api.service.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.portfolio.api.config.OFBProviderProperties;
import com.portfolio.api.service.external.JWEDecryptionService;
import com.portfolio.api.service.external.OFBOAuth2Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.UUID;

/**
 * Demo-only service for obtaining OFB OAuth2 tokens.
 *
 * Tokens are cached lazily using Spring Cache abstraction.
 * NO CUSTOMER MAPPING - clientId comes from endpoint path parameter.
 * Mock server is the single source of truth for all data.
 */
@Slf4j
@Service
@Profile("dev")
@RequiredArgsConstructor
public class DemoOFBAuthService {

    private final OFBOAuth2Client oAuth2Client;
    private final OFBProviderProperties properties;
    private final JWEDecryptionService jweDecryptionService;
    private final ObjectMapper objectMapper;

    /**
     * Get OAuth2 access token for the given clientId.
     * Cached by Spring with TTL configured in cache manager.
     *
     * @param clientId OAuth2 client identifier (e.g., "portfolio-api-conservative")
     * @return Access token for making OFB API calls
     * @throws Exception if OAuth2 flow fails
     */
    @Cacheable(value = "ofbTokens", key = "#clientId")
    public String getTokenForClient(String clientId) throws Exception {
        log.info("Generating new OAuth2 token for client: {}", clientId);

        // Step 1: Pushed Authorization Request (PAR)
        String requestUri = pushAuthorizationRequest(clientId);

        // Step 2: Get authorization code
        String authCode = getAuthorizationCode(requestUri, clientId);

        // Step 3: Exchange code for tokens
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

    private record TokenResponse(String accessToken, String idToken) {}
}
