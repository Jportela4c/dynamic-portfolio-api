package com.portfolio.api.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
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
    FII;

    @JsonValue
    public String getDisplayName() {
        return switch (this) {
            case CDB -> "CDB";
            case LCI -> "LCI";
            case LCA -> "LCA";
            case TESOURO_DIRETO -> "Tesouro Direto";
            case FUNDO_RENDA_FIXA -> "Fundo Renda Fixa";
            case FUNDO_MULTIMERCADO -> "Fundo Multimercado";
            case FUNDO_ACOES -> "Fundo Ações";
            case FII -> "FII";
        };
    }

    @JsonCreator
    public static TipoProduto fromString(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().toUpperCase().replace(" ", "_");

        try {
            return TipoProduto.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid product type: " + value +
                ". Valid values: CDB, LCI, LCA, Tesouro Direto, " +
                "Fundo Renda Fixa, Fundo Multimercado, Fundo Ações, FII"
            );
        }
    }
}
