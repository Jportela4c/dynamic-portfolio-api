package com.ofb.mock.resource;

import com.ofb.mock.model.Transaction;
import com.ofb.mock.service.MockDataService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
@Path("/api/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionResource {

    @Inject
    MockDataService mockDataService;

    @GET
    @Path("/{cpf}")
    public Response getTransactionsByCpf(@PathParam("cpf") String cpf) {
        log.info("Fetching transactions for CPF: {}", cpf);

        if (!mockDataService.customerExists(cpf)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Customer not found"))
                    .build();
        }

        List<Transaction> transactions = mockDataService.getTransactionsByCpf(cpf);
        return Response.ok(Map.of("data", transactions)).build();
    }
}
