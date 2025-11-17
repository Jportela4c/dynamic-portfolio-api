package com.portfolio.api.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Perfil de risco do investidor")
public enum PerfilRisco {

    @Schema(description = "Investidor conservador - prioriza segurança e liquidez")
    CONSERVADOR("Conservador"),

    @Schema(description = "Investidor moderado - equilibra rentabilidade e risco")
    MODERADO("Moderado"),

    @Schema(description = "Investidor agressivo - busca alta rentabilidade aceitando mais risco")
    AGRESSIVO("Agressivo");

    private final String descricao;

    PerfilRisco(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static PerfilRisco fromString(String perfil) {
        if (perfil == null) {
            return null;
        }

        String normalized = perfil.trim().toUpperCase();

        for (PerfilRisco p : PerfilRisco.values()) {
            if (p.name().equals(normalized)) {
                return p;
            }
        }

        throw new IllegalArgumentException("Perfil de risco inválido: " + perfil +
            ". Valores válidos: Conservador, Moderado, Agressivo");
    }
}
