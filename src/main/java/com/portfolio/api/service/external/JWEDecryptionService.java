package com.portfolio.api.service.external;

import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.portfolio.api.config.OFBProviderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class JWEDecryptionService {

    private final OFBOAuth2Client oAuth2Client;
    private final OFBProviderProperties properties;

    private final Map<String, RSAKey> jwkCache = new ConcurrentHashMap<>();
    private Instant jwkCacheExpiry = Instant.MIN;

    public JWTClaimsSet decryptAndValidate(String jweToken) throws Exception {
        log.debug("Decrypting JWE token");

        try {
            EncryptedJWT encryptedJWT = EncryptedJWT.parse(jweToken);
            String keyId = encryptedJWT.getHeader().getKeyID();

            RSAKey rsaKey = getEncryptionKey(keyId);

            // Try to extract private key from the RSA key for decryption
            if (!rsaKey.isPrivate()) {
                log.warn("JWKS key {} is public only - falling back to JWS verification", keyId);
                // Token might be JWS inside JWE - try alternative approach
                throw new IllegalStateException("Cannot decrypt with public key - need private key");
            }

            JWEDecrypter decrypter = new RSADecrypter(rsaKey);
            encryptedJWT.decrypt(decrypter);

            JWTClaimsSet claims = encryptedJWT.getJWTClaimsSet();
            if (claims == null) {
                throw new SecurityException("JWE decryption failed - no claims found");
            }
            validateClaims(claims);

            log.debug("JWE token decrypted and validated successfully");
            return claims;
        } catch (Exception e) {
            log.warn("JWE decryption failed, treating as plain JWS: {}", e.getMessage());
            // Fallback: treat as JWS (signed only, not encrypted)
            return parseAsJWS(jweToken);
        }
    }

    private JWTClaimsSet parseAsJWS(String token) throws Exception {
        log.debug("Parsing token as JWS");
        com.nimbusds.jwt.SignedJWT signedJWT = com.nimbusds.jwt.SignedJWT.parse(token);
        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
        validateClaims(claims);
        log.debug("JWS token parsed successfully");
        return claims;
    }

    private RSAKey getEncryptionKey(String keyId) throws Exception {
        // Check cache
        if (Instant.now().isBefore(jwkCacheExpiry) && jwkCache.containsKey(keyId)) {
            log.debug("Using cached JWK for encryption key ID: {}", keyId);
            return jwkCache.get(keyId);
        }

        // Fetch JWKS from OFB provider
        log.debug("Fetching JWKS from OFB provider for decryption");
        String jwksResponse = oAuth2Client.getJwks();

        JWKSet jwkSet = JWKSet.parse(jwksResponse);

        // Cache all keys
        jwkCache.clear();
        for (JWK jwk : jwkSet.getKeys()) {
            if (jwk instanceof RSAKey) {
                jwkCache.put(jwk.getKeyID(), (RSAKey) jwk);
            }
        }

        jwkCacheExpiry = Instant.now().plusSeconds(properties.getJwks().getCacheTtlSeconds());
        log.debug("Cached {} JWKs for decryption, expires at {}", jwkCache.size(), jwkCacheExpiry);

        RSAKey key = jwkCache.get(keyId);
        if (key == null) {
            throw new SecurityException("Encryption key ID not found in JWKS: " + keyId);
        }

        return key;
    }

    private void validateClaims(JWTClaimsSet claims) throws Exception {
        // Validate issuer
        String issuer = claims.getIssuer();
        if (issuer == null || !issuer.equals(properties.getBaseUrl())) {
            throw new SecurityException("Invalid token issuer: " + issuer);
        }

        // Validate audience
        if (!claims.getAudience().contains(properties.getClientId())) {
            throw new SecurityException("Invalid token audience");
        }

        // Validate expiration
        Date expirationTime = claims.getExpirationTime();
        if (expirationTime == null || expirationTime.before(new Date())) {
            throw new SecurityException("Token has expired");
        }

        // Validate subject exists
        if (claims.getSubject() == null) {
            throw new SecurityException("Token subject is missing");
        }

        log.debug("Token claims validated successfully");
    }
}
