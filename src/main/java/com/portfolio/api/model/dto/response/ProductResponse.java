package com.portfolio.api.model.dto.response;

import com.portfolio.api.model.enums.TipoProduto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long id;
    private String nome;
    private TipoProduto tipo;
    private BigDecimal rentabilidade;
    private String risco;
}
