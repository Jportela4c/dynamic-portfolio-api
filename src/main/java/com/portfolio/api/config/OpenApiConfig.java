package com.portfolio.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class OpenApiConfig {

    @Bean
    @Profile("dev")
    public OpenAPI devOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Portfólio Dinâmico")
                        .version("1.0.0")
                        .description("""
                                Sistema de simulação de investimentos para produtos financeiros brasileiros.

                                ## Ambiente: DESENVOLVIMENTO

                                ### Autenticação OAuth2 Password Grant (DEV ONLY)

                                Clique em "Authorize" e entre com:
                                - **Username (email):** joao.silva@example.com
                                - **Password:** customer123
                                - **Client credentials:** Pré-preenchidos automaticamente

                                **ADMIN user (multi-customer access):**
                                - Email: admin@demo.local
                                - Password: admin123
                                """)
                        .contact(new Contact()
                                .name("Dynamic Portfolio API")
                                .url("https://github.com/jportela4c/dynamic-portfolio-api")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .description("""
                                        OAuth2 Password Grant (DEV ONLY)

                                        **Credenciais disponíveis para teste:**

                                        📋 **CUSTOMER (acesso próprio apenas):**
                                        • Email: joao.silva@example.com
                                        • Password: customer123
                                        • Cliente ID: 1

                                        🔧 **ADMIN (acesso multi-customer - dev only):**
                                        • Email: admin@demo.local
                                        • Password: admin123
                                        • Cliente ID: 999 (pode acessar qualquer cliente)

                                        ℹ️ Client credentials são pré-preenchidos automaticamente.
                                        """)
                                .flows(new OAuthFlows()
                                        .password(new OAuthFlow()
                                                .tokenUrl("/api/v1/oauth2/token")
                                                .scopes(new Scopes()
                                                        .addString("read", "Read access")
                                                        .addString("write", "Write access")
                                                        .addString("openid", "OpenID")
                                                        .addString("profile", "Profile"))))))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    @Bean
    @Profile("!dev")
    public OpenAPI prodOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Portfólio Dinâmico")
                        .version("1.0.0")
                        .description("""
                                Sistema de simulação de investimentos para produtos financeiros brasileiros.

                                ## Ambiente: PRODUÇÃO

                                ### Autenticação OAuth2 Authorization Code Flow

                                Esta API usa OAuth2 Authorization Code flow (seguro).
                                Password Grant NÃO está disponível em produção.
                                """)
                        .contact(new Contact()
                                .name("Dynamic Portfolio API")
                                .url("https://github.com/jportela4c/dynamic-portfolio-api")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("OAuth2 Bearer Token")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
