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
import org.springframework.security.access.prepost.PreAuthorize;
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
        summary = "Listar simulações do usuário",
        description = """
            Retorna o histórico de simulações realizadas pelo cliente autenticado.

            <details>
            <summary><strong>Dados Retornados</strong> - Histórico de simulações</summary>

            ### Campos de cada simulação:
            - **id**: Identificador único da simulação
            - **produtoNome**: Nome do produto simulado
            - **tipoProduto**: Tipo (CDB, LCI, LCA, etc.)
            - **valorInvestido**: Valor inicial da simulação
            - **valorFinal**: Valor calculado ao final do prazo
            - **prazoMeses**: Prazo em meses
            - **dataSimulacao**: Data/hora da simulação

            ### Ordenação:
            - Mais recentes primeiro (ORDER BY dataSimulacao DESC)

            </details>
            """
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
        description = """
            Retorna agregação de simulações por produto e dia para o cliente autenticado.

            <details>
            <summary><strong>Formato da Agregação</strong> - Agrupamento por produto e data</summary>

            ### Campos de cada agregação:
            - **data**: Data das simulações (formato: YYYY-MM-DD)
            - **tipoProduto**: Tipo do produto (CDB, LCI, etc.)
            - **quantidadeSimulacoes**: Total de simulações naquele dia/produto
            - **valorTotalSimulado**: Soma dos valores simulados
            - **valorMedioSimulado**: Média dos valores simulados

            ### Uso:
            - Dashboard de análise de comportamento
            - Identificação de padrões de simulação
            - Relatórios gerenciais

            </details>
            """
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
