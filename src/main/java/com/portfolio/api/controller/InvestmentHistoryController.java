package com.portfolio.api.controller;

import com.portfolio.api.model.dto.response.InvestmentResponse;
import com.portfolio.api.service.AuthorizationValidator;
import com.portfolio.api.service.InvestmentService;
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

import java.util.List;

@RestController
@Validated
@Tag(name = "Histórico de Investimentos", description = "Endpoints para consulta do histórico de investimentos dos clientes")
public class InvestmentHistoryController {

    private final InvestmentService investmentService;
    private final AuthorizationValidator authorizationValidator;

    public InvestmentHistoryController(
            InvestmentService investmentService,
            AuthorizationValidator authorizationValidator) {
        this.investmentService = investmentService;
        this.authorizationValidator = authorizationValidator;
    }

    @Operation(
        summary = "Consultar histórico de investimentos",
        description = "Retorna o histórico completo de investimentos realizados por um cliente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Histórico encontrado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = InvestmentResponse.class))),
        @ApiResponse(responseCode = "400", description = "ID do cliente inválido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.portfolio.api.model.dto.response.ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado - usuário não autorizado a acessar dados deste cliente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.portfolio.api.model.dto.response.ErrorResponse.class)))
    })
    @GetMapping("/investimentos/{clienteId}")
    public ResponseEntity<List<InvestmentResponse>> getInvestmentHistory(
        @Parameter(description = "ID do cliente", example = "123", required = true)
        @Positive(message = "Invalid customer ID")
        @PathVariable Long clienteId,
        Authentication authentication) {

        // Validate authorization
        if (!authorizationValidator.canAccessCustomer(authentication, clienteId)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Access denied: You are not authorized to access this customer's data"
            );
        }

        List<InvestmentResponse> response = investmentService.getClientInvestments(clienteId);
        return ResponseEntity.ok(response);
    }
}
