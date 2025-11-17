package com.portfolio.api.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tipo de produto financeiro disponível")
public enum TipoProduto {

    @Schema(description = "Certificado de Depósito Bancário")
    CDB,

    @Schema(description = "Letra de Crédito Imobiliário (isento de IR)")
    LCI,

    @Schema(description = "Letra de Crédito do Agronegócio (isento de IR)")
    LCA,

    @Schema(description = "Títulos públicos do governo federal")
    TESOURO_DIRETO,

    @Schema(description = "Fundo de investimento em renda fixa")
    FUNDO_RENDA_FIXA,

    @Schema(description = "Fundo de investimento multimercado")
    FUNDO_MULTIMERCADO,

    @Schema(description = "Fundo de investimento em ações")
    FUNDO_ACOES,

    @Schema(description = "Fundo de Investimento Imobiliário")
    FII
}
