package com.portfolio.api.controller;

import com.portfolio.api.service.demo.DemoOFBAuthService;
import com.portfolio.api.service.external.OFBInvestmentDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Profile("dev")
@RequiredArgsConstructor
@Tag(name = "Perfil de Risco", description = "Endpoints para consulta de perfil de risco de clientes")
public class DemoRiskProfileController {

    private final DemoOFBAuthService authService;
    private final OFBInvestmentDataService investmentDataService;

    @Operation(
        summary = "Consultar perfil de risco (DEV)",
        description = "Calcula perfil de risco usando dados do mock OFB. " +
                     "Aceita clientId: portfolio-api-conservative, portfolio-api-moderate, portfolio-api-aggressive"
    )
    @GetMapping("/perfil-risco/{clientId}")
    public ResponseEntity<?> getRiskProfile(
        @Parameter(description = "OAuth2 Client ID", example = "portfolio-api-conservative", required = true)
        @PathVariable String clientId) throws Exception {

        log.info("DEV: Risk profile request for clientId: {}", clientId);

        // Get token from mock server
        String accessToken = authService.getTokenForClient(clientId);

        // Fetch investments
        var investments = investmentDataService.fetchInvestments(accessToken);

        // Return data
        return ResponseEntity.ok(investments);
    }
}
