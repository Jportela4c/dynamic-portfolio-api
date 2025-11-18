package com.portfolio.api.service.external;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.portfolio.api.config.OFBProviderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class JWSVerificationService {

    @Qualifier("ofbRestTemplate")
    private final RestTemplate restTemplate;
    private final OFBProviderProperties properties;

    private final Map<String, RSAKey> jwkCache = new ConcurrentHashMap<>();
    private Instant jwkCacheExpiry = Instant.MIN;

    public String verifyAndExtractPayload(String jwsToken) throws Exception {
        log.debug("Verifying JWS token");

        JWSObject jwsObject = JWSObject.parse(jwsToken);
        String keyId = jwsObject.getHeader().getKeyID();

        RSAKey rsaKey = getPublicKey(keyId);
        JWSVerifier verifier = new RSASSAVerifier(rsaKey);

        if (!jwsObject.verify(verifier)) {
            throw new SecurityException("JWS signature verification failed");
        }

        log.debug("JWS signature verified successfully");
        return jwsObject.getPayload().toString();
    }

    private RSAKey getPublicKey(String keyId) throws Exception {
        // Check cache
        if (Instant.now().isBefore(jwkCacheExpiry) && jwkCache.containsKey(keyId)) {
            log.debug("Using cached JWK for key ID: {}", keyId);
            return jwkCache.get(keyId);
        }

        // Fetch JWKS
        log.debug("Fetching JWKS from OFB provider");
        String jwksUrl = properties.getBaseUrl() + "/oauth2/jwks";
        String jwksResponse = restTemplate.getForObject(jwksUrl, String.class);

        JWKSet jwkSet = JWKSet.parse(jwksResponse);

        // Cache all keys
        jwkCache.clear();
        for (JWK jwk : jwkSet.getKeys()) {
            if (jwk instanceof RSAKey) {
                jwkCache.put(jwk.getKeyID(), (RSAKey) jwk);
            }
        }

        jwkCacheExpiry = Instant.now().plusSeconds(properties.getJwks().getCacheTtlSeconds());
        log.debug("Cached {} JWKs, expires at {}", jwkCache.size(), jwkCacheExpiry);

        RSAKey key = jwkCache.get(keyId);
        if (key == null) {
            throw new SecurityException("Key ID not found in JWKS: " + keyId);
        }

        return key;
    }
}
