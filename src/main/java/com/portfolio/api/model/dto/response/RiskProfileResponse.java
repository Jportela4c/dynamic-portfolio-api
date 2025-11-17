package com.portfolio.api.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Perfil de risco do cliente")
public class RiskProfileResponse {

    @Schema(description = "ID do cliente", example = "123")
    private Long clienteId;

    @Schema(description = "Classificação do perfil de risco", example = "Moderado", allowableValues = {"Conservador", "Moderado", "Agressivo"})
    private String perfil;

    @Schema(description = "Pontuação de risco calculada", example = "65")
    private Integer pontuacao;

    @Schema(description = "Descrição detalhada do perfil", example = "Cliente com perfil moderado, busca equilíbrio entre rentabilidade e segurança")
    private String descricao;
}
