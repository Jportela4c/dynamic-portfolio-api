package com.ofb.mock.resource;

import com.ofb.api.model.variableincome.ResponseVariableIncomesProductIdentificationData;
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
@Path("/open-banking/variable-incomes/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "OFB - Variable Incomes", description = "Renda variável (Ações, ETFs)")
public class VariableIncomesResource {

    @Inject
    MockDataService mockDataService;

    @GET
    @Path("/investments")
    @Operation(summary = "List variable incomes", description = "Returns list of stocks and ETFs")
    public Response getInvestments(@HeaderParam("Authorization") String authorization,
            @HeaderParam("x-customer-cpf") String fallbackCpf) { // TEMP: for testing with auth disabled
        log.info("OFB API: GET /open-banking/variable-incomes/v1/investments");

        // TEMP: Extract CPF from JWT with header fallback (easy rollback - just uncomment line below)
        // String cpf = "96846726756"; // TEMP: hardcoded for testing
        String cpf = JwtUtils.extractCpf(authorization, fallbackCpf);
        if (cpf == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "missing_cpf"))
                .build();
        }

        List<Map<String, Object>> investments = mockDataService.getVariableIncomesByCpf(cpf);
        log.debug("Returning {} variable incomes for CPF {}", investments.size(), cpf);

        return Response.ok(Map.of("data", investments)).build();
    }

    @GET
    @Path("/investments/{investmentId}")
    @Operation(summary = "Get investment details", description = "Returns complete details for a specific stock or ETF")
    public Response getInvestmentDetails(
            @PathParam("investmentId") String investmentId,
            @HeaderParam("Authorization") String authorization,
            @HeaderParam("x-customer-cpf") String fallbackCpf) { // TEMP: for testing with auth disabled

        log.info("OFB API: GET /open-banking/variable-incomes/v1/investments/{}", investmentId);

        // TEMP: Extract CPF from JWT with header fallback (easy rollback - just uncomment line below)
        // String cpf = "96846726756"; // TEMP: hardcoded for testing
        String cpf = JwtUtils.extractCpf(authorization, fallbackCpf);
        if (cpf == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "missing_cpf"))
                .build();
        }

        ResponseVariableIncomesProductIdentificationData investment = mockDataService.getVariableIncomeDetails(cpf, investmentId);
        if (investment == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", "investment_not_found"))
                .build();
        }

        log.debug("Returning details for investment {} (CPF {})", investmentId, cpf);
        return Response.ok(Map.of("data", investment)).build();
    }

    @GET
    @Path("/investments/{investmentId}/transactions")
    @Operation(summary = "Get variable income transactions", description = "Returns transaction history for a specific stock or ETF")
    public Response getInvestmentTransactions(
            @PathParam("investmentId") String investmentId,
            @HeaderParam("Authorization") String authorization,
            @HeaderParam("x-customer-cpf") String fallbackCpf) { // TEMP: for testing with auth disabled

        log.info("OFB API: GET /open-banking/variable-incomes/v1/investments/{}/transactions", investmentId);

        // TEMP: Extract CPF from JWT with header fallback (easy rollback - just uncomment line below)
        // String cpf = "96846726756"; // TEMP: hardcoded for testing
        String cpf = JwtUtils.extractCpf(authorization, fallbackCpf);
        if (cpf == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "missing_cpf"))
                .build();
        }

        List<Map<String, Object>> transactions = mockDataService.getTransactionsByInvestmentId(cpf, investmentId);
        log.debug("Returning {} transactions for investment {} (CPF {})", transactions.size(), investmentId, cpf);

        return Response.ok(Map.of(
            "data", transactions,
            "meta", Map.of("totalRecords", transactions.size(), "totalPages", 1)
        )).build();
    }

    @GET
    @Path("/investments/{investmentId}/balances")
    @Operation(summary = "Get variable income balance", description = "Returns current balance for a specific variable income")
    public Response getInvestmentBalances(
            @PathParam("investmentId") String investmentId,
            @HeaderParam("Authorization") String authorization,
            @HeaderParam("x-customer-cpf") String fallbackCpf) {

        log.info("OFB API: GET /open-banking/variable-incomes/v1/investments/{}/balances", investmentId);

        String cpf = JwtUtils.extractCpf(authorization, fallbackCpf);
        if (cpf == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "missing_cpf"))
                .build();
        }

        Map<String, Object> balanceData = mockDataService.generateBalanceData(cpf, investmentId);
        log.debug("Returning balance for investment {} (CPF {})", investmentId, cpf);

        return Response.ok(Map.of("data", balanceData)).build();
    }
}
