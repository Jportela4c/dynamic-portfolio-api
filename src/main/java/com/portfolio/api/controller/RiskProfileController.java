package com.portfolio.api.controller;

import com.portfolio.api.model.dto.response.RiskProfileResponse;
import com.portfolio.api.service.AuthorizationValidator;
import com.portfolio.api.service.RiskProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Validated
@Tag(name = "Perfil de Risco", description = "Endpoints para consulta de perfil de risco de clientes")
public class RiskProfileController {

    private final RiskProfileService riskProfileService;
    private final AuthorizationValidator authorizationValidator;

    public RiskProfileController(
            RiskProfileService riskProfileService,
            AuthorizationValidator authorizationValidator) {
        this.riskProfileService = riskProfileService;
        this.authorizationValidator = authorizationValidator;
    }

    @Operation(
        summary = "Consultar perfil de risco",
        description = "Calcula e retorna o perfil de risco de um cliente baseado no histórico de investimentos"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil calculado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RiskProfileResponse.class))),
        @ApiResponse(responseCode = "400", description = "ID do cliente inválido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.portfolio.api.model.dto.response.ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado - usuário não autorizado a acessar dados deste cliente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.portfolio.api.model.dto.response.ErrorResponse.class)))
    })
    @GetMapping("/perfil-risco/{clienteId}")
    public ResponseEntity<RiskProfileResponse> getRiskProfile(
        @Parameter(description = "ID do cliente", example = "123", required = true)
        @PathVariable Long clienteId,
        Authentication authentication) {

        // Validate authorization
        if (!authorizationValidator.canAccessCustomer(authentication, clienteId)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Access denied: You are not authorized to access this customer's data"
            );
        }

        RiskProfileResponse response = riskProfileService.calculateRiskProfile(clienteId);
        return ResponseEntity.ok(response);
    }
}
