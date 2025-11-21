package com.portfolio.api.model.dto.response;

import com.portfolio.api.model.enums.TipoProduto;
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
@Schema(description = "Investimento do cliente integrado via Open Finance Brasil")
public class InvestmentResponse {

    @Schema(description = "ID do investimento", example = "123456")
    private Long id;

    @Schema(description = "Tipo de produto financeiro (via OFB)",
            example = "LCA",
            allowableValues = {"CDB", "LCI", "LCA", "RDB", "DEBENTURE", "CRI", "CRA",
                              "RENDA_FIXA", "ACOES", "MULTIMERCADO", "CAMBIAL",
                              "TESOURO_SELIC", "TESOURO_IPCA", "TESOURO_PREFIXADO",
                              "VARIABLE_INCOME"})
    private TipoProduto tipo;

    @Schema(description = "Valor investido originalmente (em BRL)", example = "15814.95")
    private BigDecimal valor;

    @Schema(description = "Taxa de rentabilidade do investimento (percentual). Calculado como: (valorAtual - valorInvestido) / valorInvestido. Exemplo: 0.2138 = 21.38% de retorno",
            example = "0.2138")
    private BigDecimal rentabilidade;

    @Schema(description = "Data da última atualização", example = "2025-01-18")
    private LocalDate data;
}
