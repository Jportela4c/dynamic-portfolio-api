package com.portfolio.api.model.enums;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class TipoProdutoSerializer extends JsonSerializer<TipoProduto> {

    @Override
    public void serialize(TipoProduto value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        if (value != null) {
            gen.writeString(value.getDisplayName());
        }
    }
}
