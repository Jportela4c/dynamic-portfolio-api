package com.portfolio.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.stereotype.Component;

@Component
public class OAuth2EndpointsCustomizer implements GlobalOpenApiCustomizer {

    @Override
    public void customise(OpenAPI openApi) {
        if (openApi.getPaths() == null) {
            return;
        }

        // Enhance /oauth2/token endpoint
        enhanceTokenEndpoint(openApi);

        // Enhance /.well-known/oauth-authorization-server endpoint
        enhanceAuthServerMetadataEndpoint(openApi);

        // Enhance /.well-known/openid-configuration endpoint
        enhanceOpenIdConfigurationEndpoint(openApi);

        // Enhance /oauth2/jwks endpoint
        enhanceJwksEndpoint(openApi);
    }

    private void enhanceTokenEndpoint(OpenAPI openApi) {
        var pathItem = openApi.getPaths().get("/oauth2/token");
        if (pathItem != null && pathItem.getPost() != null) {
            Operation operation = pathItem.getPost();

            operation.setSummary("Obter token de acesso OAuth2");
            operation.setDescription(
                "Gera um token de acesso OAuth2 usando client credentials.\n\n" +
                "**Autenticação:** Basic Auth com Client ID e Client Secret\n\n" +
                "**Exemplo de uso com curl:**\n" +
                "```bash\n" +
                "curl -X POST http://localhost:8080/oauth2/token \\\n" +
                "  -u 'portfolio-api-client:api-secret' \\\n" +
                "  -H 'Content-Type: application/x-www-form-urlencoded' \\\n" +
                "  -d 'grant_type=client_credentials&scope=read write'\n" +
                "```\n\n" +
                "**Credenciais disponíveis:**\n" +
                "- Client ID: `portfolio-api-client`\n" +
                "- Client Secret: `api-secret`\n" +
                "- Scopes: `read`, `write`"
            );

            // Enhance Authorization header parameter
            if (operation.getParameters() != null) {
                operation.getParameters().stream()
                    .filter(p -> "Authorization".equals(p.getName()))
                    .findFirst()
                    .ifPresent(p -> {
                        p.setDescription("Basic Auth header com credenciais do cliente (Base64: clientId:clientSecret)");
                        p.setExample("Basic cG9ydGZvbGlvLWFwaS1jbGllbnQ6YXBpLXNlY3JldA==");
                    });
            }

            // Enhance responses
            ApiResponses responses = operation.getResponses();
            if (responses != null) {
                if (responses.get("200") != null) {
                    responses.get("200").setDescription(
                        "Token gerado com sucesso. Use o `access_token` no header Authorization: Bearer {token}"
                    );
                }
                if (responses.get("400") != null) {
                    responses.get("400").setDescription(
                        "Requisição inválida - verifique os parâmetros (grant_type, scope, etc.)"
                    );
                }
                if (responses.get("401") != null) {
                    responses.get("401").setDescription(
                        "Credenciais inválidas - verifique o Client ID e Client Secret no header Authorization"
                    );
                }
            }
        }
    }

    private void enhanceAuthServerMetadataEndpoint(OpenAPI openApi) {
        var pathItem = openApi.getPaths().get("/.well-known/oauth-authorization-server");
        if (pathItem != null && pathItem.getGet() != null) {
            Operation operation = pathItem.getGet();

            operation.setSummary("Obter metadados do servidor de autorização OAuth2");
            operation.setDescription(
                "Retorna a configuração e capacidades do servidor de autorização OAuth2 segundo a RFC 8414.\n\n" +
                "Inclui informações sobre:\n" +
                "- Endpoints disponíveis (token, authorization, introspection, revocation)\n" +
                "- Grant types suportados\n" +
                "- Métodos de autenticação de cliente\n" +
                "- Issuer (emissor dos tokens)\n\n" +
                "**Referência:** [RFC 8414 - OAuth 2.0 Authorization Server Metadata](https://tools.ietf.org/html/rfc8414)"
            );

            ApiResponses responses = operation.getResponses();
            if (responses != null && responses.get("200") != null) {
                responses.get("200").setDescription(
                    "Metadados do servidor de autorização OAuth2"
                );
            }
        }
    }

    private void enhanceOpenIdConfigurationEndpoint(OpenAPI openApi) {
        var pathItem = openApi.getPaths().get("/.well-known/openid-configuration");
        if (pathItem != null && pathItem.getGet() != null) {
            Operation operation = pathItem.getGet();

            operation.setSummary("Obter configuração OpenID Connect");
            operation.setDescription(
                "Retorna a configuração do provedor OpenID Connect segundo a especificação OpenID Connect Discovery.\n\n" +
                "Inclui informações sobre:\n" +
                "- Endpoints OIDC (userinfo, jwks_uri)\n" +
                "- Scopes suportados\n" +
                "- Tipos de resposta suportados\n" +
                "- Algoritmos de assinatura de tokens\n\n" +
                "**Referência:** [OpenID Connect Discovery](https://openid.net/specs/openid-connect-discovery-1_0.html)"
            );

            ApiResponses responses = operation.getResponses();
            if (responses != null && responses.get("200") != null) {
                responses.get("200").setDescription(
                    "Configuração do provedor OpenID Connect"
                );
            }
        }
    }

    private void enhanceJwksEndpoint(OpenAPI openApi) {
        var pathItem = openApi.getPaths().get("/oauth2/jwks");
        if (pathItem != null && pathItem.getGet() != null) {
            Operation operation = pathItem.getGet();

            operation.setSummary("Obter JSON Web Key Set (JWKS)");
            operation.setDescription(
                "Retorna o conjunto de chaves públicas usadas para verificar a assinatura dos tokens JWT.\n\n" +
                "Este endpoint é usado por clientes e resource servers para:\n" +
                "- Validar a assinatura dos tokens JWT\n" +
                "- Verificar a autenticidade dos tokens\n" +
                "- Obter as chaves públicas sem necessidade de compartilhamento prévio\n\n" +
                "**Referência:** [RFC 7517 - JSON Web Key (JWK)](https://tools.ietf.org/html/rfc7517)"
            );

            ApiResponses responses = operation.getResponses();
            if (responses != null && responses.get("200") != null) {
                responses.get("200").setDescription(
                    "JSON Web Key Set contendo as chaves públicas do servidor"
                );
            }
        }
    }
}
