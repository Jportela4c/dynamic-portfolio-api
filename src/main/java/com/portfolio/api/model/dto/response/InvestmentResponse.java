package com.portfolio.api.model.dto.response;

import com.portfolio.api.model.enums.TipoProduto;
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
    private TipoProduto tipo;
    private BigDecimal valor;
    private BigDecimal rentabilidade;
    private LocalDate data;
}
