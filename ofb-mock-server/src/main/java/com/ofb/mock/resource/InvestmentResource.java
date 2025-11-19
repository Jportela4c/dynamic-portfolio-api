package com.ofb.mock.resource;

import com.ofb.mock.model.Investment;
import com.ofb.mock.service.MockDataService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
@Path("/open-banking/bank-fixed-incomes/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InvestmentResource {

    @Inject
    MockDataService mockDataService;

    @GET
    @Path("/investments")
    public Response getInvestments(@HeaderParam("Authorization") String authorization) {
        log.info("OFB API: GET /open-banking/bank-fixed-incomes/v1/investments");

        // Mock implementation - returns all investments
        List<Investment> investments = mockDataService.getAllInvestments();

        // Return OFB-compliant response structure
        return Response.ok(Map.of("data", investments)).build();
    }

    @GET
    @Path("/investments/{investmentId}")
    public Response getInvestmentById(@PathParam("investmentId") String investmentId,
                                      @HeaderParam("Authorization") String authorization) {
        log.info("OFB API: GET /open-banking/bank-fixed-incomes/v1/investments/{}", investmentId);

        Investment investment = mockDataService.getInvestmentById(investmentId);

        if (investment == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("errors", List.of(Map.of(
                        "code", "NOT_FOUND",
                        "title", "Investment not found",
                        "detail", "Investment with id " + investmentId + " not found"
                    ))))
                    .build();
        }

        return Response.ok(Map.of("data", investment)).build();
    }
}
