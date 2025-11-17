package com.portfolio.api.config;

import com.portfolio.api.model.enums.PerfilRisco;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class PerfilRiscoConverter implements Converter<String, PerfilRisco> {

    @Override
    public PerfilRisco convert(String source) {
        // Convert PascalCase input to UPPERCASE enum name
        String normalized = source.toUpperCase();

        // Handle the three valid values
        return switch (normalized) {
            case "CONSERVADOR" -> PerfilRisco.CONSERVADOR;
            case "MODERADO" -> PerfilRisco.MODERADO;
            case "AGRESSIVO" -> PerfilRisco.AGRESSIVO;
            default -> throw new IllegalArgumentException("Invalid risk profile: " + source);
        };
    }
}
