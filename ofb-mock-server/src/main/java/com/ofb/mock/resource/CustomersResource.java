package com.ofb.mock.resource;

import com.ofb.mock.service.MockDataService;
import com.ofb.mock.util.JwtUtils;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Map;

@Slf4j
@Path("/open-banking/customers/v2")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "OFB - Customers", description = "Dados cadastrais de clientes")
public class CustomersResource {

    @Inject
    MockDataService mockDataService;

    @GET
    @Path("/personal/identifications")
    @Operation(summary = "Get personal customer identification", description = "Returns personal identification data")
    public Response getPersonalIdentifications(@HeaderParam("Authorization") String authorization) {
        log.info("OFB API: GET /open-banking/customers/v2/personal/identifications");

        String cpf = JwtUtils.extractCpf(authorization);
        if (cpf == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "invalid_token"))
                .build();
        }

        Object customer = mockDataService.getCustomerByCpf(cpf);
        if (customer == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", "customer_not_found"))
                .build();
        }

        log.debug("Returning customer identification for CPF {}", cpf);
        return Response.ok(Map.of("data", customer)).build();
    }
}
