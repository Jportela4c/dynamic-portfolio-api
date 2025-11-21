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
@Path("/open-banking/treasure-titles/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "OFB - Treasury Titles", description = "Títulos do Tesouro Direto")
public class TreasuryTitlesResource {

    @Inject
    MockDataService mockDataService;

    @GET
    @Path("/investments")
    @Operation(summary = "List treasury titles", description = "Returns list of Tesouro Direto investments")
    public Response getInvestments(@HeaderParam("Authorization") String authorization) {
        log.info("OFB API: GET /open-banking/treasure-titles/v1/investments");

        String cpf = JwtUtils.extractCpf(authorization);
        if (cpf == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "invalid_token"))
                .build();
        }

        List<Map<String, Object>> investments = mockDataService.getTreasuryTitlesByCpf(cpf);
        log.debug("Returning {} treasury titles for CPF {}", investments.size(), cpf);

        return Response.ok(Map.of("data", investments)).build();
    }
}
