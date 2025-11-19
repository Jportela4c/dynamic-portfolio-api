package com.ofb.mock.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.ofb.mock.model.Investment;
import com.ofb.mock.security.OAuth2Service;
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

    @Inject
    OAuth2Service oauth2Service;

    @Inject
    ObjectMapper objectMapper;

    @GET
    @Path("/investments")
    public Response getInvestments(@HeaderParam("Authorization") String authorization) {
        log.info("OFB API: GET /open-banking/bank-fixed-incomes/v1/investments");

        try {
            // Mock implementation - returns all investments
            List<Investment> investments = mockDataService.getAllInvestments();

            // Create OFB-compliant response structure
            Map<String, Object> responseData = Map.of("data", investments);
            String jsonPayload = objectMapper.writeValueAsString(responseData);

            // Sign response as JWS (FAPI compliant)
            JWSSigner signer = new RSASSASigner(oauth2Service.getSigningKey());
            com.nimbusds.jose.JWSObject jwsObject = new com.nimbusds.jose.JWSObject(
                new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(oauth2Service.getSigningKey().getKeyID())
                    .build(),
                new Payload(jsonPayload)
            );
            jwsObject.sign(signer);

            log.debug("Returning JWS-signed investment response");
            return Response.ok(jwsObject.serialize()).build();
        } catch (Exception e) {
            log.error("Failed to sign investment response", e);
            return Response.serverError().build();
        }
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
