package com.portfolio.api.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Resumo de movimentações do cliente em um período.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSummary {

    private String cpf;
    private Periodo periodo;
    private Resumo resumo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Periodo {
        private LocalDate dataInicio;
        private LocalDate dataFim;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Resumo {
        private Integer totalMovimentacoes;
        private BigDecimal volumeTotalAplicacoes;
        private BigDecimal volumeTotalResgates;
        private Double frequenciaMediaDias;
        private BigDecimal preferenciaLiquidez;
        private BigDecimal preferenciaProfitabilidade;
    }
}
