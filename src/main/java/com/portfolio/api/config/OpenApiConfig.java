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
            
            ## Autenticação
            Use o endpoint `/auth/login` com o username "demo" para obter um token JWT.
            Depois, clique no botão "Authorize" e insira o token no formato: `Bearer {seu-token}`
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
    description = "Autenticação JWT. Use o endpoint /auth/login para obter um token."
)
public class OpenApiConfig {
}
