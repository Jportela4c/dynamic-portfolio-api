package com.ofb.mock.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
public class JWSSigningService {

    @ConfigProperty(name = "jws.algorithm", defaultValue = "PS256")
    String algorithm;

    @ConfigProperty(name = "jws.key-id", defaultValue = "ofb-jws-key-1")
    String keyId;

    private RSAKey rsaJWK;
    private JWSSigner signer;

    @PostConstruct
    public void init() {
        try {
            log.info("Initializing JWS signing with algorithm: {}", algorithm);
            rsaJWK = new RSAKeyGenerator(2048)
                    .keyID(keyId)
                    .generate();
            signer = new RSASSASigner(rsaJWK);
            log.info("JWS signing initialized successfully with key ID: {}", keyId);
        } catch (Exception e) {
            log.error("Failed to initialize JWS signing", e);
            throw new RuntimeException("Failed to initialize JWS signing", e);
        }
    }

    public String signPayload(String payload) {
        try {
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.PS256)
                    .keyID(keyId)
                    .type(JOSEObjectType.JWT)
                    .build();

            JWSObject jwsObject = new JWSObject(header, new Payload(payload));
            jwsObject.sign(signer);

            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Failed to sign payload", e);
            throw new RuntimeException("Failed to sign payload", e);
        }
    }

    public RSAKey getPublicJWK() {
        return rsaJWK.toPublicJWK();
    }
}
