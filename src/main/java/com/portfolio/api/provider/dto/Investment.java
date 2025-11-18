package com.portfolio.api.provider.dto;

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
    private String tipo;
    private String tipoOperacao;
    private BigDecimal valor;
    private BigDecimal rentabilidade;
    private LocalDate data;
    private String nomeProduto;
}
