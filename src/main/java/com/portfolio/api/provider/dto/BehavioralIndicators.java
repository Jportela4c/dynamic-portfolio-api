package com.portfolio.api.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Indicadores comportamentais do cliente.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehavioralIndicators {

    private Integer pontuacaoRisco;
    private String perfilCalculado;
    private String tendenciaAlocacao;
    private String nivelAtividade;
}
