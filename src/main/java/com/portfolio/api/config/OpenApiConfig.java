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

                                ## Como usar esta API (Desenvolvimento)

                                ### Passo 1: Clique no botão "Authorize" acima

                                ### Passo 2: Selecione os scopes desejados

                                - **read** - Consultar dados (perfil de risco, investimentos)
                                - **write** - Criar simulações
                                - **openid** - OpenID Connect (recomendado)
                                - **profile** - Informações do perfil do usuário

                                > **Dica:** Selecione todos os scopes para acesso completo.

                                ### Passo 3: Entre com suas credenciais

                                **ADMIN (acesso a todos os clientes - dev only):**
                                - Username: `admin@demo.local`
                                - Password: `admin123`

                                **CUSTOMER (acesso apenas aos próprios dados):**
                                - Username: `joao.silva@example.com`
                                - Password: `customer123`

                                > **Nota:** Client ID e Secret são preenchidos automaticamente.

                                ### Passo 4: Clique em "Authorize" e comece a testar

                                ---

                                ## Endpoints da API (conforme especificação)

                                ### 1. Simulação de Investimentos
                                - **POST /simular-investimento** - Simular investimento com valor, prazo e tipo

                                ### 2. Histórico
                                - **GET /simulacoes** - Listar todas as simulações realizadas
                                - **GET /simulacoes/por-produto-dia** - Agregação diária por produto
                                - **GET /investimentos/{clienteId}** - Histórico de investimentos do cliente (via OFB)

                                ### 3. Perfil de Risco
                                - **GET /perfil-risco/{clienteId}** - Perfil de risco dinâmico do cliente
                                - **GET /produtos-recomendados/{perfil}** - Produtos recomendados por perfil

                                ### 4. Telemetria
                                - **GET /telemetria** - Métricas de volume e performance dos serviços

                                ---

                                ## Funcionalidades

                                - Simulação de investimentos (CDB, LCI, LCA, Tesouro Direto, Fundos)
                                - Perfilamento dinâmico de risco baseado em comportamento real
                                - Recomendação de produtos por perfil
                                - Histórico de investimentos via **Open Finance Brasil**
                                - Telemetria e métricas de performance

                                ---

                                ## Integração Open Finance Brasil (OFB)

                                Esta API integra com o padrão Open Finance Brasil para buscar dados reais de investimentos:

                                ### 5 Categorias de Investimentos OFB:
                                - **Bank Fixed Incomes** - CDB, LCI, LCA, RDB
                                - **Credit Fixed Incomes** - Debêntures, CRI, CRA
                                - **Funds** - Fundos de Renda Fixa, Ações, Multimercado
                                - **Treasury Titles** - Tesouro Selic, IPCA+, Prefixado
                                - **Variable Incomes** - Ações, BDRs, ETFs

                                ### Dados Retornados:
                                - **valor** - Valor atual do investimento (netAmount após impostos)
                                - **valorInvestido** - Valor originalmente investido
                                - **rentabilidade** - Lucro/prejuízo = valor - valorInvestido
                                - **emissor** - Instituição emissora
                                - **dataVencimento** - Data de vencimento (se aplicável)

                                ### Exemplo de Resposta:
                                ```json
                                {
                                  "id": 123456,
                                  "tipo": "LCA",
                                  "emissor": "Banco XYZ",
                                  "valorInvestido": 15814.95,
                                  "valor": 20116.19,
                                  "rentabilidade": 4301.24,
                                  "dataVencimento": "2025-12-31"
                                }
                                ```

                                ### Servidor Mock OFB:
                                O projeto inclui um servidor mock completo com 72 investimentos e 436 transações geradas com dados realistas (json-schema-faker).

                                **Swagger OFB Mock:** http://localhost:8089/q/swagger-ui

                                ---
                                """)
                        .contact(new Contact()
                                .name("Dynamic Portfolio API")
                                .url("https://github.com/jportela4c/dynamic-portfolio-api")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .description("""
                                        **Credenciais para teste:**

                                        **ADMIN (acesso multi-customer - dev only):**
                                        - Email: `admin@demo.local`
                                        - Password: `admin123`
                                        - Cliente ID: 999

                                        **CUSTOMER (acesso próprio apenas):**
                                        - Email: `joao.silva@example.com`
                                        - Password: `customer123`
                                        - Cliente ID: 1

                                        > Client ID e Secret são preenchidos automaticamente.
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
