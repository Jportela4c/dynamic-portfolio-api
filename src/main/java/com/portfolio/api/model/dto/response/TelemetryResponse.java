package com.portfolio.api.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Telemetria dos serviços da API")
public class TelemetryResponse {

    @Schema(description = "Métricas de cada serviço")
    private List<ServiceMetrics> servicos;

    @Schema(description = "Período de coleta das métricas")
    private Period periodo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Métricas de um serviço específico")
    public static class ServiceMetrics {
        @Schema(description = "Nome do serviço", example = "SimulationService")
        private String nome;

        @Schema(description = "Quantidade total de chamadas", example = "150")
        private Long quantidadeChamadas;

        @Schema(description = "Tempo médio de resposta em milissegundos", example = "45")
        private Long mediaTempoRespostaMs;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Período de tempo analisado")
    public static class Period {
        @Schema(description = "Data de início", example = "2025-01-01")
        private LocalDate inicio;

        @Schema(description = "Data de fim", example = "2025-01-31")
        private LocalDate fim;
    }
}
