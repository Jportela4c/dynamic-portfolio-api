package com.portfolio.api.model.converter;

import com.portfolio.api.model.enums.TipoProduto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoProdutoConverter implements AttributeConverter<TipoProduto, String> {

    @Override
    public String convertToDatabaseColumn(TipoProduto tipoProduto) {
        if (tipoProduto == null) {
            return null;
        }

        // Use the descricao field which matches database values
        return tipoProduto.getDescricao();
    }

    @Override
    public TipoProduto convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        
        return TipoProduto.fromString(dbData);
    }
}
