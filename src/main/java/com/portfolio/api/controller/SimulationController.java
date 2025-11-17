package com.portfolio.api.controller;

import com.portfolio.api.model.dto.response.DailyAggregationResponse;
import com.portfolio.api.model.dto.response.SimulationHistoryResponse;
import com.portfolio.api.service.SimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Simulações", description = "Endpoints para consulta do histórico de simulações")
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @Operation(
        summary = "Listar todas as simulações",
        description = "Retorna o histórico completo de todas as simulações realizadas"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Simulações encontradas com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SimulationHistoryResponse.class)))
    })
    @GetMapping("/simulacoes")
    public ResponseEntity<List<SimulationHistoryResponse>> getAllSimulations() {
        List<SimulationHistoryResponse> response = simulationService.getAllSimulations();
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Agregação diária de simulações",
        description = "Retorna agregação de simulações por produto e dia, incluindo quantidade e média de valor final"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Agregações encontradas com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DailyAggregationResponse.class)))
    })
    @GetMapping("/simulacoes/por-produto-dia")
    public ResponseEntity<List<DailyAggregationResponse>> getDailyAggregations() {
        List<DailyAggregationResponse> response = simulationService.getDailyAggregations();
        return ResponseEntity.ok(response);
    }
}
