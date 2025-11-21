package com.portfolio.api.controller;

import com.portfolio.api.model.dto.request.SimulationRequest;
import com.portfolio.api.model.dto.response.SimulationResponse;
import com.portfolio.api.service.SimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Simulação de Investimentos", description = "Endpoints para simular investimentos em produtos financeiros")
public class InvestmentSimulationController {

    private final SimulationService simulationService;

    public InvestmentSimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @Operation(
        summary = "Simular investimento",
        description = "Simula um investimento com base nos parâmetros fornecidos. Valida o produto contra o banco de dados e calcula os retornos esperados."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Simulação realizada com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SimulationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos na requisição",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.portfolio.api.model.dto.response.ValidationErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado para os parâmetros informados",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.portfolio.api.model.dto.response.ErrorResponse.class)))
    })
    @PostMapping("/simular-investimento")
    public ResponseEntity<SimulationResponse> simulateInvestment(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados da simulação de investimento. Selecione um exemplo abaixo ou digite seu próprio JSON.",
            required = true,
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "CDB - Investimento Conservador",
                        summary = "CDB de 12 meses - R$ 10 mil",
                        description = "Certificado de Depósito Bancário: baixo risco, liquidez após período",
                        value = """
                            {
                              "clienteId": 1,
                              "valor": 10000.00,
                              "prazoMeses": 12,
                              "tipoProduto": "CDB"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "LCI - Investimento Isento de IR",
                        summary = "LCI de 24 meses - R$ 25 mil",
                        description = "Letra de Crédito Imobiliário: isento de IR, financiamento imobiliário",
                        value = """
                            {
                              "clienteId": 1,
                              "valor": 25000.00,
                              "prazoMeses": 24,
                              "tipoProduto": "LCI"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "LCA - Agronegócio Isento",
                        summary = "LCA de 18 meses - R$ 50 mil",
                        description = "Letra de Crédito do Agronegócio: isento de IR, setor agrícola",
                        value = """
                            {
                              "clienteId": 1,
                              "valor": 50000.00,
                              "prazoMeses": 18,
                              "tipoProduto": "LCA"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Tesouro Selic - Baixo Risco",
                        summary = "Tesouro Selic - R$ 5 mil",
                        description = "Título público pós-fixado: alta liquidez, segue taxa Selic",
                        value = """
                            {
                              "clienteId": 1,
                              "valor": 5000.00,
                              "prazoMeses": 6,
                              "tipoProduto": "TESOURO_DIRETO"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Tesouro IPCA+ - Longo Prazo",
                        summary = "Tesouro IPCA+ 2035 - R$ 100 mil",
                        description = "Título público híbrido: proteção contra inflação + taxa fixa",
                        value = """
                            {
                              "clienteId": 1,
                              "valor": 100000.00,
                              "prazoMeses": 120,
                              "tipoProduto": "TESOURO_DIRETO"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Fundo Renda Fixa - Moderado",
                        summary = "Fundo DI - R$ 15 mil",
                        description = "Fundo de investimento: gestão profissional, diversificação",
                        value = """
                            {
                              "clienteId": 1,
                              "valor": 15000.00,
                              "prazoMeses": 12,
                              "tipoProduto": "FUNDO"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Investimento Pequeno - Iniciante",
                        summary = "CDB 6 meses - R$ 1 mil",
                        description = "Simulação para investidor iniciante com capital reduzido",
                        value = """
                            {
                              "clienteId": 1,
                              "valor": 1000.00,
                              "prazoMeses": 6,
                              "tipoProduto": "CDB"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Investimento Alto Valor",
                        summary = "LCI 36 meses - R$ 500 mil",
                        description = "Simulação para investidor com alto patrimônio",
                        value = """
                            {
                              "clienteId": 1,
                              "valor": 500000.00,
                              "prazoMeses": 36,
                              "tipoProduto": "LCI"
                            }
                            """
                    )
                }
            )
        )
        @Valid @RequestBody SimulationRequest request) {
        SimulationResponse response = simulationService.simulateInvestment(request);
        return ResponseEntity.ok(response);
    }
}
