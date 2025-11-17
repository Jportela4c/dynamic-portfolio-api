package com.portfolio.api.controller;

import com.portfolio.api.model.dto.response.RiskProfileResponse;
import com.portfolio.api.service.RiskProfileService;
import com.portfolio.api.service.TelemetryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Perfil de Risco", description = "Endpoints para consulta de perfil de risco de clientes")
public class RiskProfileController {

    private final RiskProfileService riskProfileService;
    private final TelemetryService telemetryService;

    public RiskProfileController(RiskProfileService riskProfileService,
                                 TelemetryService telemetryService) {
        this.riskProfileService = riskProfileService;
        this.telemetryService = telemetryService;
    }

    @Operation(
        summary = "Consultar perfil de risco",
        description = "Calcula e retorna o perfil de risco de um cliente baseado no histórico de investimentos"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil calculado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RiskProfileResponse.class))),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
            content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/perfil-risco/{clienteId}")
    public ResponseEntity<RiskProfileResponse> getRiskProfile(
        @Parameter(description = "ID do cliente", example = "123", required = true)
        @PathVariable Long clienteId) {
        long startTime = System.currentTimeMillis();

        try {
            RiskProfileResponse response = riskProfileService.calculateRiskProfile(clienteId);
            long responseTime = System.currentTimeMillis() - startTime;
            telemetryService.recordMetric("perfil-risco", responseTime, true, 200);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            telemetryService.recordMetric("perfil-risco", responseTime, false, 500);
            throw e;
        }
    }
}
