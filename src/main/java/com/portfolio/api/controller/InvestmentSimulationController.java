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
import org.springframework.security.access.prepost.PreAuthorize;
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
        description = """
            Simula um investimento financeiro validando o produto no banco de dados e calculando retornos esperados.

            <details>
            <summary><strong>Algoritmo de Simulação - 5 Etapas</strong> - Validação, busca, cálculo de juros compostos</summary>

            ### 1. Validação de Entrada
            - **Parâmetros obrigatórios**: clienteId, valor, prazoMeses, tipoProduto
            - **Validações**:
              - clienteId > 0
              - valor > 0 (positivo, não-nulo)
              - prazoMeses: 1-360 meses
              - tipoProduto: enum válido (CDB, LCI, LCA, TESOURO_DIRETO, FUNDO, DEBENTURE)

            ### 2. Busca de Produto Compatível
            - **Critérios de matching**:
              1. **Tipo**: tipoProduto deve corresponder ao tipo do produto no banco
              2. **Valor**: valor deve estar dentro do range [valorMinimo, valorMaximo] do produto
              3. **Prazo**: prazoMeses deve estar dentro do range [prazoMinimo, prazoMaximo] do produto
            - **Lógica**: `WHERE tipo = ? AND valor BETWEEN valorMinimo AND valorMaximo AND prazo BETWEEN prazoMinimo AND prazoMaximo`
            - **Erro 404**: Se nenhum produto satisfaz os critérios

            ### 3. Validação de Valor Mínimo
            - **Regra**: `valor >= produto.valorMinimo`
            - **Erro 400**: Se valor é menor que o mínimo permitido para o produto
            - **Exemplo**: CDB pode exigir R$ 1.000,00 mínimo

            ### 4. Cálculo de Juros Compostos
            - **Fórmula matemática**:
              ```
              Valor Final = Valor Inicial × (1 + taxa_mensal)^meses

              Onde:
              - taxa_mensal = rentabilidade_anual / 12
              - meses = prazoMeses
              ```
            - **Exemplo prático**:
              ```
              Investimento: R$ 10.000,00
              Taxa anual: 12% (0.12)
              Prazo: 12 meses

              Taxa mensal = 0.12 / 12 = 0.01 (1% ao mês)
              Valor Final = 10.000 × (1.01)^12 = R$ 11.268,25
              Rentabilidade = R$ 1.268,25 (12.68%)
              ```
            - **Precisão**: Cálculo com 10 dígitos decimais, arredondamento para 2 casas (HALF_UP)

            ### 5. Persistência e Resposta
            - **Salva no banco**:
              - clienteId, produtoId, produtoNome
              - valorInvestido, valorFinal, prazoMeses
              - dataSimulacao (timestamp atual)
            - **Retorna**:
              - **produtoValidado**: Produto encontrado (id, nome, tipo, rentabilidade, risco)
              - **resultadoSimulacao**: Valores calculados (valorFinal, rentabilidadeEfetiva, prazoMeses)
              - **dataSimulacao**: Timestamp da simulação

            </details>

            <details>
            <summary><strong>Produtos Disponíveis</strong> - CDB, LCI, LCA, Tesouro, Fundos, Debêntures</summary>

            | Tipo | Rentabilidade Típica | Risco | Exemplo |
            |------|---------------------|-------|---------|
            | CDB | 10-13% a.a. | Baixo-Médio | CDB Banco Pan 12% |
            | LCI | 9-11% a.a. | Baixo | LCI Bradesco 10% |
            | LCA | 9-11% a.a. | Baixo | LCA Santander 9.5% |
            | TESOURO_DIRETO | 6-13% a.a. | Muito Baixo | Tesouro IPCA+ 2029 |
            | FUNDO | 8-15% a.a. | Médio-Alto | Fundo DI XP |
            | DEBENTURE | 12-16% a.a. | Alto | Debênture Light |

            </details>

            <details>
            <summary><strong>Exemplo de Resposta</strong> - JSON de retorno da simulação</summary>

            ```json
            {
              "produtoValidado": {
                "id": 123,
                "nome": "CDB Banco Pan 120% CDI",
                "tipo": "CDB",
                "rentabilidade": 0.12,
                "risco": "MEDIO"
              },
              "resultadoSimulacao": {
                "valorFinal": 11268.25,
                "rentabilidadeEfetiva": 0.12,
                "prazoMeses": 12
              },
              "dataSimulacao": "2025-11-21T10:30:00"
            }
            ```

            </details>

            <details>
            <summary><strong>Erros Possíveis</strong> - 400, 404, 500</summary>

            - **400**: Dados inválidos (valor/prazo fora dos limites, campos obrigatórios ausentes)
            - **404**: Nenhum produto encontrado para os critérios especificados
            - **500**: Erro interno no cálculo ou persistência

            </details>
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Simulação realizada com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SimulationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos na requisição",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.portfolio.api.model.dto.response.ValidationErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado - usuário não autorizado a simular para este cliente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.portfolio.api.model.dto.response.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado para os parâmetros informados",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.portfolio.api.model.dto.response.ErrorResponse.class)))
    })
    @PreAuthorize("@authorizationValidator.canAccessCustomer(authentication, #request.clienteId)")
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
