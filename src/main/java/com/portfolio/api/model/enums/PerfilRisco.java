package com.portfolio.api.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Perfil de risco do investidor")
public enum PerfilRisco {

    @Schema(description = "Investidor conservador - prioriza segurança e liquidez")
    @JsonProperty("Conservador")
    CONSERVADOR,

    @Schema(description = "Investidor moderado - equilibra rentabilidade e risco")
    @JsonProperty("Moderado")
    MODERADO,

    @Schema(description = "Investidor agressivo - busca alta rentabilidade aceitando mais risco")
    @JsonProperty("Agressivo")
    AGRESSIVO
}
