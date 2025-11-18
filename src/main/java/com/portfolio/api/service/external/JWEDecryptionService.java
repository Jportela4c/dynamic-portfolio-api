package com.portfolio.api.service.external;

import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.portfolio.api.config.OFBProviderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class JWEDecryptionService {

    private final OFBProviderProperties properties;
    private final ResourceLoader resourceLoader;

    private RSAPrivateKey privateKey;

    public JWTClaimsSet decryptAndValidate(String jweToken) throws Exception {
        log.debug("Decrypting JWE token");

        if (privateKey == null) {
            loadPrivateKey();
        }

        EncryptedJWT encryptedJWT = EncryptedJWT.parse(jweToken);
        JWEDecrypter decrypter = new RSADecrypter(privateKey);

        encryptedJWT.decrypt(decrypter);

        JWTClaimsSet claims = encryptedJWT.getJWTClaimsSet();
        if (claims == null) {
            throw new SecurityException("JWE decryption failed - no claims found");
        }
        validateClaims(claims);

        log.debug("JWE token decrypted and validated successfully");
        return claims;
    }

    private void loadPrivateKey() throws Exception {
        log.debug("Loading private key from keystore: {}", properties.getKeystore().getPath());

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        Resource keystoreResource = resourceLoader.getResource(properties.getKeystore().getPath());

        try (InputStream keystoreStream = keystoreResource.getInputStream()) {
            keyStore.load(keystoreStream, properties.getKeystore().getPassword().toCharArray());
        }

        String alias = keyStore.aliases().nextElement();
        PrivateKey key = (PrivateKey) keyStore.getKey(alias, properties.getKeystore().getPassword().toCharArray());

        if (!(key instanceof RSAPrivateKey)) {
            throw new IllegalStateException("Keystore does not contain RSA private key");
        }

        this.privateKey = (RSAPrivateKey) key;
        log.debug("Private key loaded successfully");
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
