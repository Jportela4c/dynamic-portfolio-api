package com.portfolio.api.controller;

import com.portfolio.api.model.dto.request.SimulationRequest;
import com.portfolio.api.model.dto.response.SimulationResponse;
import com.portfolio.api.service.SimulationService;
import com.portfolio.api.service.TelemetryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Simulação de Investimentos", description = "Endpoints para simular investimentos em produtos financeiros")
public class InvestmentSimulationController {

    private final SimulationService simulationService;
    private final TelemetryService telemetryService;

    public InvestmentSimulationController(SimulationService simulationService,
                                          TelemetryService telemetryService) {
        this.simulationService = simulationService;
        this.telemetryService = telemetryService;
    }

    @Operation(
        summary = "Simular investimento",
        description = "Simula um investimento com base nos parâmetros fornecidos. Valida o produto contra o banco de dados e calcula os retornos esperados."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Simulação realizada com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SimulationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos na requisição",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado para os parâmetros informados",
            content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/simular-investimento")
    public ResponseEntity<SimulationResponse> simulateInvestment(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados da simulação",
            required = true,
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Exemplo CDB",
                    value = """
                        {
                          "clienteId": 123,
                          "valor": 10000.00,
                          "prazoMeses": 12,
                          "tipoProduto": "CDB"
                        }
                        """
                )
            )
        )
        @Valid @RequestBody SimulationRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            SimulationResponse response = simulationService.simulateInvestment(request);
            long responseTime = System.currentTimeMillis() - startTime;
            telemetryService.recordMetric("simular-investimento", responseTime, true, 200);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            telemetryService.recordMetric("simular-investimento", responseTime, false, 500);
            throw e;
        }
    }
}
