package com.portfolio.api.model.dto.request;

import com.portfolio.api.model.enums.TipoProduto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
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
    @NotNull(message = "ID do cliente é obrigatório")
    @Positive(message = "ID do cliente deve ser positivo")
    private Long clienteId;

    @Schema(description = "Valor do investimento em reais", example = "10000.00", required = true)
    @NotNull(message = "Valor é obrigatório")
    @Positive(message = "Valor deve ser positivo")
    private BigDecimal valor;

    @Schema(description = "Prazo do investimento em meses", example = "12", required = true, minimum = "1")
    @NotNull(message = "Prazo em meses é obrigatório")
    @Min(value = 1, message = "Prazo deve ser no mínimo 1 mês")
    private Integer prazoMeses;

    @Schema(
        description = "Tipo de produto financeiro",
        example = "CDB",
        required = true,
        allowableValues = {"CDB", "LCI", "LCA", "TESOURO_DIRETO", "FUNDO_RENDA_FIXA", "FUNDO_MULTIMERCADO", "FUNDO_ACOES", "FII"}
    )
    @NotNull(message = "Tipo de produto é obrigatório")
    private TipoProduto tipoProduto;
}
