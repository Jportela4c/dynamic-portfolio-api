package com.ofb.mock.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ofb.mock.security.JWSSigningService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.server.ServerResponseFilter;

@Slf4j
@ApplicationScoped
public class JWSResponseFilter {

    @Inject
    JWSSigningService jwsSigningService;

    @Inject
    ObjectMapper objectMapper;

    @ServerResponseFilter
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {

        String path = requestContext.getUriInfo().getPath();
        int status = responseContext.getStatus();
        log.info("JWSResponseFilter invoked - path: {}, status: {}", path, status);

        // Only sign successful OFB API responses (not OAuth2 endpoints)
        if (status == 200 && path.startsWith("/open-banking/")) {

            Object entity = responseContext.getEntity();
            log.info("Entity type: {}", entity != null ? entity.getClass().getName() : "null");

            if (entity != null) {
                try {
                    // Serialize entity to JSON
                    String jsonPayload = objectMapper.writeValueAsString(entity);
                    log.debug("JSON payload ({} bytes): {}", jsonPayload.length(),
                        jsonPayload.substring(0, Math.min(200, jsonPayload.length())));

                    // Sign the JSON payload
                    String signed = jwsSigningService.signPayload(jsonPayload);
                    responseContext.setEntity(signed);
                    responseContext.getHeaders().putSingle("Content-Type", "application/jose");
                    log.info("Response signed with JWS for path: {}", path);
                } catch (Exception e) {
                    log.error("Failed to sign response", e);
                }
            }
        } else {
            log.info("Filter skipped - path: {}, status: {}", path, status);
        }
    }
}
