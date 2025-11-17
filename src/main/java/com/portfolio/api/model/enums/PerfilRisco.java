package com.portfolio.api.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Perfil de risco do investidor")
public enum PerfilRisco {

    @Schema(description = "Investidor conservador - prioriza segurança e liquidez")
    CONSERVADOR,

    @Schema(description = "Investidor moderado - equilibra rentabilidade e risco")
    MODERADO,

    @Schema(description = "Investidor agressivo - busca alta rentabilidade aceitando mais risco")
    AGRESSIVO
}
