package com.ofb.mock;

import com.ofb.mock.security.JWSSigningService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@EnabledIfSystemProperty(named = "ofb.mock.tests.enabled", matches = "true")
public class JWSSigningTest {

    @Inject
    JWSSigningService jwsSigningService;

    @Test
    public void testSignPayload() {
        String payload = "{\"test\":\"data\"}";
        String signed = jwsSigningService.signPayload(payload);

        assertNotNull(signed);
        assertTrue(signed.split("\\.").length >= 3, "JWS should have at least 3 parts");
    }

    @Test
    public void testPublicKeyExport() {
        var publicKey = jwsSigningService.getPublicJWK();

        assertNotNull(publicKey);
        assertNotNull(publicKey.getKeyID());
        assertNull(publicKey.getPrivateExponent(), "Public key should not contain private components");
    }

    @Test
    public void testMultipleSignatures() {
        String payload1 = "{\"id\":1}";
        String payload2 = "{\"id\":2}";

        String signed1 = jwsSigningService.signPayload(payload1);
        String signed2 = jwsSigningService.signPayload(payload2);

        assertNotEquals(signed1, signed2, "Different payloads should produce different signatures");
    }
}
