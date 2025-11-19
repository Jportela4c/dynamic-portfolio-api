package com.ofb.mock.resource;

import com.ofb.mock.model.Customer;
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

import java.util.Map;

@Slf4j
@Path("/api/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Auxiliar - Clientes", description = "Endpoints auxiliares para testes e desenvolvimento (não fazem parte da spec OFB)")
public class CustomerResource {

    @Inject
    MockDataService mockDataService;

    @GET
    @Path("/{cpf}")
    @Operation(
        summary = "Buscar Cliente por CPF",
        description = """
            Endpoint auxiliar para buscar dados mock de cliente por CPF.

            ## Propósito

            Este é um endpoint auxiliar para facilitar testes e desenvolvimento.
            **Não faz parte da especificação oficial Open Finance Brasil.**

            Permite consultar dados mock de clientes para uso em cenários de teste.

            ## Uso

            Use este endpoint para:

            - Verificar dados de clientes mock disponíveis
            - Testes de integração
            - Desenvolvimento local

            ## Dados Retornados

            - Nome do cliente
            - CPF
            - Perfil de risco
            - Outras informações mock relevantes
            """
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Cliente encontrado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(
                    value = "{\"data\":{\"cpf\":\"12345678901\",\"name\":\"João Silva\",\"riskProfile\":\"Conservador\"}}"
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
    public Response getCustomerByCpf(
            @PathParam("cpf")
            @Parameter(
                description = "CPF do cliente (apenas números)",
                required = true,
                example = "12345678901"
            )
            String cpf) {
        log.info("Fetching customer for CPF: {}", cpf);

        Customer customer = mockDataService.getCustomerByCpf(cpf);
        if (customer == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Customer not found"))
                    .build();
        }

        return Response.ok(Map.of("data", customer)).build();
    }
}
