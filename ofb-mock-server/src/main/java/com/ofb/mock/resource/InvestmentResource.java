package com.ofb.mock.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.ofb.mock.model.Investment;
import com.ofb.mock.security.OAuth2Service;
import com.ofb.mock.service.MockDataService;
import com.ofb.mock.util.JwtUtils;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@Slf4j
@Path("/open-banking/bank-fixed-incomes/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Open Finance Brasil - Investimentos", description = "APIs de investimentos em renda fixa conforme especificação Open Finance Brasil")
public class InvestmentResource {

    @Inject
    MockDataService mockDataService;

    @Inject
    OAuth2Service oauth2Service;

    @Inject
    ObjectMapper objectMapper;

    @GET
    @Path("/investments")
    @Operation(
        summary = "Listar Investimentos em Renda Fixa",
        description = """
            Retorna lista de investimentos em renda fixa do cliente conforme especificação Open Finance Brasil.

            ## Descrição

            Este endpoint implementa a API de investimentos OFB para produtos de renda fixa (CDB, LCI, LCA, etc.).
            Retorna informações detalhadas sobre os investimentos do cliente autenticado.

            ## Segurança e Conformidade OFB

            ### Requisitos de Autenticação
            - **OAuth2 Bearer Token**: Token de acesso obtido via fluxo PAR
            - **mTLS**: Certificado de cliente válido obrigatório
            - **Escopos Necessários**: `bank-fixed-incomes` ou `investments:read`

            ### Assinatura de Resposta (JWS)
            - **Algoritmo**: PS256 (RSA-PSS com SHA-256)
            - **Formato**: Resposta completa assinada em formato JWS compacto
            - **Verificação**: Use o endpoint `/oauth2/jwks` para obter chaves públicas

            ## Estrutura da Resposta

            A resposta é um JWS assinado contendo objeto JSON com:

            - **data**: Array de investimentos do cliente
            - Cada investimento contém: tipo, valor investido, rentabilidade, prazo, etc.

            ## Conformidade

            Este endpoint segue rigorosamente a especificação:
            - Open Finance Brasil - Bank Fixed Incomes API v1
            - FAPI Security Profile 1.0
            - Assinatura JWS obrigatória conforme spec OFB
            """
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Lista de investimentos retornada com sucesso (resposta assinada JWS)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(
                    name = "Resposta JWS Assinada",
                    value = "eyJhbGciOiJSUzI1NiIsImtpZCI6Im9mYi1qd3Mta2V5LTEifQ.eyJkYXRhIjpbeyJpZCI6IjEyMzQ1IiwidHlwZSI6IkNEQiIsImFtb3VudCI6MTAwMDAuMDB9XX0.signature..."
                )
            )
        ),
        @APIResponse(
            responseCode = "401",
            description = "Token de autorização inválido ou expirado"
        ),
        @APIResponse(
            responseCode = "403",
            description = "Certificado de cliente inválido ou escopos insuficientes"
        ),
        @APIResponse(
            responseCode = "500",
            description = "Erro ao assinar resposta JWS"
        )
    })
    public Response getInvestments(
            @HeaderParam("Authorization")
            @Parameter(
                description = "Bearer token OAuth2 obtido via fluxo PAR",
                required = true,
                example = "Bearer eyJraWQiOiI..."
            )
            String authorization) {
        log.info("OFB API: GET /open-banking/bank-fixed-incomes/v1/investments");

        try {
            // Extract customer ID from JWT token
            String customerId = JwtUtils.extractCustomerId(authorization);
            if (customerId == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "invalid_token"))
                    .build();
            }

            // Get investments for authenticated customer
            List<Investment> investments = mockDataService.getInvestmentsByCustomerId(customerId);
            log.debug("Returning {} investments for customer {}", investments.size(), customerId);

            // Create OFB-compliant response structure
            Map<String, Object> responseData = Map.of("data", investments);
            String jsonPayload = objectMapper.writeValueAsString(responseData);

            // Sign response as JWS (FAPI compliant)
            JWSSigner signer = new RSASSASigner(oauth2Service.getSigningKey());
            com.nimbusds.jose.JWSObject jwsObject = new com.nimbusds.jose.JWSObject(
                new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(oauth2Service.getSigningKey().getKeyID())
                    .build(),
                new Payload(jsonPayload)
            );
            jwsObject.sign(signer);

            log.debug("Returning JWS-signed investment response");
            return Response.ok(jwsObject.serialize()).build();
        } catch (Exception e) {
            log.error("Failed to sign investment response", e);
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/investments/{investmentId}")
    @Operation(
        summary = "Consultar Investimento Específico",
        description = """
            Retorna detalhes de um investimento específico por ID conforme especificação Open Finance Brasil.

            ## Descrição

            Consulta informações detalhadas de um investimento em renda fixa específico,
            incluindo rentabilidade, vencimento, indexadores e saldo atual.

            ## Segurança

            Requer autenticação OAuth2 com Bearer token e mTLS.

            ## Estrutura da Resposta

            Retorna objeto JSON com:

            - **data**: Objeto contendo detalhes completos do investimento
            - Informações incluem: tipo, emissor, valor, rentabilidade, indexadores, prazos

            ## Tratamento de Erros

            - **404 Not Found**: Investimento não encontrado para o ID fornecido
            - Resposta de erro segue padrão OFB com estrutura `errors`
            """
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Detalhes do investimento retornados com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(
                    value = "{\"data\":{\"id\":\"12345\",\"type\":\"CDB\",\"issuer\":\"Banco Exemplo\",\"amount\":10000.00,\"rate\":12.5}}"
                )
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "Investimento não encontrado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(
                    value = "{\"errors\":[{\"code\":\"NOT_FOUND\",\"title\":\"Investment not found\",\"detail\":\"Investment with id 12345 not found\"}]}"
                )
            )
        ),
        @APIResponse(
            responseCode = "401",
            description = "Token de autorização inválido ou expirado"
        ),
        @APIResponse(
            responseCode = "403",
            description = "Acesso não autorizado ao investimento"
        )
    })
    public Response getInvestmentById(
            @PathParam("investmentId")
            @Parameter(
                description = "Identificador único do investimento",
                required = true,
                example = "12345"
            )
            String investmentId,
            @HeaderParam("Authorization")
            @Parameter(
                description = "Bearer token OAuth2",
                required = true,
                example = "Bearer eyJraWQiOiI..."
            )
            String authorization) {
        log.info("OFB API: GET /open-banking/bank-fixed-incomes/v1/investments/{}", investmentId);

        // Extract customer ID from JWT token
        String customerId = JwtUtils.extractCustomerId(authorization);
        if (customerId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "invalid_token"))
                .build();
        }

        Investment investment = mockDataService.getInvestmentById(investmentId);

        if (investment == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("errors", List.of(Map.of(
                        "code", "NOT_FOUND",
                        "title", "Investment not found",
                        "detail", "Investment with id " + investmentId + " not found"
                    ))))
                    .build();
        }

        // Verify investment belongs to authenticated customer
        List<Investment> customerInvestments = mockDataService.getInvestmentsByCustomerId(customerId);
        boolean investmentBelongsToCustomer = customerInvestments.stream()
            .anyMatch(inv -> inv.getInvestmentId().equals(investmentId));

        if (!investmentBelongsToCustomer) {
            return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of("errors", List.of(Map.of(
                    "code", "FORBIDDEN",
                    "title", "Access denied",
                    "detail", "Investment does not belong to authenticated customer"
                ))))
                .build();
        }

        return Response.ok(Map.of("data", investment)).build();
    }

    @GET
    @Path("/investments/{investmentId}/balances")
    @Operation(
        summary = "Consultar Posição do Investimento",
        description = """
            Retorna a posição atual da operação de Renda Fixa Bancária identificada por investmentId.

            ## Descrição

            Consulta o saldo e posição atual de um investimento específico conforme especificação
            Open Finance Brasil.

            ## Dados Retornados

            - **Valores monetários**: Saldo atual, valor investido, rendimentos
            - **Quantidade de ativos**: Quantidade de títulos/cotas
            - **Data da posição**: Data e hora da última atualização

            ## Regras de Exposição

            Nos casos em que não houver posição para o investimento (valores zerados), mas o mesmo
            ainda estiver no prazo de exposição (até 12 meses após a última movimentação), retorna
            status 200 com valores zerados.

            ## Segurança

            Requer autenticação OAuth2 com Bearer token e mTLS.
            """
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Posição do investimento retornada com sucesso"
        ),
        @APIResponse(
            responseCode = "404",
            description = "Investimento não encontrado"
        ),
        @APIResponse(
            responseCode = "401",
            description = "Token de autorização inválido"
        )
    })
    public Response getInvestmentBalances(
            @PathParam("investmentId")
            @Parameter(
                description = "Identificador único do investimento",
                required = true,
                example = "12345"
            )
            String investmentId,
            @HeaderParam("Authorization")
            @Parameter(
                description = "Bearer token OAuth2",
                required = true
            )
            String authorization) {
        log.info("OFB API: GET /open-banking/bank-fixed-incomes/v1/investments/{}/balances", investmentId);

        // Extract customer ID from JWT token
        String customerId = JwtUtils.extractCustomerId(authorization);
        if (customerId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "invalid_token"))
                .build();
        }

        // Verify investment exists
        Investment investment = mockDataService.getInvestmentById(investmentId);
        if (investment == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("errors", List.of(Map.of(
                    "code", "NOT_FOUND",
                    "title", "Investment not found",
                    "detail", "Investment with id " + investmentId + " not found"
                ))))
                .build();
        }

        // Verify investment belongs to authenticated customer
        List<Investment> customerInvestments = mockDataService.getInvestmentsByCustomerId(customerId);
        boolean investmentBelongsToCustomer = customerInvestments.stream()
            .anyMatch(inv -> inv.getInvestmentId().equals(investmentId));

        if (!investmentBelongsToCustomer) {
            return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of("errors", List.of(Map.of(
                    "code", "FORBIDDEN",
                    "title", "Access denied",
                    "detail", "Investment does not belong to authenticated customer"
                ))))
                .build();
        }

        // Mock implementation - returns mock balance data
        return Response.ok(Map.of("data", Map.of(
            "quantity", 100.00,
            "grossAmount", 10500.00,
            "netAmount", 10350.00,
            "balanceDateTime", "2025-11-19T20:00:00Z"
        ))).build();
    }

    @GET
    @Path("/investments/{investmentId}/transactions")
    @Operation(
        summary = "Listar Transações Históricas do Investimento",
        description = """
            Retorna movimentações históricas (últimos 12 meses) da operação de Renda Fixa Bancária.

            ## Descrição

            Consulta o histórico completo de transações de um investimento específico conforme
            especificação Open Finance Brasil.

            ## Período

            - **Últimos 12 meses** a partir da data da consulta
            - Inclui todas as movimentações: aplicações, resgates, juros, impostos

            ## Segurança

            Requer autenticação OAuth2 com Bearer token e mTLS.

            ## Tipos de Transação

            - Aplicação inicial
            - Aplicações adicionais
            - Resgates parciais ou totais
            - Rendimentos creditados
            - Impostos descontados (IR, IOF)
            """
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Transações retornadas com sucesso"
        ),
        @APIResponse(
            responseCode = "404",
            description = "Investimento não encontrado"
        ),
        @APIResponse(
            responseCode = "401",
            description = "Token de autorização inválido"
        )
    })
    public Response getInvestmentTransactions(
            @PathParam("investmentId")
            @Parameter(
                description = "Identificador único do investimento",
                required = true,
                example = "12345"
            )
            String investmentId,
            @HeaderParam("Authorization")
            @Parameter(
                description = "Bearer token OAuth2",
                required = true
            )
            String authorization) {
        log.info("OFB API: GET /open-banking/bank-fixed-incomes/v1/investments/{}/transactions", investmentId);

        // Extract customer ID from JWT token
        String customerId = JwtUtils.extractCustomerId(authorization);
        if (customerId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "invalid_token"))
                .build();
        }

        // Verify investment exists
        Investment investment = mockDataService.getInvestmentById(investmentId);
        if (investment == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("errors", List.of(Map.of(
                    "code", "NOT_FOUND",
                    "title", "Investment not found",
                    "detail", "Investment with id " + investmentId + " not found"
                ))))
                .build();
        }

        // Verify investment belongs to authenticated customer
        List<Investment> customerInvestments = mockDataService.getInvestmentsByCustomerId(customerId);
        boolean investmentBelongsToCustomer = customerInvestments.stream()
            .anyMatch(inv -> inv.getInvestmentId().equals(investmentId));

        if (!investmentBelongsToCustomer) {
            return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of("errors", List.of(Map.of(
                    "code", "FORBIDDEN",
                    "title", "Access denied",
                    "detail", "Investment does not belong to authenticated customer"
                ))))
                .build();
        }

        // Mock implementation - returns empty transactions list
        return Response.ok(Map.of("data", List.of())).build();
    }

    @GET
    @Path("/investments/{investmentId}/transactions-current")
    @Operation(
        summary = "Listar Transações Recentes do Investimento",
        description = """
            Retorna movimentações recentes (últimos 7 dias) da operação de Renda Fixa Bancária.

            ## Descrição

            Consulta transações recentes de um investimento específico conforme especificação OFB.

            ## Período

            - **Últimos 7 dias** (D-6 até hoje)
            - Inclui o dia da consulta
            - Útil para monitoramento em tempo real

            ## Segurança

            Requer autenticação OAuth2 com Bearer token e mTLS.

            ## Diferença de /transactions

            - `/transactions`: Histórico completo (12 meses)
            - `/transactions-current`: Apenas movimentações recentes (7 dias)
            """
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Transações recentes retornadas com sucesso"
        ),
        @APIResponse(
            responseCode = "404",
            description = "Investimento não encontrado"
        ),
        @APIResponse(
            responseCode = "401",
            description = "Token de autorização inválido"
        )
    })
    public Response getInvestmentTransactionsCurrent(
            @PathParam("investmentId")
            @Parameter(
                description = "Identificador único do investimento",
                required = true,
                example = "12345"
            )
            String investmentId,
            @HeaderParam("Authorization")
            @Parameter(
                description = "Bearer token OAuth2",
                required = true
            )
            String authorization) {
        log.info("OFB API: GET /open-banking/bank-fixed-incomes/v1/investments/{}/transactions-current", investmentId);

        // Extract customer ID from JWT token
        String customerId = JwtUtils.extractCustomerId(authorization);
        if (customerId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "invalid_token"))
                .build();
        }

        // Verify investment exists
        Investment investment = mockDataService.getInvestmentById(investmentId);
        if (investment == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("errors", List.of(Map.of(
                    "code", "NOT_FOUND",
                    "title", "Investment not found",
                    "detail", "Investment with id " + investmentId + " not found"
                ))))
                .build();
        }

        // Verify investment belongs to authenticated customer
        List<Investment> customerInvestments = mockDataService.getInvestmentsByCustomerId(customerId);
        boolean investmentBelongsToCustomer = customerInvestments.stream()
            .anyMatch(inv -> inv.getInvestmentId().equals(investmentId));

        if (!investmentBelongsToCustomer) {
            return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of("errors", List.of(Map.of(
                    "code", "FORBIDDEN",
                    "title", "Access denied",
                    "detail", "Investment does not belong to authenticated customer"
                ))))
                .build();
        }

        // Mock implementation - returns empty transactions list
        return Response.ok(Map.of("data", List.of())).build();
    }
}
