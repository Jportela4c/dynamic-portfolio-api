package com.portfolio.api.model.dto.response;

import com.portfolio.api.model.enums.TipoProduto;
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
@Schema(description = "Resposta da simulação de investimento")
public class SimulationResponse {

    @Schema(description = "Dados do produto validado")
    private ProductValidated produtoValidado;

    @Schema(description = "Resultado da simulação")
    private SimulationResult resultadoSimulacao;

    @Schema(description = "Data e hora da simulação", example = "2025-01-15T10:30:00")
    private LocalDateTime dataSimulacao;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Produto validado com seus parâmetros")
    public static class ProductValidated {
        @Schema(description = "ID do produto", example = "1")
        private Long id;

        @Schema(description = "Nome do produto", example = "CDB Caixa 2026")
        private String nome;

        @Schema(description = "Tipo de produto financeiro", example = "CDB")
        private TipoProduto tipo;

        @Schema(description = "Taxa de rentabilidade anual", example = "0.12")
        private BigDecimal rentabilidade;

        @Schema(description = "Nível de risco", example = "Baixo")
        private String risco;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Resultado calculado da simulação")
    public static class SimulationResult {
        @Schema(description = "Valor final do investimento", example = "11200.00")
        private BigDecimal valorFinal;

        @Schema(description = "Rentabilidade efetiva obtida", example = "0.12")
        private BigDecimal rentabilidadeEfetiva;

        @Schema(description = "Prazo em meses", example = "12")
        private Integer prazoMeses;
    }
}
