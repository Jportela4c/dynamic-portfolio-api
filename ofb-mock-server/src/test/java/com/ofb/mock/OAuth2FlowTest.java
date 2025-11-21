package com.ofb.mock;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
public class OAuth2FlowTest {

    private static final String VALID_CLIENT_ID = "portfolio-api";

    @Test
    public void testPushedAuthorizationRequest() {
        given()
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
            .body("request_uri", startsWith("urn:ietf:params:oauth:request_uri:"))
            .body("expires_in", equalTo(90));
    }

    @Test
    public void testAuthorizationEndpoint() {
        // First create PAR
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

        // Then authorize
        given()
            .queryParam("request_uri", requestUri)
            .queryParam("client_id", VALID_CLIENT_ID)
            .redirects().follow(false)
        .when()
            .get("/oauth2/authorize")
        .then()
            .statusCode(303)
            .header("Location", containsString("https://example.com/callback?code=CODE_"));
    }

    @Test
    public void testFullOAuth2Flow() {
        // Step 1: PAR
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

        // Step 2: Authorize
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

        // Step 3: Token exchange
        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("grant_type", "authorization_code")
            .formParam("code", code)
            .formParam("redirect_uri", "https://example.com/callback")
            .formParam("client_id", VALID_CLIENT_ID)
        .when()
            .post("/oauth2/token")
        .then()
            .statusCode(200)
            .body("access_token", notNullValue())
            .body("token_type", equalTo("Bearer"))
            .body("expires_in", equalTo(3600))
            .body("id_token", notNullValue());
    }

    @Test
    public void testOpenIdConfiguration() {
        given()
        .when()
            .get("/oauth2/.well-known/openid-configuration")
        .then()
            .statusCode(200)
            .body("issuer", equalTo("https://localhost:8443"))
            .body("authorization_endpoint", notNullValue())
            .body("token_endpoint", notNullValue())
            .body("pushed_authorization_request_endpoint", notNullValue())
            .body("jwks_uri", notNullValue());
    }

    @Test
    public void testJwksEndpoint() {
        given()
        .when()
            .get("/oauth2/jwks")
        .then()
            .statusCode(200)
            .body("keys", notNullValue())
            .body("keys.size()", equalTo(3));
    }
}
