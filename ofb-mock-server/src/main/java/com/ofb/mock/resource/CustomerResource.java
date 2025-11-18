package com.ofb.mock.resource;

import com.ofb.mock.model.Customer;
import com.ofb.mock.service.MockDataService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Path("/api/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {

    @Inject
    MockDataService mockDataService;

    @GET
    @Path("/{cpf}")
    public Response getCustomerByCpf(@PathParam("cpf") String cpf) {
        log.info("Fetching customer for CPF: {}", cpf);

        Customer customer = mockDataService.getCustomerByCpf(cpf);
        if (customer == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Customer not found"))
                    .build();
        }

        return Response.ok(Map.of("data", customer)).build();
    }
}
