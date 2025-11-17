package com.portfolio.api.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Histórico de simulação realizada")
public class SimulationHistoryResponse {

    @Schema(description = "ID da simulação", example = "1")
    private Long id;

    @Schema(description = "ID do cliente", example = "123")
    private Long clienteId;

    @Schema(description = "Nome do produto simulado", example = "CDB Caixa 2026")
    private String produto;

    @Schema(description = "Valor investido", example = "10000.00")
    private BigDecimal valorInvestido;

    @Schema(description = "Valor final calculado", example = "11200.00")
    private BigDecimal valorFinal;

    @Schema(description = "Prazo em meses", example = "12")
    private Integer prazoMeses;

    @Schema(description = "Data e hora da simulação", example = "2025-01-15T10:30:00")
    private LocalDateTime dataSimulacao;
}
