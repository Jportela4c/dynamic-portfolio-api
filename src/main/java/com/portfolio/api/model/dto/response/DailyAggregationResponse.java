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
@Schema(description = "Agregação diária de simulações por produto")
public class DailyAggregationResponse {

    @Schema(description = "Nome do produto", example = "CDB Caixa 2026")
    private String produto;

    @Schema(description = "Data da agregação", example = "2025-01-15")
    private LocalDate data;

    @Schema(description = "Quantidade total de simulações no dia", example = "25")
    private Long quantidadeSimulacoes;

    @Schema(description = "Média do valor final das simulações", example = "11500.00")
    private BigDecimal mediaValorFinal;
}
