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
@Schema(description = "Requisição para simulação de investimento")
public class SimulationRequest {

    @Schema(description = "ID do cliente", example = "123", required = true)
    @NotNull(message = "Invalid request")
    @Positive(message = "Invalid request")
    private Long clienteId;

    @Schema(description = "Valor do investimento em reais", example = "10000.00", required = true)
    @ValidInvestmentValue
    private BigDecimal valor;

    @Schema(description = "Prazo do investimento em meses", example = "12", required = true, minimum = "1")
    @ValidInvestmentTerm
    private Integer prazoMeses;

    @Schema(description = "Tipo de produto financeiro", example = "CDB", required = true)
    @NotNull(message = "Invalid request")
    private TipoProduto tipoProduto;
}
