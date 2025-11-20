package com.ofb.mock.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ApplicationScoped
public class OAuth2Service {

    @ConfigProperty(name = "oauth.issuer", defaultValue = "https://localhost:8443")
    String issuer;

    @ConfigProperty(name = "oauth.token.expiry-seconds", defaultValue = "3600")
    int tokenExpirySeconds;

    @ConfigProperty(name = "oauth.signing.key-id", defaultValue = "ofb-mock-key-1")
    String signingKeyId;

    @ConfigProperty(name = "jwe.key-id", defaultValue = "ofb-jwe-key-1")
    String jweKeyId;

    private RSAKey signingKey;
    private RSAKey encryptionKey;
    private JWSSigner signer;
    private JWEEncrypter encrypter;

    private final Map<String, PushedAuthRequest> parStore = new ConcurrentHashMap<>();
    private final Map<String, String> authCodeStore = new ConcurrentHashMap<>();

    /**
     * Demo customer mapping: client_id → customer_id
     *
     * Maps OAuth2 client IDs to test customer identifiers.
     * The customer ID is embedded in JWT 'sub' claim.
     */
    private final Map<String, String> clientToCustomerMap = Map.of(
        "portfolio-api-conservative", "cliente-101",
        "portfolio-api-moderate", "cliente-102",
        "portfolio-api-aggressive", "cliente-103",
        "portfolio-api-client", "cliente-default"
    );

    @PostConstruct
    public void init() {
        try {
            log.info("Initializing OAuth2 service");
            signingKey = new RSAKeyGenerator(2048).keyID(signingKeyId).generate();
            encryptionKey = new RSAKeyGenerator(2048).keyID(jweKeyId).generate();
            signer = new RSASSASigner(signingKey);
            encrypter = new RSAEncrypter(encryptionKey);
            log.info("OAuth2 service initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize OAuth2 service", e);
            throw new RuntimeException("Failed to initialize OAuth2 service", e);
        }
    }

    public String createPushedAuthRequest(String clientId, String scope, String redirectUri) {
        String requestUri = "urn:ietf:params:oauth:request_uri:" + UUID.randomUUID();
        PushedAuthRequest par = new PushedAuthRequest(clientId, scope, redirectUri, Instant.now());
        parStore.put(requestUri, par);
        log.debug("Created PAR: {}", requestUri);
        return requestUri;
    }

    public PushedAuthRequest getPushedAuthRequest(String requestUri) {
        return parStore.get(requestUri);
    }

    public String createAuthorizationCode(String requestUri) {
        String authCode = "CODE_" + UUID.randomUUID().toString().replace("-", "");
        authCodeStore.put(authCode, requestUri);
        log.debug("Created authorization code for requestUri: {}", requestUri);
        return authCode;
    }

    public String createAccessToken(String authCode) throws JOSEException {
        String requestUri = authCodeStore.remove(authCode);
        if (requestUri == null) {
            throw new IllegalArgumentException("Invalid authorization code");
        }

        PushedAuthRequest par = parStore.get(requestUri);
        if (par == null) {
            throw new IllegalArgumentException("PAR not found for authorization code");
        }

        // Map client_id to customer_id for demo purposes
        String customerId = getCustomerIdForClient(par.getClientId());

        Instant now = Instant.now();
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(customerId)  // Customer ID in 'sub' claim
                .audience(issuer)
                .expirationTime(Date.from(now.plusSeconds(tokenExpirySeconds)))
                .issueTime(Date.from(now))
                .claim("scope", par.getScope())
                .claim("client_id", par.getClientId())
                .claim("consent_id", "consent-" + customerId)
                .jwtID(UUID.randomUUID().toString())
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(signingKeyId).build(),
                claimsSet
        );
        signedJWT.sign(signer);

        log.debug("Created access token for client: {}, customer: {}", par.getClientId(), customerId);
        return signedJWT.serialize();
    }

    /**
     * Maps OAuth2 client ID to customer ID.
     *
     * @param clientId OAuth2 client identifier
     * @return Customer identifier for demo data
     */
    private String getCustomerIdForClient(String clientId) {
        return clientToCustomerMap.getOrDefault(clientId, "cliente-default");
    }

    public String createIdToken(String clientId) throws JOSEException {
        Instant now = Instant.now();
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(clientId)
                .audience(clientId)
                .expirationTime(Date.from(now.plusSeconds(tokenExpirySeconds)))
                .issueTime(Date.from(now))
                .jwtID(UUID.randomUUID().toString())
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(signingKeyId).build(),
                claimsSet
        );
        signedJWT.sign(signer);

        // For mock/testing: return JWS only (not encrypted)
        // In production OFB: would be nested JWE(JWS) for FAPI compliance
        log.debug("Created signed ID token for client: {}", clientId);
        return signedJWT.serialize();
    }

    public RSAKey getSigningKey() {
        return signingKey;
    }

    public RSAKey getSigningPublicKey() {
        return signingKey.toPublicJWK();
    }

    public RSAKey getEncryptionPublicKey() {
        return encryptionKey.toPublicJWK();
    }

    public static class PushedAuthRequest {
        private final String clientId;
        private final String scope;
        private final String redirectUri;
        private final Instant createdAt;

        public PushedAuthRequest(String clientId, String scope, String redirectUri, Instant createdAt) {
            this.clientId = clientId;
            this.scope = scope;
            this.redirectUri = redirectUri;
            this.createdAt = createdAt;
        }

        public String getClientId() { return clientId; }
        public String getScope() { return scope; }
        public String getRedirectUri() { return redirectUri; }
        public Instant getCreatedAt() { return createdAt; }
    }
}
