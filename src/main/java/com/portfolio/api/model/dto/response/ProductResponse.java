package com.portfolio.api.model.dto.response;

import com.portfolio.api.model.enums.TipoProduto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Produto de investimento recomendado")
public class ProductResponse {

    @Schema(description = "ID do produto", example = "1")
    private Long id;

    @Schema(description = "Nome do produto", example = "CDB Caixa 2026")
    private String nome;

    @Schema(description = "Tipo de produto financeiro", example = "CDB")
    private TipoProduto tipo;

    @Schema(description = "Taxa de rentabilidade anual", example = "0.12")
    private BigDecimal rentabilidade;

    @Schema(description = "Nível de risco", example = "Baixo", allowableValues = {"Baixo", "Médio", "Alto"})
    private String risco;
}
