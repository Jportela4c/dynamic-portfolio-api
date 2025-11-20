package com.portfolio.api.service;

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
 * Service for obtaining OFB OAuth2 tokens.
 *
 * Tokens are cached lazily using Spring Cache abstraction.
 * Uses single client_id from configuration for all customers.
 * Customer identification via CPF passed to OAuth2 server.
 *
 * This service is SHARED between dev and prod profiles.
 * Only the orchestration (provider) differs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OFBAuthService {

    private final OFBOAuth2Client oAuth2Client;
    private final OFBProviderProperties properties;
    private final JWEDecryptionService jweDecryptionService;
    private final ObjectMapper objectMapper;

    /**
     * Authenticate customer via CPF and obtain OAuth2 access token.
     * Cached by Spring with TTL configured in cache manager.
     *
     * @param cpf Customer CPF (11 digits)
     * @return Access token for making OFB API calls
     * @throws Exception if OAuth2 flow fails
     */
    @Cacheable(value = "ofbTokens", key = "#cpf")
    public String authenticateWithCPF(String cpf) throws Exception {
        log.info("Generating new OAuth2 token for CPF: {}***", cpf.substring(0, 3));

        // Step 1: Pushed Authorization Request (PAR) with CPF hint
        String requestUri = pushAuthorizationRequest(cpf);

        // Step 2: Get authorization code
        String authCode = getAuthorizationCode(requestUri, cpf);

        // Step 3: Exchange code for tokens
        TokenResponse tokenResponse = exchangeCodeForTokens(authCode);

        // Step 4: Validate ID token
        JWTClaimsSet idTokenClaims = jweDecryptionService.decryptAndValidate(tokenResponse.idToken);
        log.debug("ID token validated for subject: {}", idTokenClaims.getSubject());

        log.info("OAuth2 PAR flow completed for CPF");
        return tokenResponse.accessToken;
    }

    private String pushAuthorizationRequest(String cpf) throws Exception {
        MultiValueMap<String, String> parRequest = new LinkedMultiValueMap<>();
        parRequest.add("client_id", properties.getClientId());
        parRequest.add("redirect_uri", properties.getRedirectUri());
        parRequest.add("scope", properties.getScope());
        parRequest.add("response_type", "code");
        parRequest.add("state", UUID.randomUUID().toString());
        parRequest.add("nonce", UUID.randomUUID().toString());
        parRequest.add("cpf_hint", cpf);

        String responseBody = oAuth2Client.pushAuthorizationRequest(parRequest);
        JsonNode parResponse = objectMapper.readTree(responseBody);
        return parResponse.get("request_uri").asText();
    }

    private String getAuthorizationCode(String requestUri, String cpf) throws Exception {
        String responseBody = oAuth2Client.authorize(requestUri, properties.getClientId());
        JsonNode authResponse = objectMapper.readTree(responseBody);
        return authResponse.get("code").asText();
    }

    private TokenResponse exchangeCodeForTokens(String authCode) throws Exception {
        MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
        tokenRequest.add("grant_type", "authorization_code");
        tokenRequest.add("code", authCode);
        tokenRequest.add("redirect_uri", properties.getRedirectUri());
        tokenRequest.add("client_id", properties.getClientId());

        String responseBody = oAuth2Client.exchangeToken(tokenRequest);
        JsonNode tokenResponse = objectMapper.readTree(responseBody);

        String accessToken = tokenResponse.get("access_token").asText();
        String idToken = tokenResponse.get("id_token").asText();

        return new TokenResponse(accessToken, idToken);
    }

    private record TokenResponse(String accessToken, String idToken) {}
}
