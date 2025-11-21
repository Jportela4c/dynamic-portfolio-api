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
                        .title("API de Portfólio Dinâmico - DEV")
                        .version("1.0.0")
                        .description("""
                                Sistema de simulação de investimentos para produtos financeiros brasileiros.

                                ## Quick Start

                                1. Clique em **Authorize** acima
                                2. Selecione todos os scopes
                                3. Login: `joao.silva@example.com` / `customer123`
                                4. Teste os endpoints!

                                ---

                                <details>
                                <summary><strong>Credenciais de Teste</strong> - Admin + 5 clientes com diferentes perfis</summary>

                                ### ADMIN (Dev Only)
                                | Email | Senha | ID |
                                |-------|-------|-----|
                                | `admin@demo.local` | `admin123` | 999 |

                                ### CLIENTES

                                | ID | Email | Senha | Perfil |
                                |----|-------|-------|--------|
                                | 1 | `joao.silva@example.com` | `customer123` | Conservador |
                                | 2 | `maria.santos@example.com` | `customer123` | Moderado |
                                | 3 | `pedro.costa@example.com` | `customer123` | Agressivo |
                                | 4 | `ana.oliveira@example.com` | `customer123` | Conservador |
                                | 5 | `carlos.lima@example.com` | `customer123` | Agressivo |

                                > Client ID e Secret preenchidos automaticamente.

                                </details>

                                <details>
                                <summary><strong>Detalhes dos Clientes</strong> - CPF, qtd investimentos, patrimônio</summary>

                                | Cliente | CPF | Investimentos | Patrimônio |
                                |---------|-----|---------------|------------|
                                | João Silva | 12345678901 | 12 apps | R$ 85k |
                                | Maria Santos | 98765432109 | 18 apps | R$ 156k |
                                | Pedro Costa | 11122233344 | 20 apps | R$ 320k |
                                | Ana Oliveira | 55566677788 | 10 apps | R$ 42k |
                                | Carlos Lima | 99988877766 | 12 apps | R$ 510k |

                                </details>

                                <details>
                                <summary><strong>Como trocar de usuário</strong> - Logout e re-autenticação</summary>

                                1. Clique no cadeado
                                2. Clique **"Logout"**
                                3. Clique **"Authorize"** novamente

                                > Apenas fechar o dialog NÃO limpa a sessão!

                                </details>

                                <details>
                                <summary><strong>Endpoints da API</strong> - Simulação, Histórico, Perfil, Telemetria</summary>

                                ### Simulação
                                - `POST /simular-investimento` - Simular investimento

                                ### Histórico
                                - `GET /simulacoes` - Listar simulações
                                - `GET /simulacoes/por-produto-dia` - Agregação diária
                                - `GET /investimentos/{clienteId}` - Investimentos OFB

                                ### Perfil de Risco
                                - `GET /perfil-risco/{clienteId}` - Perfil dinâmico
                                - `GET /produtos-recomendados/{perfil}` - Recomendações

                                ### Telemetria
                                - `GET /telemetria` - Métricas de performance

                                </details>

                                <details>
                                <summary><strong>Funcionalidades do Sistema</strong> - Simulação, Perfil de Risco, OFB...</summary>

                                - Simulação de investimentos (CDB, LCI, LCA, Tesouro Direto, Fundos)
                                - Perfilamento dinâmico de risco baseado em comportamento real
                                - Recomendação de produtos por perfil
                                - Histórico de investimentos via **Open Finance Brasil**
                                - Telemetria e métricas de performance

                                </details>

                                <details>
                                <summary><strong>Integração Open Finance Brasil</strong> - 5 categorias, mock server...</summary>

                                ### Categorias de Investimentos OFB
                                - **Bank Fixed Incomes** - CDB, LCI, LCA, RDB
                                - **Credit Fixed Incomes** - Debêntures, CRI, CRA
                                - **Funds** - Fundos RF, Ações, Multimercado
                                - **Treasury Titles** - Tesouro Selic, IPCA+, Prefixado
                                - **Variable Incomes** - Ações, BDRs, ETFs

                                ### Servidor Mock OFB
                                72 investimentos + 436 transações (json-schema-faker)

                                - [Swagger UI OFB](http://localhost:8089/q/swagger-ui)
                                - [OpenAPI Spec](http://localhost:8089/q/openapi)

                                </details>

                                ---
                                """)
                        .contact(new Contact()
                                .name("Dynamic Portfolio API")
                                .url("https://github.com/jportela4c/dynamic-portfolio-api")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .description("""
                                        OAuth2 Authorization Code Flow

                                        **Quick:** `joao.silva@example.com` / `customer123`

                                        <details>
                                        <summary><strong>Todas as Credenciais</strong> - 5 clientes + admin</summary>

                                        #### CLIENTES

                                        ```
                                        ID 1  │ joao.silva@example.com    │ customer123  │ Conservador
                                        ID 2  │ maria.santos@example.com  │ customer123  │ Moderado
                                        ID 3  │ pedro.costa@example.com   │ customer123  │ Agressivo
                                        ID 4  │ ana.oliveira@example.com  │ customer123  │ Conservador
                                        ID 5  │ carlos.lima@example.com   │ customer123  │ Agressivo
                                        ```

                                        #### ADMIN

                                        ```
                                        ID 999  │ admin@demo.local  │ admin123
                                        ```

                                        </details>

                                        *Client ID/Secret preenchidos automaticamente*
                                        """)
                                .flows(new OAuthFlows()
                                        .authorizationCode(new OAuthFlow()
                                                .authorizationUrl("/api/v1/oauth2/authorize")
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
