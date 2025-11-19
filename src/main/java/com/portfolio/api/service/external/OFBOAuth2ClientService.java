package com.portfolio.api.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.portfolio.api.config.OFBProviderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OFBOAuth2ClientService {

    private final OFBOAuth2Client oAuth2Client;
    private final OFBProviderProperties properties;
    private final JWEDecryptionService jweDecryptionService;
    private final ObjectMapper objectMapper;

    private String cachedAccessToken;
    private Instant tokenExpiry = Instant.MIN;

    public String getAccessToken() throws Exception {
        // Check cache
        if (Instant.now().isBefore(tokenExpiry) && cachedAccessToken != null) {
            log.debug("Using cached access token");
            return cachedAccessToken;
        }

        log.info("Initiating OAuth2 PAR flow");

        // Step 1: Push Authorization Request (PAR)
        String requestUri = pushAuthorizationRequest();

        // Step 2: Exchange authorization code (simulated for client credentials)
        String authorizationCode = getAuthorizationCode(requestUri);

        // Step 3: Exchange code for tokens
        TokenResponse tokenResponse = exchangeCodeForTokens(authorizationCode);

        // Step 4: Decrypt and validate ID token
        JWTClaimsSet idTokenClaims = jweDecryptionService.decryptAndValidate(tokenResponse.idToken);
        log.debug("ID token validated for subject: {}", idTokenClaims.getSubject());

        // Cache token
        cachedAccessToken = tokenResponse.accessToken;
        tokenExpiry = Instant.now().plusSeconds(properties.getToken().getCacheTtlSeconds());

        log.info("OAuth2 PAR flow completed successfully");
        return cachedAccessToken;
    }

    private String pushAuthorizationRequest() throws Exception {
        log.debug("Pushing authorization request to OFB provider");

        MultiValueMap<String, String> parRequest = new LinkedMultiValueMap<>();
        parRequest.add("client_id", properties.getClientId());
        parRequest.add("redirect_uri", properties.getRedirectUri());
        parRequest.add("scope", properties.getScope());
        parRequest.add("response_type", "code");
        parRequest.add("state", UUID.randomUUID().toString());
        parRequest.add("nonce", UUID.randomUUID().toString());

        String responseBody = oAuth2Client.pushAuthorizationRequest(parRequest);
        JsonNode parResponse = objectMapper.readTree(responseBody);
        String requestUri = parResponse.get("request_uri").asText();

        log.debug("PAR request successful, received request_uri: {}", requestUri);
        return requestUri;
    }

    private String getAuthorizationCode(String requestUri) throws Exception {
        log.debug("Exchanging request_uri for authorization code");

        MultiValueMap<String, String> authRequest = new LinkedMultiValueMap<>();
        authRequest.add("request_uri", requestUri);
        authRequest.add("client_id", properties.getClientId());

        String responseBody = oAuth2Client.authorize(authRequest);
        JsonNode authResponse = objectMapper.readTree(responseBody);
        String authorizationCode = authResponse.get("code").asText();

        log.debug("Authorization code received");
        return authorizationCode;
    }

    private TokenResponse exchangeCodeForTokens(String authorizationCode) throws Exception {
        log.debug("Exchanging authorization code for tokens");

        MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
        tokenRequest.add("grant_type", "authorization_code");
        tokenRequest.add("code", authorizationCode);
        tokenRequest.add("redirect_uri", properties.getRedirectUri());
        tokenRequest.add("client_id", properties.getClientId());

        String responseBody = oAuth2Client.exchangeToken(tokenRequest);
        JsonNode tokenResponse = objectMapper.readTree(responseBody);

        String accessToken = tokenResponse.get("access_token").asText();
        String idToken = tokenResponse.get("id_token").asText();

        log.debug("Token exchange successful");
        return new TokenResponse(accessToken, idToken);
    }

    public void invalidateCache() {
        log.info("Invalidating cached access token");
        cachedAccessToken = null;
        tokenExpiry = Instant.MIN;
    }

    private record TokenResponse(String accessToken, String idToken) {
    }
}
