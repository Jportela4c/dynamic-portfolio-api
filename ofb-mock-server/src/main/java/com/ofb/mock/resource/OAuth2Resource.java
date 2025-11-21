package com.ofb.mock.resource;

import com.ofb.mock.security.JWSSigningService;
import com.ofb.mock.security.OAuth2Service;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Path("/oauth2")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "OAuth2 / OIDC", description = "Endpoints de autenticação e autorização OAuth2 compatíveis com FAPI (Financial-grade API)")
public class OAuth2Resource {

    @Inject
    OAuth2Service oauth2Service;

    @Inject
    JWSSigningService jwsSigningService;

    @ConfigProperty(name = "oauth.issuer", defaultValue = "https://localhost:8443")
    String issuer;

    @POST
    @Path("/par")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
        summary = "PAR - Solicitação de Autorização Enviada (Pushed Authorization Request)",
        description = """
            Cria uma solicitação de autorização OAuth2 enviada conforme RFC 9126 e FAPI (Financial-grade API).

            ## Descrição

            Este endpoint é o primeiro passo do fluxo OAuth2 seguro compatível com Open Finance Brasil.
            O cliente envia os parâmetros de autorização diretamente ao servidor de autorização via POST
            (em vez de enviá-los através do navegador), recebendo um `request_uri` que será usado no
            próximo passo do fluxo.

            ## Requisitos de Segurança

            - **mTLS obrigatório**: Certificado de cliente válido é necessário
            - **Validação de parâmetros**: Todos os parâmetros são validados antes de criar o request_uri
            - **Expiração curta**: O request_uri expira em 90 segundos

            ## Fluxo Completo OAuth2 PAR

            1. **PAR** (você está aqui) → Enviar parâmetros e obter request_uri
            2. **Authorize** → Usar request_uri para obter authorization code
            3. **Token** → Trocar authorization code por access token

            ## Referências Técnicas

            - RFC 9126: OAuth 2.0 Pushed Authorization Requests
            - FAPI Security Profile 1.0
            - Open Finance Brasil - Security Profile
            """
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "PAR criado com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    name = "Sucesso",
                    value = "{\"request_uri\":\"urn:ietf:params:oauth:request_uri:77e50d57-55a1-4303-93af-51df7a7ec0d3\",\"expires_in\":90}"
                )
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Parâmetros inválidos ou ausentes",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(
                    value = "{\"error\":\"invalid_request\",\"error_description\":\"Parâmetros obrigatórios ausentes\"}"
                )
            )
        )
    })
    public Response pushedAuthorizationRequest(
            @FormParam("client_id")
            @Parameter(
                description = "ID do client OAuth2 registrado",
                example = "portfolio-api",
                required = true
            )
            String clientId,

            @FormParam("cpf_hint")
            @Parameter(
                description = "CPF do cliente (11 dígitos) - Demo only parameter for mock server",
                example = "12345678901",
                required = true
            )
            String cpfHint,

            @FormParam("scope")
            @Parameter(
                description = "Escopos solicitados (ex: investments:read openid)",
                example = "investments:read",
                required = true
            )
            String scope,

            @FormParam("redirect_uri")
            @Parameter(
                description = "URI de redirecionamento após autorização",
                example = "https://example.com/callback",
                required = true
            )
            String redirectUri,

            @FormParam("response_type")
            @Parameter(
                description = "Tipo de resposta OAuth2",
                example = "code",
                required = false
            )
            String responseType) {

        log.info("PAR request - clientId: {}, cpfHint: {}***, scope: {}",
                 clientId, cpfHint != null && cpfHint.length() >= 3 ? cpfHint.substring(0, 3) : "???", scope);

        if (clientId == null || cpfHint == null || scope == null || redirectUri == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "invalid_request", "error_description", "Missing required parameters"))
                    .build();
        }

        try {
            String requestUri = oauth2Service.createPushedAuthRequest(clientId, cpfHint, scope, redirectUri);

            Map<String, Object> response = new HashMap<>();
            response.put("request_uri", requestUri);
            response.put("expires_in", 90);

            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            log.warn("PAR request failed: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "invalid_request", "error_description", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/authorize")
    @Operation(
        summary = "Autorização OAuth2 - Obter Código de Autorização",
        description = """
            Processa a solicitação de autorização usando o request_uri obtido no endpoint PAR.

            ## Descrição

            Este é o segundo passo do fluxo OAuth2. O cliente utiliza o `request_uri` recebido
            do endpoint PAR para obter um código de autorização (authorization code).

            ## Comportamento do Servidor Mock

            **Importante**: Este servidor mock simula aprovação automática de consentimento.

            - **Servidor Mock**: Aprova automaticamente e retorna código
            - **Produção OFB Real**: Mostraria tela de login e solicitação de consentimento ao usuário

            ## Fluxo Completo OAuth2 PAR

            1. **PAR** → Criar request_uri
            2. **Authorize** (você está aqui) → Obter authorization code
            3. **Token** → Trocar code por access token

            ## Formato da Resposta

            Redireciona para `redirect_uri?code=ABC123` com status HTTP 303 (See Other).

            Este comportamento está conforme a especificação OAuth2 RFC 6749.
            """
    )
    @APIResponses({
        @APIResponse(
            responseCode = "303",
            description = "Redirecionamento para redirect_uri com código de autorização"
        ),
        @APIResponse(responseCode = "400", description = "request_uri inválido ou expirado")
    })
    public Response authorize(
            @QueryParam("request_uri")
            @Parameter(description = "URI da requisição PAR", required = true, example = "urn:ietf:params:oauth:request_uri:77e50d57...")
            String requestUri,
            @QueryParam("client_id")
            @Parameter(description = "ID do client", required = true, example = "portfolio-api-client")
            String clientId) {

        log.info("Authorization request - requestUri: {}, clientId: {}", requestUri, clientId);

        OAuth2Service.PushedAuthRequest par = oauth2Service.getPushedAuthRequest(requestUri);
        if (par == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "invalid_request_uri"))
                    .build();
        }

        if (!par.getClientId().equals(clientId)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "invalid_client"))
                    .build();
        }

        // Mock server: Auto-approve consent (simulates user clicking "Approve")
        // In production OFB: would show login page, user authenticates, grants consent
        log.info("Mock: Auto-approving consent (simulating user approval)");
        String authCode = oauth2Service.createAuthorizationCode(requestUri);

        // OAuth2 spec: redirect to redirect_uri with code parameter
        String redirectLocation = par.getRedirectUri() + "?code=" + authCode;
        log.debug("Redirecting to: {}", redirectLocation);

        return Response.seeOther(URI.create(redirectLocation)).build();
    }

    @POST
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
        summary = "Token OAuth2 - Trocar Código por Access Token",
        description = """
            Troca o código de autorização (authorization code) por tokens de acesso e identificação.

            ## Descrição

            Este é o terceiro e último passo do fluxo OAuth2 PAR. O cliente envia o código de
            autorização recebido do endpoint `/authorize` e recebe tokens para acessar as APIs
            protegidas do Open Finance Brasil.

            ## Tokens Emitidos

            ### Access Token (Token de Acesso)

            - **Formato**: JWT assinado com algoritmo PS256
            - **Uso**: Autenticar requisições às APIs OFB
            - **Validade**: 3600 segundos (1 hora)
            - **Verificação**: Use o endpoint `/oauth2/jwks` para obter chaves públicas

            ### ID Token (Token de Identificação)

            - **Formato**: JWE criptografado (RSA-OAEP + A256GCM)
            - **Conteúdo**: Informações sobre o cliente e sessão
            - **Padrão**: OpenID Connect (OIDC)

            ## Fluxo Completo OAuth2 PAR

            1. **PAR** → Criar request_uri
            2. **Authorize** → Obter authorization code
            3. **Token** (você está aqui) → Trocar code por tokens

            ## Requisitos de Segurança

            - Requer autenticação mTLS (certificado de cliente)
            - O código de autorização só pode ser usado uma vez
            - Tokens assinados digitalmente (não podem ser falsificados)
            - ID token criptografado (protege dados sensíveis)
            """
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Tokens emitidos com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(
                    value = "{\"access_token\":\"eyJ...\",\"token_type\":\"Bearer\",\"expires_in\":3600,\"id_token\":\"eyJ...\"}"
                )
            )
        ),
        @APIResponse(responseCode = "400", description = "Código inválido ou grant_type não suportado")
    })
    public Response token(
            @FormParam("grant_type")
            @Parameter(description = "Tipo de grant OAuth2", required = true, example = "authorization_code")
            String grantType,

            @FormParam("code")
            @Parameter(description = "Código de autorização recebido do endpoint /authorize", required = true)
            String code,

            @FormParam("redirect_uri")
            @Parameter(description = "URI de redirecionamento (deve ser igual ao usado no PAR)")
            String redirectUri,

            @FormParam("client_id")
            @Parameter(description = "ID do client", required = true, example = "portfolio-api-client")
            String clientId) {

        log.info("Token request - grantType: {}, clientId: {}", grantType, clientId);

        if (!"authorization_code".equals(grantType)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "unsupported_grant_type"))
                    .build();
        }

        if (code == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "invalid_request"))
                    .build();
        }

        try {
            String accessToken = oauth2Service.createAccessToken(code);
            String idToken = oauth2Service.createIdToken(clientId);

            Map<String, Object> response = new HashMap<>();
            response.put("access_token", accessToken);
            response.put("token_type", "Bearer");
            response.put("expires_in", 3600);
            response.put("id_token", idToken);

            return Response.ok(response).build();
        } catch (Exception e) {
            log.error("Failed to create token", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "invalid_grant"))
                    .build();
        }
    }

    @GET
    @Path("/.well-known/openid-configuration")
    @Operation(
        summary = "OIDC Discovery - Descoberta Automática de Configuração",
        description = """
            Retorna os metadados do servidor de autorização conforme especificação OIDC Discovery.

            ## Descrição

            Este endpoint implementa o padrão OpenID Connect Discovery (RFC 8414), permitindo
            que clientes descubram automaticamente a configuração do servidor OAuth2/OIDC sem
            necessidade de configuração manual.

            ## Conteúdo da Resposta

            O endpoint retorna um documento JSON contendo:

            - **URLs dos Endpoints**: Localização de todos os endpoints OAuth2/OIDC
            - **Capacidades Suportadas**: Grant types, response types, algoritmos
            - **Métodos de Autenticação**: Como clientes devem se autenticar
            - **Recursos FAPI**: Extensões específicas para Financial-grade API

            ## Uso Recomendado

            Clientes devem **sempre** buscar este endpoint primeiro ao se conectar ao servidor,
            em vez de configurar URLs manualmente. Isso garante:

            - Configuração sempre atualizada
            - Compatibilidade com mudanças futuras
            - Descoberta automática de novos recursos

            ## Especificações Implementadas

            - RFC 8414: OAuth 2.0 Authorization Server Metadata
            - OpenID Connect Discovery 1.0
            - Open Finance Brasil - Discovery Profile
            """
    )
    @APIResponse(
        responseCode = "200",
        description = "Configuração OIDC do servidor",
        content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    public Response openidConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("issuer", issuer);
        config.put("authorization_endpoint", issuer + "/oauth2/authorize");
        config.put("token_endpoint", issuer + "/oauth2/token");
        config.put("pushed_authorization_request_endpoint", issuer + "/oauth2/par");
        config.put("jwks_uri", issuer + "/oauth2/jwks");
        config.put("response_types_supported", new String[]{"code"});
        config.put("grant_types_supported", new String[]{"authorization_code"});
        config.put("token_endpoint_auth_methods_supported", new String[]{"tls_client_auth"});

        return Response.ok(config).build();
    }

    @GET
    @Path("/jwks")
    @Operation(
        summary = "JWKS - Conjunto de Chaves Públicas (JSON Web Key Set)",
        description = """
            Retorna o conjunto de chaves públicas usadas pelo servidor de autorização para
            operações criptográficas.

            ## Descrição

            Este endpoint expõe as chaves públicas necessárias para que clientes possam:

            - **Validar assinaturas**: Verificar a autenticidade de tokens JWT (algoritmo PS256)
            - **Criptografar dados**: Criptografar ID tokens e outras informações (algoritmo RSA-OAEP)

            ## Uso do JWKS

            Clientes devem buscar este endpoint para:

            1. **Verificar Access Tokens**: Validar a assinatura do JWT antes de aceitar o token
            2. **Rotação de Chaves**: Detectar quando novas chaves foram adicionadas ao servidor
            3. **Múltiplas Chaves**: Identificar qual chave foi usada através do campo `kid` (Key ID)

            ## Formato da Resposta

            Retorna um objeto JSON contendo array `keys` com as chaves públicas em formato JWK (JSON Web Key).

            Cada chave contém:

            - **kid**: Identificador único da chave
            - **kty**: Tipo da chave (RSA)
            - **use**: Uso da chave (sig para assinatura, enc para criptografia)
            - **alg**: Algoritmo criptográfico
            - **n** e **e**: Componentes da chave pública RSA

            ## Boas Práticas de Segurança

            - Implemente cache com TTL apropriado (ex: 24 horas)
            - Revalide chaves periodicamente
            - Suporte múltiplas chaves simultaneamente durante rotação
            - Valide sempre o campo `kid` ao verificar tokens
            """
    )
    @APIResponse(
        responseCode = "200",
        description = "Conjunto de chaves públicas JWK",
        content = @Content(mediaType = MediaType.APPLICATION_JSON)
    )
    public Response jwks() {
        Map<String, Object> jwks = new HashMap<>();
        jwks.put("keys", new Object[]{
                oauth2Service.getSigningPublicKey().toJSONObject(),
                oauth2Service.getEncryptionPublicKey().toJSONObject(),
                jwsSigningService.getPublicJWK().toJSONObject()  // Add JWS signing key for OFB API responses
        });

        return Response.ok(jwks).build();
    }
}
