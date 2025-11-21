# API de Portfólio Dinâmico

Sistema de análise de perfil de risco e recomendação de investimentos para produtos financeiros brasileiros com integração Open Finance Brasil (OFB).

---

## ⚡ Início Rápido (Um Único Comando)

**Execute isso e está pronto (funciona em qualquer sistema operacional):**

```bash
./run.cmd
```

Pronto! A API estará rodando em `http://localhost:8080/api/v1`

**Testar no navegador:**
- **API Principal**: http://localhost:8080/api/v1/swagger-ui/index.html
- **OFB Mock Server**: http://localhost:8089/q/swagger-ui (Servidor simulado Open Finance Brasil)

---

## 📋 Índice

- [Início Rápido](#-início-rápido-um-único-comando)
- [Visão Geral do Sistema](#-visão-geral-do-sistema)
- [Arquitetura](#-arquitetura)
- [Fluxos de Autenticação](#-fluxos-de-autenticação)
- [Motor de Análise de Perfil de Risco](#-motor-de-análise-de-perfil-de-risco)
- [Algoritmo de Simulação](#-algoritmo-de-simulação)
- [Sistema de Recomendação](#-sistema-de-recomendação)
- [Servidor Mock OFB](#-servidor-mock-ofb)
- [Como Testar a API](#como-testar-a-api)
- [Como Rodar a Aplicação](#como-rodar-a-aplicação)
- [Endpoints da API](#endpoints-da-api)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Banco de Dados](#banco-de-dados)
- [Desenvolvimento Local](#desenvolvimento-local)
- [Testes](#testes)
- [Solução de Problemas](#solução-de-problemas)

---

## 🎯 Visão Geral do Sistema

Sistema completo de análise de investimentos que:

1. **Classifica automaticamente** o perfil de risco do cliente baseado no comportamento real de investimentos
2. **Busca dados de investimentos** de múltiplas instituições financeiras via Open Finance Brasil
3. **Simula investimentos** com validação de produtos e cálculo de rentabilidade
4. **Recomenda produtos** adequados ao perfil de risco identificado
5. **Fornece telemetria** sobre o uso do sistema

### Status Atual da Implementação

✅ **100% Funcional:**
- THE SPEC API completa (7 endpoints)
- Motor de perfil de risco dinâmico (5 scorers)
- Integração OFB completa com 72 investimentos e 436 transações mockados
- Autenticação OAuth2 Authorization Code Flow
- Sistema de resiliência (Circuit Breaker, Retry, Cache)
- Testes unitários e de integração

📊 **Dados Mock Realistas:**
- 5 categorias de investimentos OFB: Bank Fixed Incomes, Treasury Titles, Funds, Variable Incomes, Credit Fixed Incomes
- Dados gerados com json-schema-faker seguindo specs oficiais OFB
- Cálculos de IR (Imposto de Renda) brasileiro aplicados

---

## 🏗️ Arquitetura

### Arquitetura de Duas Camadas (THE SPEC vs OFB)

O sistema possui **duas camadas OAuth2 completamente independentes**:

```
┌──────────────────────────────────────────────────────────────────┐
│                        CAMADA 1: THE SPEC                        │
│                  (API Principal - CAIXA Dashboard)               │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  PROPÓSITO:    Backend do Dashboard de Investimentos CAIXA      │
│  CLIENTE:      Frontend Web/Mobile                              │
│  ENDPOINTS:    GET /perfil-risco/{clienteId}                    │
│               GET /investimentos/{clienteId}                    │
│               POST /simular-investimento                        │
│  AUTENTICAÇÃO: OAuth2 Authorization Code Flow + JWT            │
│  IDENTIDADE:   clienteId (Long) - ID interno do sistema        │
│  PERFIL:       dev / prod - SEM mudança de endpoints           │
│                                                                  │
└──────────────────────┬───────────────────────────────────────────┘
                       │ OAuth2 OFB
                       │ (CPF-based)
                       ▼
┌──────────────────────────────────────────────────────────────────┐
│                         CAMADA 2: OFB                            │
│            (Integração Open Finance Brasil - Banks)             │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  PROPÓSITO:    Buscar dados de investimentos dos bancos         │
│  CLIENTE:      Backend da API Principal apenas                  │
│  ENDPOINTS:    GET /bank-fixed-incomes/v1/investments           │
│               GET /funds/v1/investments                         │
│               GET /treasury-titles/v1/investments               │
│               GET /variable-incomes/v1/investments              │
│               GET /credit-fixed-incomes/v1/investments          │
│  AUTENTICAÇÃO: OAuth2 PAR + mTLS + JWS                          │
│  IDENTIDADE:   CPF (String) - Documento do cliente no JWT      │
│  PERFIL:       dev (mock) / prod (bancos reais)                │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

### Diferenças Críticas Entre Camadas

| Aspecto | THE SPEC (Camada 1) | OFB (Camada 2) |
|---------|---------------------|----------------|
| **O que é?** | API CAIXA para clientes | Dados de bancos externos |
| **Quem chama?** | Frontend (navegador/app) | Backend da API Principal |
| **ID do Cliente** | `clienteId` (Long) | `cpf` (String no JWT) |
| **Muda com profile?** | NÃO - sempre os mesmos endpoints | SIM - dev=mock, prod=bancos reais |
| **Spec fonte** | Especificação do desafio | OpenAPI OFB oficial |

### Profile Swapping (dev vs prod)

**Perfil `dev` (Demonstração):**
- OFB Mock Server local (72 investimentos mockados)
- Auto-autorização de consentimentos (1 segundo)
- Usuário ADMIN com acesso multi-cliente habilitado
- Frontend → Main API → Mock OFB

**Perfil `prod` (Produção):**
- Bancos OFB reais via mTLS
- Consentimento real com redirecionamento para banco
- ADMIN desabilitado (apenas CUSTOMER)
- Frontend → Main API → Bancos OFB Reais

**IMPORTANTE:** Frontend **não vê diferença** - mesmos endpoints, mesmos JSONs.

---

## 🔐 Fluxos de Autenticação

### 1. Autenticação do Frontend (OAuth2 Authorization Code Flow)

Autenticação **padrão indústria** para aplicações web seguras.

```
┌─────────────┐                                      ┌─────────────┐
│   Frontend  │                                      │  Main API   │
│  (Browser)  │                                      │             │
└──────┬──────┘                                      └──────┬──────┘
       │                                                    │
       │ 1. GET /oauth2/authorize?client_id=...           │
       │      &redirect_uri=...&response_type=code         │
       │───────────────────────────────────────────────────▶│
       │                                                    │
       │ 2. Redirect to Login Page                         │
       │◀───────────────────────────────────────────────────│
       │                                                    │
       │ 3. POST /login (email + password)                 │
       │───────────────────────────────────────────────────▶│
       │                                                    │
       │                              4. Validate User     │
       │                                 (Database lookup) │
       │                                                    │
       │ 5. Redirect to redirect_uri?code=ABC123           │
       │◀───────────────────────────────────────────────────│
       │                                                    │
       │ 6. POST /oauth2/token                             │
       │    grant_type=authorization_code                  │
       │    code=ABC123                                    │
       │───────────────────────────────────────────────────▶│
       │                                                    │
       │ 7. { "access_token": "JWT...",                    │
       │      "token_type": "Bearer",                      │
       │      "expires_in": 3600 }                         │
       │◀───────────────────────────────────────────────────│
       │                                                    │
       │ 8. API calls with Authorization: Bearer JWT...    │
       │───────────────────────────────────────────────────▶│
       │                                                    │
```

**JWT Claims (Camada 1):**
```json
{
  "sub": "joao.silva@example.com",
  "userId": 1,
  "cpf": "12345678901",
  "role": "CUSTOMER",
  "scope": ["read", "write", "openid", "profile"],
  "exp": 1640000000,
  "iat": 1639996400
}
```

**Credenciais de Teste:**

| Usuário | Email | Senha | Role | Cliente ID | Perfil de Risco |
|---------|-------|-------|------|------------|-----------------|
| João Silva | joao.silva@example.com | customer123 | CUSTOMER | 1 | Conservador |
| Maria Santos | maria.santos@example.com | customer123 | CUSTOMER | 2 | Moderado |
| Pedro Costa | pedro.costa@example.com | customer123 | CUSTOMER | 3 | Agressivo |
| Ana Oliveira | ana.oliveira@example.com | customer123 | CUSTOMER | 4 | Conservador |
| Carlos Lima | carlos.lima@example.com | customer123 | CUSTOMER | 5 | Agressivo |
| Administrador | admin@demo.local | admin123 | ADMIN | 999 | N/A (acesso a todos os clientes) |

### 2. Autenticação OFB (Backend → Bancos)

Fluxo OAuth2 PAR (Pushed Authorization Request) conforme FAPI.

```
┌─────────────┐                                      ┌─────────────┐
│  Main API   │                                      │  OFB Bank   │
│  (Backend)  │                                      │  (Mock/Real)│
└──────┬──────┘                                      └──────┬──────┘
       │                                                    │
       │ 1. POST /oauth2/par                               │
       │    client_id=portfolio-api-client                 │
       │    scope=investments:read                         │
       │    cpf={customer_cpf}                             │
       │───────────────────────────────────────────────────▶│
       │                                                    │
       │ 2. { "request_uri": "urn:...",                    │
       │      "expires_in": 90 }                           │
       │◀───────────────────────────────────────────────────│
       │                                                    │
       │ 3. GET /oauth2/authorize?request_uri=...          │
       │───────────────────────────────────────────────────▶│
       │                                                    │
       │                         4. Auto-authorize (dev)   │
       │                            OR User consent (prod) │
       │                                                    │
       │ 5. Redirect with authorization_code               │
       │◀───────────────────────────────────────────────────│
       │                                                    │
       │ 6. POST /oauth2/token                             │
       │    grant_type=authorization_code                  │
       │    code=...                                       │
       │───────────────────────────────────────────────────▶│
       │                                                    │
       │ 7. { "access_token": "JWT...",                    │
       │      "id_token": "JWE(JWS(cpf))" }                │
       │◀───────────────────────────────────────────────────│
       │                                                    │
       │ 8. GET /bank-fixed-incomes/v1/investments         │
       │    Authorization: Bearer JWT...                   │
       │───────────────────────────────────────────────────▶│
       │                                                    │
       │ 9. JWS-signed response with investments           │
       │◀───────────────────────────────────────────────────│
       │                                                    │
```

**JWT Claims (Camada 2 - OFB):**
```json
{
  "sub": "12345678901",
  "cpf": "12345678901",
  "scope": ["investments:read", "customers:read"],
  "exp": 1640000000,
  "iat": 1639996400
}
```

**Importante:** O CPF **nunca** aparece em request/response bodies, **apenas no JWT**.

---

## 🧠 Motor de Análise de Perfil de Risco

Sistema de classificação **dinâmica** baseado no comportamento **real** de investimentos do cliente.

### Algoritmo Multi-Fator

O perfil de risco é calculado através de **5 scorers independentes** com pesos definidos:

```java
FACTOR_WEIGHTS = {
    "amount":       0.25,  // Volume de investimentos
    "frequency":    0.20,  // Frequência de transações
    "product_risk": 0.30,  // Preferência por produtos de risco
    "liquidity":    0.15,  // Preferência por liquidez
    "horizon":      0.10   // Horizonte de investimento
}

TOTAL_SCORE = (amount_score × 0.25) +
              (frequency_score × 0.20) +
              (product_risk_score × 0.30) +
              (liquidity_score × 0.15) +
              (horizon_score × 0.10)

PERFIL = {
    TOTAL_SCORE ≤ 40  → CONSERVADOR
    40 < TOTAL_SCORE ≤ 70 → MODERADO
    TOTAL_SCORE > 70  → AGRESSIVO
}
```

### 1. AmountScorer (Peso: 0.25) - Volume de Investimentos

**Premissa:** Maior volume geralmente indica maior capacidade de tolerar risco.

**Thresholds baseados em ANBIMA / CVM Instrução 539/2013:**

| Faixa de Valor | Score | Classificação |
|----------------|-------|---------------|
| < R$ 10.000 | 10-25 | Capacidade muito baixa |
| R$ 10k - R$ 50k | 25-40 | Capacidade baixa |
| R$ 50k - R$ 150k | 40-60 | Capacidade moderada |
| R$ 150k - R$ 500k | 60-80 | Capacidade moderada-alta |
| R$ 500k - R$ 1M | 80-95 | Capacidade alta |
| > R$ 1M | 100 | Investidor profissional |

**Cálculo:** Interpolação linear entre os thresholds.

### 2. FrequencyScorer (Peso: 0.20) - Frequência de Transações

**Premissa:** Maior frequência de transações correlaciona com maior tolerância ao risco (Barber & Odean, 2000).

**Cálculo:**
```
transactions_per_year = total_transactions / years_active

Score = {
    ≥ 12 transações/ano (mensal+)   → 100 (Agressivo)
    4-12 transações/ano (trimestral)  → 70 (Moderado-Agressivo)
    1-4 transações/ano (anual)       → 40 (Moderado)
    < 1 transação/ano                → 20 (Conservador)
}
```

**Fonte de dados:** Campos `transactionCount`, `firstTransactionDate`, `lastTransactionDate` de cada investimento.

### 3. ProductRiskScorer (Peso: 0.30) - Preferência por Produtos de Risco

**Premissa:** Escolhas reais de produtos revelam apetite ao risco (peso mais alto: 30%).

**Níveis de risco por tipo de produto (baseado em classificação ANBIMA):**

| Produto | Nível de Risco | Justificativa |
|---------|----------------|---------------|
| Poupança | 1 | Garantia FGC, liquidez imediata |
| Tesouro Selic | 2 | Risco soberano, liquidez diária |
| Tesouro Prefixado/IPCA | 2 | Risco soberano, liquidez D+1 |
| LCI/LCA | 3 | Isenção IR, garantia FGC |
| RDB | 3 | Garantia FGC, baixa liquidez |
| CDB | 4 | Garantia FGC, risco bancário |
| Fundo Renda Fixa | 5 | Marcação a mercado, sem garantia |
| Fundo Cambial | 6 | Volatilidade cambial |
| Fundo Multimercado | 7 | Estratégias diversas, volatilidade média |
| Fundo de Ações | 9 | Alta volatilidade, risco de mercado |

**Cálculo (média ponderada pelo valor investido):**
```
weighted_risk = Σ(risk_level_i × value_i) / total_value
score = (weighted_risk / 10) × 100
```

### 4. LiquidityScorer (Peso: 0.15) - Preferência por Liquidez

**Premissa:** Menor necessidade de liquidez = maior horizonte = maior tolerância ao risco.

**Níveis de liquidez (1=alta liquidez, 10=baixa liquidez):**

| Produto | Nível | Liquidez Real |
|---------|-------|---------------|
| Poupança | 1 | D+0 (imediato) |
| Tesouro Selic | 2 | D+1 |
| Fundo Renda Fixa | 3 | D+1 a D+30 (cotização) |
| CDB/LCI/LCA | 5 | D+1 a D+90 (conforme contrato) |
| Tesouro Prefixado/IPCA | 7 | D+1 (com risco de marcação) |
| Ações/Fundos de Ações | 8 | D+2 (liquidez dependente do mercado) |
| Tesouro RendA+/Educa+ | 9 | Longo prazo (2030+) |

**Cálculo (média ponderada pelo valor investido):**
```
weighted_liquidity = Σ(liquidity_level_i × value_i) / total_value
score = (weighted_liquidity / 10) × 100
```

### 5. HorizonScorer (Peso: 0.10) - Horizonte de Investimento

**Premissa:** Investimentos de longo prazo indicam maior tolerância ao risco.

**Cálculo:**
```
average_years_to_maturity = Σ(years_to_maturity_i × value_i) / total_value

Score = {
    > 10 anos  → 100 (Muito longo prazo)
    5-10 anos  → 70  (Longo prazo)
    2-5 anos   → 50  (Médio prazo)
    1-2 anos   → 30  (Curto prazo)
    < 1 ano    → 10  (Curtíssimo prazo)
}
```

### Resiliência e Performance

O sistema implementa padrões de resiliência para garantir alta disponibilidade:

**Circuit Breaker:**
- Protege contra falhas no provedor OFB
- Após 3 falhas consecutivas, abre o circuito por 30 segundos
- Tenta semi-abrir após intervalo, testando recuperação

**Retry:**
- 3 tentativas com backoff exponencial (1s, 2s, 4s)
- Protege contra falhas temporárias de rede

**Cache (Dois Níveis):**
1. **Cache em memória (Caffeine):** 5 minutos, 1000 entradas
2. **Cache persistente (Database):** 24 horas, fallback quando OFB indisponível

**Exemplo de fluxo com falha:**
```
1. Cliente solicita perfil de risco
2. Main API tenta buscar investimentos via OFB → FALHA
3. Retry 1 (após 1s) → FALHA
4. Retry 2 (após 2s) → FALHA
5. Retry 3 (após 4s) → FALHA
6. Circuit Breaker abre
7. Fallback: busca cache persistente (database)
8. Retorna dados cacheados (24h válido)
9. Cálculo de perfil executado normalmente
```

---

## 🎲 Algoritmo de Simulação

Simula o resultado futuro de um investimento com validação de produto e cálculo de juros compostos.

### Fluxo de Simulação

```
┌─────────────────────────────────────────────────────────┐
│ 1. Validação de Request                                 │
│    - clienteId existe?                                  │
│    - tipoProduto válido?                                │
│    - valor > 0?                                         │
│    - prazoMeses > 0?                                    │
└────────────────┬────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────┐
│ 2. Busca de Produto Adequado                            │
│    SELECT * FROM products                               │
│    WHERE tipo = :tipo                                   │
│      AND ativo = true                                   │
│      AND valor_minimo <= :valor                         │
│      AND prazo_minimo_meses <= :prazo                   │
│    ORDER BY rentabilidade DESC                          │
│    LIMIT 1                                              │
└────────────────┬────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────┐
│ 3. Cálculo de Rentabilidade (Juros Compostos)          │
│                                                         │
│    FV = PV × (1 + r)^n                                  │
│                                                         │
│    Onde:                                                │
│    FV = Valor Final (Future Value)                     │
│    PV = Valor Investido (Present Value)                │
│    r  = Taxa mensal (rentabilidade / 12)               │
│    n  = Prazo em meses                                  │
│                                                         │
│    Exemplo:                                             │
│    PV = R$ 10.000,00                                    │
│    Rentabilidade anual = 12% (0.12)                    │
│    Prazo = 12 meses                                     │
│    r = 0.12 / 12 = 0.01                                 │
│    FV = 10000 × (1.01)^12 = R$ 11.268,25                │
└────────────────┬────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────┐
│ 4. Persistência                                         │
│    - Salva simulação no banco (simulations table)      │
│    - Registra telemetria (tempo de resposta)           │
└────────────────┬────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────┐
│ 5. Response                                             │
│    {                                                    │
│      "produtoValidado": { id, nome, tipo, ... },        │
│      "resultadoSimulacao": {                            │
│        "valorFinal": 11268.25,                          │
│        "rentabilidadeEfetiva": 0.12,                    │
│        "prazoMeses": 12                                 │
│      },                                                 │
│      "dataSimulacao": "2025-01-15T10:30:00Z"            │
│    }                                                    │
└─────────────────────────────────────────────────────────┘
```

### Validações Aplicadas

1. **Tipo de Produto:** Deve existir na tabela `products` e estar ativo
2. **Valor Mínimo:** `valor >= produto.valor_minimo`
3. **Prazo Mínimo:** `prazo >= produto.prazo_minimo_meses`
4. **Cliente Existente:** `clienteId` deve existir na tabela `clients`

### Seleção de Melhor Produto

Quando múltiplos produtos atendem aos critérios, seleciona o de **maior rentabilidade**.

**Exemplo:**
```
Request: tipo=CDB, valor=50000, prazo=12

Produtos candidatos:
- CDB 100% CDI (rent: 0.10, min: 10000, prazo_min: 6)  ← NÃO selecionado
- CDB 110% CDI (rent: 0.11, min: 50000, prazo_min: 12) ← SELECIONADO (maior rentabilidade)
- CDB 95% CDI  (rent: 0.095, min: 5000, prazo_min: 3)  ← NÃO selecionado

Retorna: CDB 110% CDI
```

---

## 💡 Sistema de Recomendação

Sistema simples e direto baseado em **compatibilidade de perfil**.

### Algoritmo de Recomendação

```
┌─────────────────────────────────────────────────────────┐
│ 1. Input: Perfil de Risco                               │
│    - CONSERVADOR                                        │
│    - MODERADO                                           │
│    - AGRESSIVO                                          │
└────────────────┬────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────┐
│ 2. Query Database                                       │
│    SELECT * FROM products                               │
│    WHERE perfil_adequado = :perfil                      │
│      AND ativo = true                                   │
│    ORDER BY rentabilidade DESC                          │
└────────────────┬────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────┐
│ 3. Retorna Lista de Produtos                            │
│    - Ordenados por rentabilidade (maior primeiro)      │
│    - Apenas produtos ativos                             │
│    - Filtrados por perfil adequado                      │
└─────────────────────────────────────────────────────────┘
```

### Mapeamento Perfil → Produtos

| Perfil | Produtos Recomendados | Características |
|--------|----------------------|-----------------|
| **CONSERVADOR** | • Poupança<br>• Tesouro Selic<br>• LCI/LCA<br>• CDB baixo risco | • Alta liquidez<br>• Garantias (FGC/Soberano)<br>• Baixa volatilidade<br>• Rentabilidade até 100% CDI |
| **MODERADO** | • CDB 100-110% CDI<br>• Tesouro IPCA+<br>• Fundos Renda Fixa<br>• Multimercado conservador | • Liquidez moderada<br>• Mix risco/retorno<br>• Rentabilidade 100-120% CDI<br>• Alguns com IR |
| **AGRESSIVO** | • CDB > 120% CDI<br>• Debêntures<br>• Fundos de Ações<br>• Ações<br>• CRI/CRA | • Liquidez variável<br>• Foco em rentabilidade<br>• Aceita volatilidade<br>• Horizonte longo prazo |

### Exemplo de Response

```json
{
  "perfil": "MODERADO",
  "produtos": [
    {
      "id": 5,
      "nome": "CDB 110% CDI - 12 meses",
      "tipo": "CDB",
      "rentabilidade": 0.11,
      "risco": "MEDIO"
    },
    {
      "id": 8,
      "nome": "Tesouro IPCA+ 2029",
      "tipo": "TESOURO_IPCA",
      "rentabilidade": 0.065,
      "risco": "BAIXO"
    }
  ]
}
```

---

## 🏦 Servidor Mock OFB

Este projeto inclui um **servidor mock completo de Open Finance Brasil** para desenvolvimento e testes.

**Documentação completa**: [`ofb-mock-server/README.md`](ofb-mock-server/README.md)

**Swagger UI**: http://localhost:8089/q/swagger-ui

### Conformidade OFB

- ✅ OAuth2 PAR (Pushed Authorization Request)
- ✅ mTLS (autenticação mútua com certificados)
- ✅ JWS (assinatura de respostas PS256)
- ✅ JWE (criptografia de ID tokens)
- ✅ 5 APIs de investimentos (Bank Fixed Incomes, Treasury, Funds, Variable Incomes, Credit)
- ✅ 72 investimentos mockados + 436 transações
- ✅ Cálculos reais de IR brasileiro

⚠️ **Uso educacional apenas** - não usar em produção.

### Integração com Main API

```
Main API                    OFB Mock Server
   │                              │
   │  1. POST /oauth2/par         │
   │  (cpf=12345678901)           │
   │─────────────────────────────▶│
   │                              │
   │  2. { request_uri }          │
   │◀─────────────────────────────│
   │                              │
   │  3. GET /oauth2/authorize    │
   │  ?request_uri=...            │
   │─────────────────────────────▶│
   │                              │
   │  4. Auto-autorização (1s)    │
   │                              │
   │  5. { authorization_code }   │
   │◀─────────────────────────────│
   │                              │
   │  6. POST /oauth2/token       │
   │─────────────────────────────▶│
   │                              │
   │  7. { access_token, id_token }│
   │◀─────────────────────────────│
   │                              │
   │  8. GET /investments         │
   │  Authorization: Bearer...    │
   │─────────────────────────────▶│
   │                              │
   │  9. JWS-signed investments   │
   │◀─────────────────────────────│
```

---

## Como Testar a API

### Forma 1: Usando o Swagger (Mais Fácil - Recomendado)

**Swagger não precisa de autenticação manual!** OAuth2 integrado.

1. Certifique-se que a aplicação está rodando
2. Abra no navegador: http://localhost:8080/api/v1/swagger-ui.html
3. Clique no botão **"Authorize"** (cadeado verde no topo)
4. Preencha:
   - **client_id**: `portfolio-web-app`
   - **client_secret**: `webapp-secret`
   - Marque os escopos: `read`, `write`, `openid`, `profile`
5. Clique em **"Authorize"**
6. Você será redirecionado para página de login
7. Faça login com:
   - **Cliente**: `joao.silva@example.com` / `customer123`
   - **Admin**: `admin@demo.local` / `admin123`
8. Após login, retorna ao Swagger autenticado
9. Teste os endpoints!

### Forma 2: Linha de Comando (curl)

**⚠️ COMPLEXO:** Requer fluxo OAuth2 completo. **Recomendamos usar Swagger UI**.

Se realmente precisa de curl, copie o token do Swagger:
1. Autentique no Swagger UI
2. Abra Developer Tools (F12) → Application → Local Storage
3. Copie o token OAuth2
4. Use nos comandos:

```bash
TOKEN="seu_token_aqui"

# Consultar perfil de risco
curl -X GET "http://localhost:8080/api/v1/perfil-risco/1" \
  -H "Authorization: Bearer $TOKEN"

# Ver investimentos
curl -X GET "http://localhost:8080/api/v1/investimentos/1" \
  -H "Authorization: Bearer $TOKEN"

# Simular investimento
curl -X POST "http://localhost:8080/api/v1/simular-investimento" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": 1,
    "valor": 10000.00,
    "prazoMeses": 12,
    "tipoProduto": "CDB"
  }'

# Ver produtos recomendados
curl -X GET "http://localhost:8080/api/v1/produtos-recomendados/MODERADO" \
  -H "Authorization: Bearer $TOKEN"
```

---

## Como Rodar a Aplicação

### Opção 1: Comando Único (Mais Simples)

```bash
./run.cmd
```

Funciona em Windows, macOS e Linux. Instala Task automaticamente se necessário.

### Opção 2: Docker Direto

```bash
docker compose up -d --wait
```

Pronto! API em `http://localhost:8080`

**O que acontece automaticamente:**
1. Compilação Maven dentro do Docker
2. SQL Server iniciado e ready
3. Banco `portfoliodb` criado
4. Migrations Flyway executadas (V1-V12)
5. API iniciada com healthcheck
6. OFB Mock Server iniciado
7. Pronto para uso!

Para parar:
```bash
docker compose down
```

Para parar e apagar dados:
```bash
docker compose down -v
```

---

## Endpoints da API

### Autenticação
- `GET /oauth2/authorize` - Iniciar fluxo de autorização
- `POST /oauth2/token` - Obter access token
- `POST /oauth2/introspect` - Validar token
- `POST /oauth2/revoke` - Revogar token
- `POST /logout` - Logout (limpa sessão)

### Perfil de Risco
- `GET /perfil-risco/{clienteId}` - Calcular perfil de risco dinâmico
  - Retorna: perfil, pontuação, descrição
  - Requer: token JWT com acesso ao clienteId

### Investimentos
- `GET /investimentos/{clienteId}` - Histórico de investimentos via OFB
  - Busca dados de 5 categorias OFB
  - Cache: 5min (memória) + 24h (database)
  - Resiliência: Circuit Breaker + Retry

### Simulações
- `POST /simular-investimento` - Simular investimento
  - Body: `{ clienteId, valor, prazoMeses, tipoProduto }`
  - Validação de produto + cálculo de juros compostos
- `GET /simulacoes` - Listar todas as simulações
- `GET /simulacoes/por-produto-dia` - Agregação por produto/dia

### Recomendações
- `GET /produtos-recomendados/{perfil}` - Produtos recomendados por perfil
  - Perfil: CONSERVADOR, MODERADO, AGRESSIVO
  - Retorna produtos ordenados por rentabilidade

### Telemetria
- `GET /telemetria` - Métricas de uso do sistema
  - Volumes por serviço
  - Tempos médios de resposta
- `GET /actuator/health` - Health check
- `GET /actuator/prometheus` - Métricas Prometheus

**Documentação completa:** http://localhost:8080/api/v1/swagger-ui.html

---

## Tecnologias Utilizadas

### Backend
- Java 21 (LTS)
- Spring Boot 3.4.1
- Spring Security OAuth2 Authorization Server
- Spring Data JPA
- Resilience4j (Circuit Breaker, Retry)
- Caffeine Cache

### Banco de Dados
- SQL Server 2022 (produção)
- Flyway (migrações versionadas)

### Integração
- Quarkus 3.17.4 (OFB Mock Server)
- Jackson (JSON serialization)
- RestTemplate (HTTP client)

### Observabilidade
- Micrometer (métricas)
- Prometheus (coleta de métricas)
- Logback (logs estruturados)

### Desenvolvimento
- Docker & Docker Compose
- Maven 3.9+
- Swagger/OpenAPI 3
- JUnit 5 + Mockito (testes)

---

## Banco de Dados

### Schema Principal

```sql
-- Clientes
clients (
  cliente_id BIGINT PRIMARY KEY,
  cpf VARCHAR(11) UNIQUE,
  nome VARCHAR(255),
  email VARCHAR(255) UNIQUE,
  password VARCHAR(255),  -- BCrypt
  role VARCHAR(20),       -- CUSTOMER, ADMIN
  data_cadastro DATETIME,
  ativo BIT
)

-- Produtos de Investimento
products (
  id BIGINT PRIMARY KEY,
  nome VARCHAR(255),
  tipo VARCHAR(50),
  rentabilidade DECIMAL(5,4),
  risco VARCHAR(20),
  valor_minimo DECIMAL(15,2),
  prazo_minimo_meses INT,
  perfil_adequado VARCHAR(20),  -- Recomendação
  ativo BIT
)

-- Simulações
simulations (
  id BIGINT PRIMARY KEY,
  cliente_id BIGINT FOREIGN KEY,
  produto_id BIGINT FOREIGN KEY,
  produto_nome VARCHAR(255),
  valor_investido DECIMAL(15,2),
  valor_final DECIMAL(15,2),
  prazo_meses INT,
  data_simulacao DATETIME
)

-- Cache de Investimentos OFB
investment_data_cache (
  id BIGINT PRIMARY KEY,
  cpf VARCHAR(11),
  investment_data TEXT,  -- JSON serializado
  fetched_at DATETIME,
  expires_at DATETIME
)

-- Telemetria
telemetry (
  id BIGINT PRIMARY KEY,
  servico VARCHAR(100),
  tempo_resposta_ms BIGINT,
  timestamp DATETIME,
  status VARCHAR(20)
)
```

### Migrações Flyway

Executadas automaticamente na ordem:

1. `V1__create_products_table.sql` - Tabela de produtos
2. `V2__create_simulations_table.sql` - Tabela de simulações
3. `V3__create_investments_table.sql` - Tabela de investimentos (deprecated)
4. `V4__create_telemetry_table.sql` - Tabela de telemetria
5. `V5__seed_sample_products.sql` - Produtos de exemplo
6. `V6__create_clients_table.sql` - Tabela de clientes
7. `V7__drop_legacy_investments_table.sql` - Remove tabela antiga
8. `V8__add_client_identifier_mapping.sql` - Mapeamento clientId→CPF
9. `V9__create_investment_data_cache_table.sql` - Cache persistente
10. `V10__seed_sample_clients.sql` - Clientes de exemplo
11. `V11__add_role_column_and_admin_user.sql` - Roles + usuário ADMIN
12. `V12__add_password_to_customers.sql` - Senhas BCrypt

Arquivos em: `src/main/resources/db/migration/`

---

## Desenvolvimento Local

### Pré-requisitos

- Java 21 (LTS)
- Maven 3.9+
- Docker (para banco de dados)

### Instalando Java e Maven

**SDKMAN (Linux/macOS/Git Bash):**
```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env install
```

**Manual:** Baixe e instale Java 21 e Maven 3.9+.

### Rodando Localmente (sem Docker para API)

**Passo 1:** Suba banco de dados:
```bash
docker compose up sqlserver sqlserver-init flyway -d
```

**Passo 2:** Execute a aplicação:
```bash
sdk env  # Se usando SDKMAN
mvn spring-boot:run
```

API rodará em `http://localhost:8080`.

---

## Testes

### Executar Testes

```bash
# Todos os testes
mvn test

# Apenas unitários
mvn test -Dtest=**/*Test

# Apenas integração
mvn test -Dtest=**/*IT

# Com cobertura
mvn clean verify
```

### Cobertura

- **Testes Unitários:** Scorers, serviços, calculators
- **Testes de Integração:** Endpoints completos
- **Framework:** JUnit 5 + Mockito + Spring Boot Test

**Principais cenários testados:**
- Cálculo de perfil de risco (5 scorers)
- Simulação de investimentos (validação + cálculo)
- Autenticação OAuth2 (Authorization Code Flow)
- Integração OFB (mock)
- Resiliência (Circuit Breaker, Retry, Cache)

---

## Solução de Problemas

### A aplicação não sobe

```bash
# Ver logs
docker compose logs

# Log específico
docker compose logs api
docker compose logs ofb-mock-server
```

### Erro de conexão com banco

```bash
# Verificar saúde
docker compose ps

# Reiniciar tudo
docker compose down -v
docker compose up -d
```

### Migrações Flyway falharam

```bash
# Ver log do Flyway
docker compose logs flyway

# Resetar banco (APAGA TUDO)
docker compose down -v
docker compose up -d
```

### Porta 8080 ocupada

**Linux/macOS:**
```bash
lsof -i :8080
```

**Windows:**
```cmd
netstat -ano | findstr :8080
```

**Usar outra porta:**
```bash
SERVER_PORT=8081 mvn spring-boot:run
```

---

## Licença

Este projeto é para fins educacionais e de demonstração.
