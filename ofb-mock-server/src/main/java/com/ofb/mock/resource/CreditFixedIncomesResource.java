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

import java.util.List;
import java.util.Map;

@Slf4j
@Path("/open-banking/credit-fixed-incomes/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "OFB - Credit Fixed Incomes", description = "Renda fixa crédito privado (Debêntures, CRI, CRA)")
public class CreditFixedIncomesResource {

    @Inject
    MockDataService mockDataService;

    @GET
    @Path("/investments")
    @Operation(summary = "List credit fixed incomes", description = "Returns list of Debêntures, CRI, CRA investments")
    public Response getInvestments(@HeaderParam("Authorization") String authorization) {
        log.info("OFB API: GET /open-banking/credit-fixed-incomes/v1/investments");

        String cpf = JwtUtils.extractCpf(authorization);
        if (cpf == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "invalid_token"))
                .build();
        }

        List<Map<String, Object>> investments = mockDataService.getCreditFixedIncomesByCpf(cpf);
        log.debug("Returning {} credit fixed incomes for CPF {}", investments.size(), cpf);

        return Response.ok(Map.of("data", investments)).build();
    }

    @GET
    @Path("/investments/{investmentId}/transactions")
    @Operation(summary = "Get credit fixed income transactions", description = "Returns transaction history for a specific credit fixed income")
    public Response getInvestmentTransactions(
            @PathParam("investmentId") String investmentId,
            @HeaderParam("Authorization") String authorization) {

        log.info("OFB API: GET /open-banking/credit-fixed-incomes/v1/investments/{}/transactions", investmentId);

        String cpf = JwtUtils.extractCpf(authorization);
        if (cpf == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "invalid_token"))
                .build();
        }

        List<Map<String, Object>> transactions = mockDataService.getTransactionsByInvestmentId(cpf, investmentId);
        log.debug("Returning {} transactions for investment {} (CPF {})", transactions.size(), investmentId, cpf);

        return Response.ok(Map.of(
            "data", transactions,
            "meta", Map.of("totalRecords", transactions.size(), "totalPages", 1)
        )).build();
    }
}
