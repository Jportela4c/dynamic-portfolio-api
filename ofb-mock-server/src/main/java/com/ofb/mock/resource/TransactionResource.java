package com.ofb.mock.resource;

import com.ofb.mock.model.Transaction;
import com.ofb.mock.service.MockDataService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@Slf4j
@Path("/api/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Auxiliar - Transações", description = "Endpoints auxiliares para testes e desenvolvimento (não fazem parte da spec OFB)")
public class TransactionResource {

    @Inject
    MockDataService mockDataService;

    @GET
    @Path("/{cpf}")
    @Operation(
        summary = "Listar Transações por CPF",
        description = """
            Endpoint auxiliar para listar transações mock de um cliente por CPF.

            ## Propósito

            Este é um endpoint auxiliar para facilitar testes e desenvolvimento.
            **Não faz parte da especificação oficial Open Finance Brasil.**

            Retorna histórico de transações mock para cenários de teste.

            ## Uso

            Use este endpoint para:

            - Verificar transações de teste disponíveis
            - Simular histórico de movimentações
            - Testes de integração

            ## Dados Retornados

            Array de transações contendo:

            - Data da transação
            - Tipo (aplicação, resgate, etc.)
            - Valor
            - Produto relacionado
            """
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Transações encontradas",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(
                    value = "{\"data\":[{\"date\":\"2025-01-15\",\"type\":\"aplicacao\",\"amount\":5000.00,\"product\":\"CDB\"}]}"
                )
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "Cliente não encontrado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(
                    value = "{\"error\":\"Customer not found\"}"
                )
            )
        )
    })
    public Response getTransactionsByCpf(
            @PathParam("cpf")
            @Parameter(
                description = "CPF do cliente (apenas números)",
                required = true,
                example = "12345678901"
            )
            String cpf) {
        log.info("Fetching transactions for CPF: {}", cpf);

        if (!mockDataService.customerExists(cpf)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Customer not found"))
                    .build();
        }

        List<Transaction> transactions = mockDataService.getTransactionsByCpf(cpf);
        return Response.ok(Map.of("data", transactions)).build();
    }
}
