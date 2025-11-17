package com.portfolio.api.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requisição de autenticação")
public class LoginRequest {

    @Schema(description = "Nome de usuário", example = "demo", required = true)
    @NotBlank(message = "Invalid request")
    private String username;

    @Schema(description = "Senha do usuário", example = "password", required = false)
    private String password;
}
