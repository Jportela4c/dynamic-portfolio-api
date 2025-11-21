package com.portfolio.api.exception;

import com.portfolio.api.model.enums.TipoProduto;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

public class ProductNotFoundException extends ApiException {

    private static final String MESSAGE = "Produto não disponível";

    public ProductNotFoundException(TipoProduto tipo, BigDecimal valor, Integer prazoMeses) {
        super(HttpStatus.NOT_FOUND, MESSAGE, new ProductContext(tipo, valor, prazoMeses));
    }

    private record ProductContext(TipoProduto tipo, BigDecimal valor, Integer prazoMeses) {}
}
