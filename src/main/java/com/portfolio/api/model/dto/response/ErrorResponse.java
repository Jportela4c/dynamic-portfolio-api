package com.portfolio.api.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Resposta de erro padrão da API")
public class ErrorResponse {

    @Schema(description = "Código HTTP do erro", example = "404")
    private int status;

    @Schema(description = "Mensagem descritiva do erro", example = "Produto não encontrado")
    private String message;

    @Schema(description = "Data e hora do erro", example = "2025-01-15T10:30:00")
    private LocalDateTime timestamp;
}
