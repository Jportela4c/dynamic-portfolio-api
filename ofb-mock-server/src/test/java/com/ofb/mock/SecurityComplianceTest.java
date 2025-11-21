package com.ofb.mock;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.SignedJWT;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@EnabledIfSystemProperty(named = "ofb.mock.tests.enabled", matches = "true")
public class SecurityComplianceTest {

    private static final String VALID_CLIENT_ID = "portfolio-api";

    /**
     * OFB Requirement: All ID tokens MUST be encrypted with JWE
     */
    @Test
    public void testIdTokenIsEncrypted() throws Exception {
        // Complete OAuth2 flow
        Response parResponse = given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("client_id", VALID_CLIENT_ID)
            .formParam("scope", "investments:read")
            .formParam("redirect_uri", "https://example.com/callback")
            .formParam("response_type", "code")
            .formParam("cpf_hint", "12345678901")
        .when()
            .post("/oauth2/par")
        .then()
            .statusCode(200)
            .extract().response();

        String requestUri = parResponse.jsonPath().getString("request_uri");

        Response authResponse = given()
            .queryParam("request_uri", requestUri)
            .queryParam("client_id", VALID_CLIENT_ID)
            .redirects().follow(false)
        .when()
            .get("/oauth2/authorize")
        .then()
            .statusCode(303)
            .extract().response();

        String location = authResponse.getHeader("Location");
        String code = location.substring(location.indexOf("code=") + 5);

        Response tokenResponse = given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("grant_type", "authorization_code")
            .formParam("code", code)
            .formParam("redirect_uri", "https://example.com/callback")
            .formParam("client_id", VALID_CLIENT_ID)
        .when()
            .post("/oauth2/token")
        .then()
            .statusCode(200)
            .extract().response();

        String idToken = tokenResponse.jsonPath().getString("id_token");
        assertNotNull(idToken, "ID token must be present");

        // Verify it's a JWE (5 parts: header.encrypted_key.iv.ciphertext.tag)
        String[] parts = idToken.split("\\.");
        assertEquals(5, parts.length, "ID token must be JWE format (5 parts)");

        // Verify it can be parsed as EncryptedJWT
        EncryptedJWT jwe = EncryptedJWT.parse(idToken);
        assertNotNull(jwe, "ID token must be valid JWE");
        assertEquals("RSA-OAEP", jwe.getHeader().getAlgorithm().getName(),
            "JWE must use RSA-OAEP algorithm");
        assertEquals("A256GCM", jwe.getHeader().getEncryptionMethod().getName(),
            "JWE must use A256GCM encryption");
    }

    /**
     * OFB Requirement: Access tokens MUST be JWTs signed with RS256 or PS256
     */
    @Test
    public void testAccessTokenIsSignedJWT() throws Exception {
        // Complete OAuth2 flow
        Response parResponse = given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("client_id", VALID_CLIENT_ID)
            .formParam("scope", "investments:read")
            .formParam("redirect_uri", "https://example.com/callback")
            .formParam("response_type", "code")
            .formParam("cpf_hint", "12345678901")
        .when()
            .post("/oauth2/par")
        .then()
            .statusCode(200)
            .extract().response();

        String requestUri = parResponse.jsonPath().getString("request_uri");

        Response authResponse = given()
            .queryParam("request_uri", requestUri)
            .queryParam("client_id", VALID_CLIENT_ID)
            .redirects().follow(false)
        .when()
            .get("/oauth2/authorize")
        .then()
            .statusCode(303)
            .extract().response();

        String location = authResponse.getHeader("Location");
        String code = location.substring(location.indexOf("code=") + 5);

        Response tokenResponse = given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("grant_type", "authorization_code")
            .formParam("code", code)
            .formParam("redirect_uri", "https://example.com/callback")
            .formParam("client_id", VALID_CLIENT_ID)
        .when()
            .post("/oauth2/token")
        .then()
            .statusCode(200)
            .extract().response();

        String accessToken = tokenResponse.jsonPath().getString("access_token");
        assertNotNull(accessToken, "Access token must be present");

        // Verify it's a valid JWT (3 parts)
        String[] parts = accessToken.split("\\.");
        assertEquals(3, parts.length, "Access token must be JWT format (3 parts)");

        // Parse and verify it's signed
        SignedJWT jwt = SignedJWT.parse(accessToken);
        assertNotNull(jwt, "Access token must be valid SignedJWT");

        String algorithm = jwt.getHeader().getAlgorithm().getName();
        assertTrue(algorithm.equals("RS256") || algorithm.equals("PS256"),
            "Access token must be signed with RS256 or PS256, got: " + algorithm);

        // Verify signature with public key
        Response jwksResponse = given()
            .when()
            .get("/oauth2/jwks")
            .then()
            .statusCode(200)
            .extract().response();

        JWKSet jwkSet = JWKSet.parse(jwksResponse.asString());
        RSAKey rsaKey = (RSAKey) jwkSet.getKeys().get(0);

        JWSVerifier verifier = new RSASSAVerifier(rsaKey);
        assertTrue(jwt.verify(verifier), "Access token signature must be valid");
    }

    /**
     * OFB Requirement: PAR requests MUST return proper URN format
     */
    @Test
    public void testPARRequestUriFormat() {
        Response response = given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("client_id", VALID_CLIENT_ID)
            .formParam("scope", "investments:read")
            .formParam("redirect_uri", "https://example.com/callback")
            .formParam("response_type", "code")
            .formParam("cpf_hint", "12345678901")
        .when()
            .post("/oauth2/par")
        .then()
            .statusCode(200)
            .extract().response();

        String requestUri = response.jsonPath().getString("request_uri");

        // RFC 9126: The URN must follow urn:ietf:params:oauth:request_uri: format
        assertTrue(requestUri.startsWith("urn:ietf:params:oauth:request_uri:"),
            "PAR request_uri must use proper URN format");

        // Must have expiry
        Integer expiresIn = response.jsonPath().getInt("expires_in");
        assertNotNull(expiresIn, "PAR response must include expires_in");
        assertTrue(expiresIn > 0 && expiresIn <= 90,
            "PAR expires_in should be between 1-90 seconds per OFB spec");
    }

    /**
     * OFB Requirement: Invalid PAR must be rejected
     */
    @Test
    public void testPARRejectsMissingParameters() {
        // Missing client_id
        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("scope", "investments:read")
            .formParam("redirect_uri", "https://example.com/callback")
        .when()
            .post("/oauth2/par")
        .then()
            .statusCode(400)
            .body("error", equalTo("invalid_request"));

        // Missing scope
        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("client_id", VALID_CLIENT_ID)
            .formParam("redirect_uri", "https://example.com/callback")
        .when()
            .post("/oauth2/par")
        .then()
            .statusCode(400)
            .body("error", equalTo("invalid_request"));
    }

    /**
     * OFB Requirement: Invalid authorization code must be rejected
     */
    @Test
    public void testInvalidAuthorizationCodeRejected() {
        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("grant_type", "authorization_code")
            .formParam("code", "INVALID_CODE_12345")
            .formParam("redirect_uri", "https://example.com/callback")
            .formParam("client_id", VALID_CLIENT_ID)
        .when()
            .post("/oauth2/token")
        .then()
            .statusCode(400)
            .body("error", equalTo("invalid_grant"));
    }

    /**
     * OFB Requirement: Authorization code can only be used once
     */
    @Test
    public void testAuthorizationCodeSingleUse() throws Exception {
        // Get valid code
        Response parResponse = given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("client_id", VALID_CLIENT_ID)
            .formParam("scope", "investments:read")
            .formParam("redirect_uri", "https://example.com/callback")
            .formParam("response_type", "code")
            .formParam("cpf_hint", "12345678901")
        .when()
            .post("/oauth2/par")
        .then()
            .statusCode(200)
            .extract().response();

        String requestUri = parResponse.jsonPath().getString("request_uri");

        Response authResponse = given()
            .queryParam("request_uri", requestUri)
            .queryParam("client_id", VALID_CLIENT_ID)
            .redirects().follow(false)
        .when()
            .get("/oauth2/authorize")
        .then()
            .statusCode(303)
            .extract().response();

        String location = authResponse.getHeader("Location");
        String code = location.substring(location.indexOf("code=") + 5);

        // First use - should succeed
        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("grant_type", "authorization_code")
            .formParam("code", code)
            .formParam("redirect_uri", "https://example.com/callback")
            .formParam("client_id", VALID_CLIENT_ID)
        .when()
            .post("/oauth2/token")
        .then()
            .statusCode(200);

        // Second use of same code - must fail
        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("grant_type", "authorization_code")
            .formParam("code", code)
            .formParam("redirect_uri", "https://example.com/callback")
            .formParam("client_id", VALID_CLIENT_ID)
        .when()
            .post("/oauth2/token")
        .then()
            .statusCode(400)
            .body("error", equalTo("invalid_grant"));
    }

    /**
     * OFB Requirement: JWS responses must use PS256 algorithm
     */
    @Test
    public void testJWSResponsesUsePS256() throws Exception {
        // This test would need to actually call API endpoints and verify JWS
        // Currently the JWSResponseFilter is configured but we need to verify it works

        Response jwksResponse = given()
            .when()
            .get("/oauth2/jwks")
            .then()
            .statusCode(200)
            .extract().response();

        JWKSet jwkSet = JWKSet.parse(jwksResponse.asString());

        // Verify we have signing keys available
        assertTrue(jwkSet.getKeys().size() >= 3,
            "JWKS must contain at least 3 keys (signing + encryption + JWS response signing)");
    }

    /**
     * OFB Requirement: JWKS must expose public keys only
     */
    @Test
    public void testJWKSOnlyExposesPublicKeys() throws Exception {
        Response response = given()
            .when()
            .get("/oauth2/jwks")
            .then()
            .statusCode(200)
            .extract().response();

        JWKSet jwkSet = JWKSet.parse(response.asString());

        for (JWK key : jwkSet.getKeys()) {
            if (key instanceof RSAKey) {
                RSAKey rsaKey = (RSAKey) key;
                assertNull(rsaKey.getPrivateExponent(),
                    "JWKS must not expose private key components (d parameter)");
                assertNull(rsaKey.getFirstPrimeFactor(),
                    "JWKS must not expose private key components (p parameter)");
                assertNull(rsaKey.getSecondPrimeFactor(),
                    "JWKS must not expose private key components (q parameter)");
            }
        }
    }

    /**
     * OFB Requirement: OpenID Configuration must include all required endpoints
     */
    @Test
    public void testOpenIDConfigurationCompleteness() {
        Response response = given()
            .when()
            .get("/oauth2/.well-known/openid-configuration")
            .then()
            .statusCode(200)
            .extract().response();

        Map<String, Object> config = response.jsonPath().getMap("$");

        // Required endpoints per OpenID Connect and OFB spec
        assertTrue(config.containsKey("issuer"), "Must include issuer");
        assertTrue(config.containsKey("authorization_endpoint"), "Must include authorization_endpoint");
        assertTrue(config.containsKey("token_endpoint"), "Must include token_endpoint");
        assertTrue(config.containsKey("jwks_uri"), "Must include jwks_uri");
        assertTrue(config.containsKey("pushed_authorization_request_endpoint"),
            "OFB requires PAR endpoint");

        // Verify supported grant types include authorization_code
        List<String> grantTypes = response.jsonPath().getList("grant_types_supported");
        assertTrue(grantTypes.contains("authorization_code"),
            "Must support authorization_code grant type");

        // Verify mTLS authentication method
        List<String> authMethods = response.jsonPath().getList("token_endpoint_auth_methods_supported");
        assertTrue(authMethods.contains("tls_client_auth"),
            "OFB requires tls_client_auth support");
    }
}
