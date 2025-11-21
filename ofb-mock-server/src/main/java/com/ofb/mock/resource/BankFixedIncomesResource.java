package com.ofb.mock.resource;

import com.ofb.api.model.bankfixedincome.*;
import com.ofb.mock.service.MockDataService;
import com.ofb.mock.util.JwtUtils;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Slf4j
@Path("/open-banking/bank-fixed-incomes/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "OFB - Bank Fixed Incomes", description = "Investimentos em renda fixa bancária (CDB, LCI, LCA, RDB)")
public class BankFixedIncomesResource {

    @Inject
    MockDataService mockDataService;

    @GET
    @Path("/investments")
    @Operation(
        summary = "List bank fixed income investments",
        description = "Returns list of CDB, LCI, LCA, RDB investments (4 fields: brandName, companyCnpj, investmentType, investmentId)"
    )
    public Response getInvestments(@HeaderParam("Authorization") String authorization) {
        log.info("OFB API: GET /open-banking/bank-fixed-incomes/v1/investments");

        String cpf = JwtUtils.extractCpf(authorization);
        if (cpf == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "invalid_token"))
                .build();
        }

        // Get list data (4 fields only)
        List<ResponseBankFixedIncomesProductListDataInner> investments =
            mockDataService.getBankFixedIncomesList(cpf);

        log.debug("Returning {} bank fixed income investments for CPF {}", investments.size(), cpf);

        // Build OFB-compliant response
        ResponseBankFixedIncomesProductList response = new ResponseBankFixedIncomesProductList();
        response.setData(investments);

        // Add links (mock)
        BankFixedIncomeProductListLinks links = new BankFixedIncomeProductListLinks();
        links.setSelf(URI.create("https://localhost:8089/open-banking/bank-fixed-incomes/v1/investments"));
        response.setLinks(links);

        // Add meta (mock)
        BankFixedIncomesMeta meta = new BankFixedIncomesMeta();
        meta.setTotalRecords(investments.size());
        meta.setTotalPages(1);
        response.setMeta(meta);

        return Response.ok(response).build();
    }

    @GET
    @Path("/investments/{investmentId}")
    @Operation(
        summary = "Get investment details",
        description = "Returns complete details for a specific bank fixed income investment (15+ fields)"
    )
    public Response getInvestmentDetails(
            @PathParam("investmentId") String investmentId,
            @HeaderParam("Authorization") String authorization) {

        log.info("OFB API: GET /open-banking/bank-fixed-incomes/v1/investments/{}", investmentId);

        String cpf = JwtUtils.extractCpf(authorization);
        if (cpf == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "invalid_token"))
                .build();
        }

        Map<String, Object> investment = mockDataService.getBankFixedIncomeDetails(cpf, investmentId);
        if (investment == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", "investment_not_found"))
                .build();
        }

        log.debug("Returning details for investment {} (CPF {})", investmentId, cpf);

        // Return details structure (keep as Map for now - contains all 15+ fields)
        // TODO: Convert to ResponseBankFixedIncomesProductIdentification when needed
        return Response.ok(Map.of("data", investment)).build();
    }

    @GET
    @Path("/investments/{investmentId}/transactions")
    @Operation(
        summary = "Get investment transactions",
        description = "Returns transaction history for a specific bank fixed income investment"
    )
    public Response getInvestmentTransactions(
            @PathParam("investmentId") String investmentId,
            @HeaderParam("Authorization") String authorization) {

        log.info("OFB API: GET /open-banking/bank-fixed-incomes/v1/investments/{}/transactions", investmentId);

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
            "meta", Map.of(
                "totalRecords", transactions.size(),
                "totalPages", 1
            )
        )).build();
    }
}
