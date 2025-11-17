package com.portfolio.api.controller;

import com.portfolio.api.model.dto.response.RiskProfileResponse;
import com.portfolio.api.service.RiskProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@Tag(name = "Perfil de Risco", description = "Endpoints para consulta de perfil de risco de clientes")
public class RiskProfileController {

    private final RiskProfileService riskProfileService;

    public RiskProfileController(RiskProfileService riskProfileService) {
        this.riskProfileService = riskProfileService;
    }

    @Operation(
        summary = "Consultar perfil de risco",
        description = "Calcula e retorna o perfil de risco de um cliente baseado no histórico de investimentos"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil calculado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RiskProfileResponse.class))),
        @ApiResponse(responseCode = "400", description = "ID do cliente inválido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.portfolio.api.model.dto.response.ErrorResponse.class)))
    })
    @GetMapping("/perfil-risco/{clienteId}")
    public ResponseEntity<RiskProfileResponse> getRiskProfile(
        @Parameter(description = "ID do cliente", example = "123", required = true)
        @Positive(message = "Invalid customer ID")
        @PathVariable Long clienteId) {
        RiskProfileResponse response = riskProfileService.calculateRiskProfile(clienteId);
        return ResponseEntity.ok(response);
    }
}
