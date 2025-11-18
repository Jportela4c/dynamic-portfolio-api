package com.ofb.mock.resource;

import com.ofb.mock.security.OAuth2Service;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Path("/oauth2")
@Produces(MediaType.APPLICATION_JSON)
public class OAuth2Resource {

    @Inject
    OAuth2Service oauth2Service;

    @POST
    @Path("/par")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response pushedAuthorizationRequest(
            @FormParam("client_id") String clientId,
            @FormParam("scope") String scope,
            @FormParam("redirect_uri") String redirectUri,
            @FormParam("response_type") String responseType) {

        log.info("PAR request - clientId: {}, scope: {}", clientId, scope);

        if (clientId == null || scope == null || redirectUri == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "invalid_request"))
                    .build();
        }

        String requestUri = oauth2Service.createPushedAuthRequest(clientId, scope, redirectUri);

        Map<String, Object> response = new HashMap<>();
        response.put("request_uri", requestUri);
        response.put("expires_in", 90);

        return Response.ok(response).build();
    }

    @GET
    @Path("/authorize")
    public Response authorize(
            @QueryParam("request_uri") String requestUri,
            @QueryParam("client_id") String clientId) {

        log.info("Authorization request - requestUri: {}, clientId: {}", requestUri, clientId);

        OAuth2Service.PushedAuthRequest par = oauth2Service.getPushedAuthRequest(requestUri);
        if (par == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "invalid_request_uri"))
                    .build();
        }

        if (!par.getClientId().equals(clientId)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "invalid_client"))
                    .build();
        }

        String authCode = oauth2Service.createAuthorizationCode(requestUri);
        String redirectUri = par.getRedirectUri() + "?code=" + authCode;

        return Response.seeOther(URI.create(redirectUri)).build();
    }

    @POST
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response token(
            @FormParam("grant_type") String grantType,
            @FormParam("code") String code,
            @FormParam("redirect_uri") String redirectUri,
            @FormParam("client_id") String clientId) {

        log.info("Token request - grantType: {}, clientId: {}", grantType, clientId);

        if (!"authorization_code".equals(grantType)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "unsupported_grant_type"))
                    .build();
        }

        if (code == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "invalid_request"))
                    .build();
        }

        try {
            String accessToken = oauth2Service.createAccessToken(code);
            String idToken = oauth2Service.createIdToken(clientId);

            Map<String, Object> response = new HashMap<>();
            response.put("access_token", accessToken);
            response.put("token_type", "Bearer");
            response.put("expires_in", 3600);
            response.put("id_token", idToken);

            return Response.ok(response).build();
        } catch (Exception e) {
            log.error("Failed to create token", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "invalid_grant"))
                    .build();
        }
    }

    @GET
    @Path("/.well-known/openid-configuration")
    public Response openidConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("issuer", "https://localhost:8443");
        config.put("authorization_endpoint", "https://localhost:8443/oauth2/authorize");
        config.put("token_endpoint", "https://localhost:8443/oauth2/token");
        config.put("pushed_authorization_request_endpoint", "https://localhost:8443/oauth2/par");
        config.put("jwks_uri", "https://localhost:8443/oauth2/jwks");
        config.put("response_types_supported", new String[]{"code"});
        config.put("grant_types_supported", new String[]{"authorization_code"});
        config.put("token_endpoint_auth_methods_supported", new String[]{"tls_client_auth"});

        return Response.ok(config).build();
    }

    @GET
    @Path("/jwks")
    public Response jwks() {
        Map<String, Object> jwks = new HashMap<>();
        jwks.put("keys", new Object[]{
                oauth2Service.getSigningPublicKey().toJSONObject(),
                oauth2Service.getEncryptionPublicKey().toJSONObject()
        });

        return Response.ok(jwks).build();
    }
}
