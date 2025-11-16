package com.portfolio.api.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskProfileResponse {

    private Long clienteId;
    private String perfil;
    private Integer pontuacao;
    private String descricao;
    private Map<String, Integer> fatores;
}
