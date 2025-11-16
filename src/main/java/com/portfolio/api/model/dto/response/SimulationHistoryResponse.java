package com.portfolio.api.model.dto.response;

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
public class SimulationHistoryResponse {

    private Long id;
    private Long clienteId;
    private String produto;
    private BigDecimal valorInvestido;
    private BigDecimal valorFinal;
    private Integer prazoMeses;
    private LocalDateTime dataSimulacao;
}
