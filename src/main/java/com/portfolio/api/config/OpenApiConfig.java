package com.portfolio.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "API de Portfólio Dinâmico",
        version = "1.0.0",
        description = """
            Sistema de simulação de investimentos para produtos financeiros brasileiros.

            ## Funcionalidades
            - Simulação de investimentos (CDB, LCI, LCA, Tesouro Direto, Fundos)
            - Perfilamento dinâmico de risco
            - Recomendação de produtos por perfil
            - Histórico de investimentos
            - Telemetria e métricas

            ## Autenticação OAuth2

            Esta API usa OAuth2 para autenticação.

            **Como obter um access token:**
            1. Use o endpoint `POST /oauth2/token` (documentado na seção OAuth2 abaixo)
            2. Envie Basic Auth com Client ID e Secret (abaixo)
            3. Body: `grant_type=client_credentials&scope=read write` (form-urlencoded)
            4. Copie o `access_token` da resposta
            5. Clique em "Authorize" acima e cole: `{seu-access-token}` (sem "Bearer")

            **Client Credentials:**
            - Client ID: `portfolio-api-client`
            - Client Secret: `api-secret`

            ## Respostas de Erro
            A API utiliza respostas de erro padronizadas:
            - **ErrorResponse**: Erros gerais (404, 500, etc.)
            - **ValidationErrorResponse**: Erros de validação de campos (400)

            Todas as respostas de erro incluem timestamp, código HTTP e mensagem descritiva.
            """,
        contact = @Contact(
            name = "Dynamic Portfolio API",
            url = "https://github.com/jportela4c/dynamic-portfolio-api"
        )
    ),
    servers = {
        @Server(url = "/api/v1", description = "API v1")
    },
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "OAuth2 Bearer Token. Use o endpoint /oauth2/token com client credentials (portfolio-api-client:api-secret) para obter um access token."
)
public class OpenApiConfig {

}
