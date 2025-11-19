package com.portfolio.api.provider.dto;

import com.portfolio.api.model.enums.TipoProduto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Histórico de investimento (aplicação ou resgate).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Investment {

    private Long id;
    private TipoProduto tipo;
    private String tipoOperacao;
    private BigDecimal valor;
    private BigDecimal rentabilidade;
    private LocalDate data;
    private String nomeProduto;
}
