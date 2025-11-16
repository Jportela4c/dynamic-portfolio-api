package com.portfolio.api.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyAggregationResponse {

    private String produto;
    private LocalDate data;
    private Long quantidadeSimulacoes;
    private BigDecimal mediaValorFinal;
}
