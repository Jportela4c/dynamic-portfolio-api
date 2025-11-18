package com.portfolio.api.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Dados da carteira do cliente vindos do Sistema de Investimentos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPortfolio {

    private String cpf;
    private BigDecimal totalInvestido;
    private BigDecimal valorAtual;
    private BigDecimal rentabilidadeTotal;
    private LocalDateTime dataUltimaAtualizacao;
}
