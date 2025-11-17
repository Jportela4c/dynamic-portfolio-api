package com.portfolio.api.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tipo de produto financeiro disponível")
public enum TipoProduto {

    @Schema(description = "Certificado de Depósito Bancário")
    CDB("Certificado de Depósito Bancário"),

    @Schema(description = "Letra de Crédito Imobiliário (isento de IR)")
    LCI("Letra de Crédito Imobiliário"),

    @Schema(description = "Letra de Crédito do Agronegócio (isento de IR)")
    LCA("Letra de Crédito do Agronegócio"),

    @Schema(description = "Títulos públicos do governo federal")
    TESOURO_DIRETO("Tesouro Direto"),

    @Schema(description = "Fundos de investimento geridos profissionalmente")
    FUNDO("Fundo de Investimento");

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

        // Normaliza string: remove espaços, converte para maiúscula
        String normalized = tipo.trim().toUpperCase().replace(" ", "_");

        for (TipoProduto t : TipoProduto.values()) {
            if (t.name().equals(normalized)) {
                return t;
            }
        }

        throw new IllegalArgumentException("Tipo de produto inválido: " + tipo +
            ". Valores válidos: CDB, LCI, LCA, Tesouro Direto, Fundo");
    }
}
