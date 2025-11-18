package com.portfolio.api.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Histórico de investimento do cliente")
public class InvestmentResponse {

    @Schema(description = "ID do investimento", example = "1")
    private Long id;

    @Schema(description = "Tipo de produto financeiro", example = "CDB")
    private String tipo;

    @Schema(description = "Valor investido", example = "5000.00")
    private BigDecimal valor;

    @Schema(description = "Taxa de rentabilidade", example = "0.12")
    private BigDecimal rentabilidade;

    @Schema(description = "Data do investimento", example = "2025-01-15")
    private LocalDate data;
}
