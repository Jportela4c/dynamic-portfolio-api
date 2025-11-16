package com.portfolio.api.model.dto.response;

import com.fasterxml.jackson.annotation.format.DateTimeFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationResponse {

    private ProductValidated produtoValidado;
    private SimulationResult resultadoSimulacao;
    private LocalDateTime dataSimulacao;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductValidated {
        private Long id;
        private String nome;
        private String tipo;
        private BigDecimal rentabilidade;
        private String risco;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SimulationResult {
        private BigDecimal valorFinal;
        private BigDecimal rentabilidadeEfetiva;
        private Integer prazoMeses;
    }
}
