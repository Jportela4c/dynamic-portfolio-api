package com.portfolio.api.model.dto.request;

import com.portfolio.api.model.enums.TipoProduto;
import com.portfolio.api.validation.ValidInvestmentTerm;
import com.portfolio.api.validation.ValidInvestmentValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requisição para simulação de investimento. Use os exemplos no Swagger UI para facilitar o teste.")
public class SimulationRequest {

    @Schema(description = "ID do cliente solicitante da simulação",
            example = "1",
            required = true)
    @NotNull(message = "Requisição inválida")
    @Positive(message = "Requisição inválida")
    private Long clienteId;

    @Schema(description = "Valor a ser investido em reais (BRL). Deve ser positivo e maior que zero.",
            example = "10000.00",
            required = true,
            minimum = "0.01")
    @ValidInvestmentValue
    private BigDecimal valor;

    @Schema(description = "Prazo do investimento em meses. Valores típicos: 6, 12, 18, 24, 36, 120 meses.",
            example = "12",
            required = true,
            minimum = "1",
            maximum = "360")
    @ValidInvestmentTerm
    private Integer prazoMeses;

    @Schema(description = "Tipo de produto financeiro para simulação",
            example = "CDB",
            required = true,
            allowableValues = {"CDB", "RDB", "LCI", "LCA", "RENDA_FIXA", "ACOES", "MULTIMERCADO", "CAMBIAL", "FII", "TESOURO_SELIC", "TESOURO_PREFIXADO", "TESOURO_IPCA", "CRI", "CRA", "VARIABLE_INCOME"})
    @NotNull(message = "Requisição inválida")
    private TipoProduto tipoProduto;
}
