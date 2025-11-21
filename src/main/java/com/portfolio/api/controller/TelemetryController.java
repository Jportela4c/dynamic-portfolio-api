package com.portfolio.api.controller;

import com.portfolio.api.model.dto.response.TelemetryResponse;
import com.portfolio.api.service.PrometheusMetricsService;
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

@RestController
@Tag(name = "Telemetria", description = "Endpoints para consulta de métricas e telemetria dos serviços")
public class TelemetryController {

    private final PrometheusMetricsService prometheusMetricsService;

    public TelemetryController(PrometheusMetricsService prometheusMetricsService) {
        this.prometheusMetricsService = prometheusMetricsService;
    }

    @Operation(
        summary = "Consultar telemetria dos serviços",
        description = """
            Retorna métricas de performance dos serviços, incluindo quantidade de chamadas e tempo médio de resposta.

            <details>
            <summary><strong>Métricas Coletadas</strong> - Prometheus/Micrometer</summary>

            ### Serviços Monitorados:
            - **simular-investimento**: POST /simular-investimento
            - **perfil-risco**: GET /perfil-risco/{clienteId}
            - **investimentos**: GET /investimentos/{clienteId}
            - **simulacoes**: GET /simulacoes
            - **produtos-recomendados**: GET /produtos-recomendados/{perfil}

            ### Métricas por Serviço:
            - **quantidadeChamadas**: Total de requisições no período
            - **mediaTempoRespostaMs**: Latência média em milissegundos

            ### Fonte:
            - Dados coletados via Micrometer/Prometheus
            - Período configurável (padrão: últimos 30 dias)

            </details>
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Telemetria obtida com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TelemetryResponse.class)))
    })
    @GetMapping("/telemetria")
    public ResponseEntity<TelemetryResponse> getTelemetry() {
        TelemetryResponse response = prometheusMetricsService.getTelemetry();
        return ResponseEntity.ok(response);
    }
}
