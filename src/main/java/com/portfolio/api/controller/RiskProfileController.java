package com.portfolio.api.controller;

import com.portfolio.api.model.dto.response.RiskProfileResponse;
import com.portfolio.api.service.RiskProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@Tag(name = "Perfil de Risco", description = "Endpoints para consulta de perfil de risco de clientes")
public class RiskProfileController {

    private final RiskProfileService riskProfileService;

    public RiskProfileController(RiskProfileService riskProfileService) {
        this.riskProfileService = riskProfileService;
    }

    @Operation(
        summary = "Consultar perfil de risco dinâmico",
        description = """
            Calcula o perfil de risco do cliente baseado em comportamento real de investimentos (Open Finance Brasil).

            ## Algoritmo de Classificação de Risco - 5 Fatores

            O sistema analisa 5 dimensões do comportamento do investidor, cada uma contribuindo com um peso específico
            para a pontuação final (0-100):

            ### 1. Volume de Investimentos (25% do score)
            - **Objetivo**: Medir capacidade financeira e tolerância ao risco
            - **Thresholds** (baseado em ANBIMA/CVM 539/2013):
              - R$ 0 - 10k: Score 20-30 (Conservador)
              - R$ 10k - 50k: Score 30-40 (Conservador+)
              - R$ 50k - 150k: Score 40-60 (Moderado)
              - R$ 150k - 500k: Score 60-80 (Moderado-Agressivo)
              - R$ 500k - 1M: Score 80-95 (Agressivo)
              - R$ 1M+: Score 100 (Investidor Profissional)
            - **Lógica**: Maior volume = maior capacidade de absorver volatilidade

            ### 2. Frequência de Transações (20% do score)
            - **Objetivo**: Avaliar engajamento e experiência do investidor
            - **Cálculo**: Média de transações por mês (baseado em OFB transaction data)
            - **Scoring**:
              - 0-1 txn/mês: Score 20 (Passivo)
              - 1-3 txn/mês: Score 40 (Moderado)
              - 3-6 txn/mês: Score 60 (Ativo)
              - 6-12 txn/mês: Score 80 (Muito Ativo)
              - 12+ txn/mês: Score 100 (Day Trader)
            - **Lógica**: Maior frequência = maior conhecimento e tolerância a risco

            ### 3. Preferência por Risco do Produto (30% do score) - **MAIOR PESO**
            - **Objetivo**: Identificar preferência histórica por produtos de risco (THE SPEC: "Preferência por liquidez ou rentabilidade")
            - **Classificação de Produtos**:
              - **Renda Fixa Garantida** (Conservador): CDB, LCI, LCA, Tesouro Direto
              - **Renda Fixa Não-Garantida** (Moderado): Debêntures, CRI, CRA
              - **Fundos** (Moderado-Agressivo): Multimercado, Renda Fixa, DI
              - **Renda Variável** (Agressivo): Ações, BDRs, ETFs, FIIs
            - **Lógica**: Distribuição de investimentos entre categorias de risco

            ### 4. Preferência por Liquidez (15% do score)
            - **Objetivo**: Avaliar necessidade de resgate rápido (THE SPEC: "Preferência por liquidez")
            - **Scoring**:
              - Alta liquidez (>70%): Score 30 (Conservador)
              - Média liquidez (40-70%): Score 60 (Moderado)
              - Baixa liquidez (<40%): Score 90 (Agressivo)
            - **Lógica**: Baixa liquidez = maior tolerância a lock-up periods

            ### 5. Horizonte de Investimento (10% do score)
            - **Objetivo**: Medir visão de longo prazo vs. curto prazo
            - **Cálculo**: Prazo médio até vencimento dos investimentos
            - **Scoring**:
              - < 6 meses: Score 30 (Curto prazo)
              - 6-24 meses: Score 60 (Médio prazo)
              - > 24 meses: Score 90 (Longo prazo)
            - **Lógica**: Longo prazo permite maior exposição a volatilidade

            ## Fórmula Final
            ```
            Score Total = (AmountScore × 0.25) +
                         (FrequencyScore × 0.20) +
                         (ProductRiskScore × 0.30) +
                         (LiquidityScore × 0.15) +
                         (HorizonScore × 0.10)
            ```

            ## Classificação do Perfil
            - **0-40**: Conservador - Baixo risco, foco em segurança e liquidez
            - **41-70**: Moderado - Equilíbrio entre segurança e rentabilidade
            - **71-100**: Agressivo - Alto risco, foco em rentabilidade máxima

            ## Fonte de Dados
            - Investimentos e transações obtidos via **Open Finance Brasil (OFB)**
            - Dados em tempo real de todas as instituições financeiras autorizadas
            - CPF do cliente usado para autenticação OFB

            ## Exemplo de Resposta
            ```json
            {
              "clienteId": 1,
              "perfil": "Moderado",
              "pontuacao": 52,
              "descricao": "Perfil equilibrado entre segurança e rentabilidade."
            }
            ```
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil calculado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RiskProfileResponse.class))),
        @ApiResponse(responseCode = "400", description = "ID do cliente inválido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.portfolio.api.model.dto.response.ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado - usuário não autorizado a acessar dados deste cliente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.portfolio.api.model.dto.response.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado ou CPF não mapeado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.portfolio.api.model.dto.response.ErrorResponse.class))),
        @ApiResponse(responseCode = "503", description = "Serviço OFB indisponível - tente novamente mais tarde",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.portfolio.api.model.dto.response.ErrorResponse.class)))
    })
    @PreAuthorize("@authorizationValidator.canAccessCustomer(authentication, #clienteId)")
    @GetMapping("/perfil-risco/{clienteId}")
    public ResponseEntity<RiskProfileResponse> getRiskProfile(
        @Parameter(description = "ID do cliente", example = "1", required = true)
        @PathVariable Long clienteId) {

        // Authorization already validated by @PreAuthorize!
        // If we reached here, access is permitted
        RiskProfileResponse response = riskProfileService.calculateRiskProfile(clienteId);
        return ResponseEntity.ok(response);
    }
}
