package com.portfolio.api.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Resposta de erro de validação de campos")
public class ValidationErrorResponse {

    @Schema(description = "Código HTTP do erro", example = "400")
    private int status;

    @Schema(description = "Mensagem geral do erro", example = "Erro de validação nos campos")
    private String message;

    @Schema(description = "Mapa de erros por campo", example = "{\"valor\": \"Valor deve ser positivo\", \"prazoMeses\": \"Prazo deve ser no mínimo 1 mês\"}")
    private Map<String, String> errors;

    @Schema(description = "Data e hora do erro", example = "2025-01-15T10:30:00")
    private LocalDateTime timestamp;
}
