package com.portfolio.api.model.enums;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class TipoProdutoDeserializer extends JsonDeserializer<TipoProduto> {

    @Override
    public TipoProduto deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText().trim().toUpperCase().replace(" ", "_");

        try {
            return TipoProduto.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid product type: " + p.getText() +
                ". Valid values: CDB, LCI, LCA, TESOURO_DIRETO (or 'Tesouro Direto'), " +
                "FUNDO_RENDA_FIXA, FUNDO_MULTIMERCADO, FUNDO_ACOES, FII"
            );
        }
    }
}
