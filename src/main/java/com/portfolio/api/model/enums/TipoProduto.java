package com.portfolio.api.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tipo de produto financeiro disponível")
public enum TipoProduto {

    @Schema(description = "Certificado de Depósito Bancário")
    @JsonProperty("CDB")
    CDB,

    @Schema(description = "Letra de Crédito Imobiliário (isento de IR)")
    @JsonProperty("LCI")
    LCI,

    @Schema(description = "Letra de Crédito do Agronegócio (isento de IR)")
    @JsonProperty("LCA")
    LCA,

    @Schema(description = "Títulos públicos do governo federal")
    @JsonProperty("TesouroDireto")
    TESOURO_DIRETO,

    @Schema(description = "Fundo de investimento em renda fixa")
    @JsonProperty("FundoRendaFixa")
    FUNDO_RENDA_FIXA,

    @Schema(description = "Fundo de investimento multimercado")
    @JsonProperty("FundoMultimercado")
    FUNDO_MULTIMERCADO,

    @Schema(description = "Fundo de investimento em ações")
    @JsonProperty("FundoAcoes")
    FUNDO_ACOES,

    @Schema(description = "Fundo de Investimento Imobiliário")
    @JsonProperty("FII")
    FII
}
