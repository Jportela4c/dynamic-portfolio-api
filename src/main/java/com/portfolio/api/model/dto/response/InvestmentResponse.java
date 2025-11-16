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
public class InvestmentResponse {

    private Long id;
    private String tipo;
    private BigDecimal valor;
    private BigDecimal rentabilidade;
    private LocalDate data;
}
