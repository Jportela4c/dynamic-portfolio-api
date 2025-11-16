package com.portfolio.api.model.dto.response;

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
public class TelemetryResponse {

    private List<ServiceMetrics> servicos;
    private Period periodo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ServiceMetrics {
        private String nome;
        private Long quantidadeChamadas;
        private Long mediaTempoRespostaMs;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Period {
        private LocalDate inicio;
        private LocalDate fim;
    }
}
