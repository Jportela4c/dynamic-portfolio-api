package com.ofb.mock.filter;

import com.ofb.mock.security.JWSSigningService;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@Provider
public class JWSResponseFilter implements ContainerResponseFilter {

    @Inject
    JWSSigningService jwsSigningService;

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {

        // Only sign successful API responses (not OAuth2 endpoints)
        if (responseContext.getStatus() == 200 &&
            requestContext.getUriInfo().getPath().startsWith("api/")) {

            Object entity = responseContext.getEntity();
            if (entity != null) {
                String payload = entity.toString();
                String signed = jwsSigningService.signPayload(payload);
                responseContext.setEntity(signed);
                responseContext.getHeaders().putSingle("Content-Type", "application/jose");
                log.debug("Response signed with JWS for path: {}", requestContext.getUriInfo().getPath());
            }
        }
    }
}
