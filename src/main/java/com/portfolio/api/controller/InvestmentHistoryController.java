package com.portfolio.api.controller;

import com.portfolio.api.model.dto.response.InvestmentResponse;
import com.portfolio.api.service.InvestmentService;
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

import java.util.List;

@RestController
@Tag(name = "Histórico de Investimentos", description = "Endpoints para consulta do histórico de investimentos dos clientes")
public class InvestmentHistoryController {

    private final InvestmentService investmentService;
    private final TelemetryService telemetryService;

    public InvestmentHistoryController(InvestmentService investmentService,
                                       TelemetryService telemetryService) {
        this.investmentService = investmentService;
        this.telemetryService = telemetryService;
    }

    @Operation(
        summary = "Consultar histórico de investimentos",
        description = "Retorna o histórico completo de investimentos realizados por um cliente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Histórico encontrado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = InvestmentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
            content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/investimentos/{clienteId}")
    public ResponseEntity<List<InvestmentResponse>> getInvestmentHistory(
        @Parameter(description = "ID do cliente", example = "123", required = true)
        @PathVariable Long clienteId) {
        long startTime = System.currentTimeMillis();

        try {
            List<InvestmentResponse> response = investmentService.getClientInvestments(clienteId);
            long responseTime = System.currentTimeMillis() - startTime;
            telemetryService.recordMetric("investimentos", responseTime, true, 200);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            telemetryService.recordMetric("investimentos", responseTime, false, 500);
            throw e;
        }
    }
}
