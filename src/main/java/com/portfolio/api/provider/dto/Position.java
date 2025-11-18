package com.portfolio.api.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Posição de investimento do cliente.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Position {

    private String codigoProduto;
    private String nomeProduto;
    private String tipoProduto;
    private BigDecimal valorAplicado;
    private BigDecimal valorAtual;
    private BigDecimal rentabilidade;
    private LocalDate dataAplicacao;
    private LocalDate dataVencimento;
    private String liquidez;
}
