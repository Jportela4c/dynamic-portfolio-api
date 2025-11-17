package com.portfolio.api.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tipo de produto financeiro disponível")
public enum TipoProduto {

    @Schema(description = "Certificado de Depósito Bancário")
    CDB("CDB"),

    @Schema(description = "Letra de Crédito Imobiliário (isento de IR)")
    LCI("LCI"),

    @Schema(description = "Letra de Crédito do Agronegócio (isento de IR)")
    LCA("LCA"),

    @Schema(description = "Títulos públicos do governo federal")
    TESOURO_DIRETO("Tesouro Direto"),

    @Schema(description = "Fundo de investimento em renda fixa")
    FUNDO_RENDA_FIXA("Fundo Renda Fixa"),

    @Schema(description = "Fundo de investimento multimercado")
    FUNDO_MULTIMERCADO("Fundo Multimercado"),

    @Schema(description = "Fundo de investimento em ações")
    FUNDO_ACOES("Fundo Ações"),

    @Schema(description = "Fundo de Investimento Imobiliário")
    FII("FII");

    private final String descricao;

    TipoProduto(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static TipoProduto fromString(String tipo) {
        if (tipo == null) {
            return null;
        }

        // First try exact match with descricao
        for (TipoProduto t : TipoProduto.values()) {
            if (t.descricao.equalsIgnoreCase(tipo.trim())) {
                return t;
            }
        }

        // Then try normalized enum name match
        String normalized = tipo.trim().toUpperCase()
                .replace(" ", "_")
                .replace("Ç", "C")
                .replace("Õ", "O");

        for (TipoProduto t : TipoProduto.values()) {
            if (t.name().equals(normalized)) {
                return t;
            }
        }

        throw new IllegalArgumentException("Tipo de produto inválido: " + tipo +
            ". Valores válidos: CDB, LCI, LCA, Tesouro Direto, Fundo Renda Fixa, " +
            "Fundo Multimercado, Fundo Ações, FII");
    }
}
