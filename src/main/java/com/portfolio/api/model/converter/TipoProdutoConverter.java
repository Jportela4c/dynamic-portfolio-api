package com.portfolio.api.model.converter;

import com.portfolio.api.model.enums.TipoProduto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class TipoProdutoConverter implements AttributeConverter<TipoProduto, String> {

    @Override
    public String convertToDatabaseColumn(TipoProduto tipoProduto) {
        if (tipoProduto == null) {
            return null;
        }
        
        // Convert enum to database format
        return switch (tipoProduto) {
            case CDB -> "CDB";
            case LCI -> "LCI";
            case LCA -> "LCA";
            case TESOURO_DIRETO -> "Tesouro Direto";
            case FUNDO -> "Fundo";
        };
    }

    @Override
    public TipoProduto convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        
        return TipoProduto.fromString(dbData);
    }
}
