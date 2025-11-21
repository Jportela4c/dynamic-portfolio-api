package com.ofb.mock.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.FileInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Startup
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
    private JWSSigner signer;
    private JWEEncrypter encrypter;

    private final Map<String, PushedAuthRequest> parStore = new ConcurrentHashMap<>();
    private final Map<String, String> authCodeStore = new ConcurrentHashMap<>();

    /**
     * Valid client_id for this mock server.
     * In production OFB, this would be validated against registered clients.
     */
    private static final String VALID_CLIENT_ID = "portfolio-api";

    @PostConstruct
    public void init() {
        try {
            log.info("Initializing OAuth2 service");
            signingKey = new RSAKeyGenerator(2048).keyID(signingKeyId).generate();
            signer = new RSASSASigner(signingKey);

            // Load client's public key from certificate for JWE encryption
            RSAPublicKey clientPublicKey = loadClientPublicKey();
            encrypter = new RSAEncrypter(clientPublicKey);

            // Export public key for SmallRye JWT validation
            exportPublicKeyForJWTValidation();

            log.info("OAuth2 service initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize OAuth2 service", e);
            throw new RuntimeException("Failed to initialize OAuth2 service", e);
        }
    }

    private RSAPublicKey loadClientPublicKey() throws Exception {
        log.info("Loading client public key from certificate: /app-certs/client.crt");

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        try (FileInputStream fis = new FileInputStream("/app-certs/client.crt")) {
            X509Certificate cert = (X509Certificate) cf.generateCertificate(fis);
            RSAPublicKey publicKey = (RSAPublicKey) cert.getPublicKey();
            log.info("Client public key loaded successfully");
            return publicKey;
        }
    }

    private void exportPublicKeyForJWTValidation() {
        try {
            // Get public key in PEM format
            RSAPublicKey publicKey = signingKey.toRSAPublicKey();
            String pemPublicKey = "-----BEGIN PUBLIC KEY-----\n" +
                Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(publicKey.getEncoded()) +
                "\n-----END PUBLIC KEY-----\n";

            // Set as system property for SmallRye JWT
            System.setProperty("mp.jwt.verify.publickey", pemPublicKey);

            log.info("Configured SmallRye JWT with generated public key");
        } catch (Exception e) {
            log.warn("Failed to configure JWT public key (will fallback to manual validation): {}", e.getMessage());
        }
    }

    public String createPushedAuthRequest(String clientId, String cpfHint, String scope, String redirectUri) {
        // Validate client_id
        if (!VALID_CLIENT_ID.equals(clientId)) {
            throw new IllegalArgumentException("Invalid client_id: " + clientId);
        }

        // Validate CPF hint (demo only - in production, CPF comes from user login)
        if (cpfHint == null || cpfHint.length() != 11) {
            throw new IllegalArgumentException("Invalid cpf_hint: must be 11 digits");
        }

        String requestUri = "urn:ietf:params:oauth:request_uri:" + UUID.randomUUID();
        PushedAuthRequest par = new PushedAuthRequest(clientId, cpfHint, scope, redirectUri, Instant.now());
        parStore.put(requestUri, par);
        log.debug("Created PAR for CPF: {}***, requestUri: {}", cpfHint.substring(0, 3), requestUri);
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

        // Use CPF from PAR request (provided as cpf_hint in demo)
        String cpf = par.getCpfHint();

        Instant now = Instant.now();
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(cpf)  // CPF as subject (OFB standard)
                .audience(issuer)
                .expirationTime(Date.from(now.plusSeconds(tokenExpirySeconds)))
                .issueTime(Date.from(now))
                .claim("scope", par.getScope())
                .claim("client_id", par.getClientId())
                .claim("cpf", cpf)  // MANDATORY OFB claim
                .claim("consent_id", "consent-" + cpf)
                .jwtID(UUID.randomUUID().toString())
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(signingKeyId).build(),
                claimsSet
        );
        signedJWT.sign(signer);

        log.debug("Created access token for client: {}, CPF: {}***", par.getClientId(), cpf.substring(0, 3));
        return signedJWT.serialize();
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

        // OFB requirement: Encrypt ID token with JWE (nested JWE(JWS))
        JWEObject jweObject = new JWEObject(
                new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A256GCM)
                        .keyID(jweKeyId)
                        .contentType("JWT")
                        .build(),
                new Payload(signedJWT)
        );
        jweObject.encrypt(encrypter);

        log.debug("Created encrypted ID token (JWE) for client: {}", clientId);
        return jweObject.serialize();
    }

    public RSAKey getSigningKey() {
        return signingKey;
    }

    public RSAKey getSigningPublicKey() {
        return signingKey.toPublicJWK();
    }

    public static class PushedAuthRequest {
        private final String clientId;
        private final String cpfHint;
        private final String scope;
        private final String redirectUri;
        private final Instant createdAt;

        public PushedAuthRequest(String clientId, String cpfHint, String scope, String redirectUri, Instant createdAt) {
            this.clientId = clientId;
            this.cpfHint = cpfHint;
            this.scope = scope;
            this.redirectUri = redirectUri;
            this.createdAt = createdAt;
        }

        public String getClientId() { return clientId; }
        public String getCpfHint() { return cpfHint; }
        public String getScope() { return scope; }
        public String getRedirectUri() { return redirectUri; }
        public Instant getCreatedAt() { return createdAt; }
    }
}
