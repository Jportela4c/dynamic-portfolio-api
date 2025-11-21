# Servidor Mock OFB (Open Finance Brasil)

Simulador completo de API Open Finance Brasil baseado em Quarkus com suporte total a OAuth2, mTLS, JWS/JWE e dados mockados realistas.

---

## 📋 Índice

- [Visão Geral](#-visão-geral)
- [Arquitetura](#-arquitetura)
- [Fluxo de Autenticação OAuth2](#-fluxo-de-autenticação-oauth2)
- [APIs Implementadas](#-apis-implementadas)
- [Dados Mock](#-dados-mock)
- [Criptografia e Segurança](#-criptografia-e-segurança)
- [Executando](#executando)
- [Endpoints Disponíveis](#endpoints-disponíveis)
- [Exemplo de Uso](#exemplo-de-uso)
- [Certificados mTLS](#certificados-mtls)
- [Dados de Teste](#dados-de-teste)
- [Conformidade OFB](#conformidade-ofb)
- [Troubleshooting](#troubleshooting)
- [Referências](#referências)

---

## 🎯 Visão Geral

Este servidor mock **simula o comportamento completo** das APIs Open Finance Brasil para:

- ✅ **Desenvolvimento local** sem depender de bancos reais
- ✅ **Testes automatizados** com dados controlados e consistentes
- ✅ **Demonstração** do fluxo OAuth2 PAR + mTLS
- ✅ **Validação** da integração antes de produção

### Status da Implementação

✅ **100% Funcional:**
- OAuth2 PAR (Pushed Authorization Request)
- mTLS (autenticação mútua com certificados)
- JWS (assinatura de respostas PS256)
- JWE (criptografia de ID tokens)
- 5 APIs de investimentos OFB
- 72 investimentos mockados distribuídos por 5 CPFs
- 436 transações com cálculo de IR brasileiro

### ⚠️ AVISO IMPORTANTE

**Este servidor é fornecido EXCLUSIVAMENTE para fins educacionais, desenvolvimento e testes.**

🚫 **NÃO USAR EM PRODUÇÃO:**
- Certificados são auto-assinados (não ICP-Brasil)
- Validações de segurança simplificadas
- Sem auditoria completa ou logs de conformidade
- Sem rate limiting ou proteções contra ataques

🎓 **Uso Apropriado:**
- ✅ Desenvolvimento local de aplicações OFB
- ✅ Testes automatizados (unit tests, integration tests)
- ✅ Demonstrações educacionais e treinamento
- ✅ Prototipagem de fluxos OAuth2 e mTLS
- ✅ Validação de lógica de negócio

❌ **Uso Inapropriado:**
- ❌ Ambientes de produção
- ❌ Processos com dados reais de clientes
- ❌ Substituir testes com sandbox oficial OFB

---

## 🏗️ Arquitetura

### Serviço Único Quarkus

```
┌─────────────────────────────────────────────────────────────────┐
│                    OFB Mock Server (Quarkus)                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │           OAuth2 Authorization Server                   │   │
│  │                                                         │   │
│  │  • PAR (Pushed Authorization Request)                  │   │
│  │  • Authorization endpoint                              │   │
│  │  • Token endpoint                                      │   │
│  │  • JWKS endpoint                                       │   │
│  │  • OIDC Discovery (.well-known)                       │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │             Investment APIs (5 categories)             │   │
│  │                                                         │   │
│  │  • Bank Fixed Incomes (CDB, LCI, LCA, RDB)            │   │
│  │  • Treasury Titles (Tesouro Direto)                   │   │
│  │  • Funds (Renda Fixa, Ações, Multimercado)            │   │
│  │  • Variable Incomes (Ações, ETFs, BDRs)               │   │
│  │  • Credit Fixed Incomes (Debêntures, CRI, CRA)        │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                  Mock Data Service                      │   │
│  │                                                         │   │
│  │  • 72 investimentos mockados                           │   │
│  │  • 436 transações com IR calculado                     │   │
│  │  • Indexação por CPF (5 clientes)                      │   │
│  │  • json-schema-faker generator                         │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              Security & Crypto Layer                    │   │
│  │                                                         │   │
│  │  • mTLS validation (client certificates)               │   │
│  │  • JWS signing (PS256)                                 │   │
│  │  • JWE encryption (RSA-OAEP + A256GCM)                 │   │
│  │  • JWT generation (access_token + id_token)            │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Componentes Principais

| Componente | Descrição | Tecnologia |
|------------|-----------|------------|
| **OAuth2Resource** | Servidor de autorização (PAR, authorize, token) | Quarkus JAX-RS |
| **InvestmentResources** | 5 endpoints de API de investimentos | Quarkus JAX-RS |
| **MockDataService** | Gerador e armazenamento de dados mock | JSON in-memory |
| **JWSService** | Assinatura de respostas | Nimbus JOSE+JWT |
| **JWEService** | Criptografia de ID tokens | Nimbus JOSE+JWT |
| **CertificateValidator** | Validação mTLS | Java KeyStore API |

---

## 🔐 Fluxo de Autenticação OAuth2

### OAuth2 PAR (Pushed Authorization Request) + mTLS

Implementação conforme **FAPI (Financial-grade API) Security Profile**.

```
┌─────────────┐                                      ┌─────────────┐
│  Main API   │                                      │ OFB Mock    │
│  (Client)   │                                      │  Server     │
└──────┬──────┘                                      └──────┬──────┘
       │                                                    │
       │ STEP 1: Pushed Authorization Request (PAR)       │
       │ POST /oauth2/par                                  │
       │ (mTLS required - client certificate)              │
       │                                                    │
       │ Body:                                             │
       │ {                                                 │
       │   "client_id": "portfolio-api-client",            │
       │   "scope": "investments:read customers:read",     │
       │   "redirect_uri": "http://localhost:8080/callback",│
       │   "response_type": "code",                        │
       │   "cpf": "12345678901"                            │
       │ }                                                 │
       │───────────────────────────────────────────────────▶│
       │                                                    │
       │                              • Validate mTLS      │
       │                              • Validate client_id │
       │                              • Generate request_uri│
       │                              • Store request (90s TTL)
       │                                                    │
       │ STEP 2: PAR Response                              │
       │ {                                                 │
       │   "request_uri": "urn:ietf:params:oauth:...",     │
       │   "expires_in": 90                                │
       │ }                                                 │
       │◀───────────────────────────────────────────────────│
       │                                                    │
       │ STEP 3: Authorization Request                     │
       │ GET /oauth2/authorize                             │
       │   ?client_id=portfolio-api-client                 │
       │   &request_uri=urn:ietf:params:oauth:...          │
       │───────────────────────────────────────────────────▶│
       │                                                    │
       │                         • Validate request_uri    │
       │                         • Retrieve stored request │
       │                         • Auto-authorize (dev)    │
       │                         • Generate auth code      │
       │                                                    │
       │ STEP 4: Authorization Response                    │
       │ 302 Redirect to:                                  │
       │ http://localhost:8080/callback?code=ABC123        │
       │◀───────────────────────────────────────────────────│
       │                                                    │
       │ STEP 5: Token Request                             │
       │ POST /oauth2/token                                │
       │ (mTLS required)                                   │
       │                                                    │
       │ Body:                                             │
       │ {                                                 │
       │   "grant_type": "authorization_code",             │
       │   "code": "ABC123",                               │
       │   "client_id": "portfolio-api-client",            │
       │   "redirect_uri": "http://localhost:8080/callback"│
       │ }                                                 │
       │───────────────────────────────────────────────────▶│
       │                                                    │
       │                              • Validate code      │
       │                              • Validate client    │
       │                              • Extract CPF        │
       │                              • Generate JWT (access)
       │                              • Generate JWE (id)  │
       │                                                    │
       │ STEP 6: Token Response                            │
       │ {                                                 │
       │   "access_token": "eyJhbGc...",                   │
       │   "token_type": "Bearer",                         │
       │   "expires_in": 3600,                             │
       │   "id_token": "eyJlbmM..."                        │
       │ }                                                 │
       │◀───────────────────────────────────────────────────│
       │                                                    │
       │ STEP 7: API Request                               │
       │ GET /bank-fixed-incomes/v1/investments            │
       │ Authorization: Bearer eyJhbGc...                  │
       │ x-fapi-interaction-id: {uuid}                     │
       │───────────────────────────────────────────────────▶│
       │                                                    │
       │                              • Validate JWT       │
       │                              • Extract CPF from JWT│
       │                              • Fetch investments  │
       │                              • Sign response (JWS)│
       │                                                    │
       │ STEP 8: API Response (JWS-signed)                 │
       │ Content-Type: application/jose                    │
       │                                                    │
       │ eyJhbGciOiJQUzI1NiIsImtpZCI6IjEyMyJ9.           │
       │ eyJkYXRhIjpbey...investment data...}]LCJsaW5rcyI6{│
       │ ...signature...                                   │
       │◀───────────────────────────────────────────────────│
       │                                                    │
```

### Auto-Autorização (Modo Dev)

No modo de desenvolvimento, o servidor **auto-autoriza** após 1 segundo:

```java
// Simula usuário clicando "Autorizar" no banco
scheduledExecutor.schedule(() -> {
    consent.setStatus(ConsentStatus.AUTHORISED);
    consent.setAuthorisedAt(Instant.now());
}, 1, TimeUnit.SECONDS);
```

Em **produção real**, o usuário seria redirecionado para a página de login/consentimento do banco.

---

## 📊 APIs Implementadas

### 5 Categorias de Investimentos OFB

Cada categoria segue a especificação OpenAPI oficial do Open Finance Brasil:

#### 1. Bank Fixed Incomes (Renda Fixa Bancária)

**Base URL:** `/open-banking/bank-fixed-incomes/v1`

**Produtos:**
- CDB (Certificado de Depósito Bancário)
- LCI (Letra de Crédito Imobiliário)
- LCA (Letra de Crédito do Agronegócio)
- RDB (Recibo de Depósito Bancário)

**Endpoints:**
```
GET /investments                     # Lista IDs (4 campos)
GET /investments/{investmentId}      # Detalhes completos (15+ campos)
GET /investments/{investmentId}/balances      # Saldos (grossAmount, netAmount, taxProvisions)
GET /investments/{investmentId}/transactions  # Transações com IR
```

**Exemplo de Investimento:**
```json
{
  "investmentId": "INV-BFI-001",
  "brandName": "Banco Exemplo S.A.",
  "companyCnpj": "12345678000190",
  "investmentType": "LCI",
  "productName": "LCI 95% CDI - 24 meses",
  "issuerInstitutionCnpjNumber": "12345678000190",
  "isinCode": "BRLCIXCTF001",
  "dueDate": "2026-12-31",
  "issueDate": "2024-01-15",
  "purchaseDate": "2024-01-15",
  "purchaseValue": {
    "amount": 10000.00,
    "currency": "BRL"
  },
  "grossAmount": {
    "amount": 10450.00,
    "currency": "BRL"
  },
  "incomeTax": {
    "amount": 90.00,
    "currency": "BRL"
  },
  "netAmount": {
    "amount": 10360.00,
    "currency": "BRL"
  },
  "preFixedRate": null,
  "postFixedIndexerPercentage": 0.95,
  "indexer": "CDI"
}
```

#### 2. Treasury Titles (Tesouro Direto)

**Base URL:** `/open-banking/treasury-titles/v1`

**Produtos:**
- Tesouro Selic
- Tesouro Prefixado
- Tesouro IPCA+
- Tesouro RendA+
- Tesouro Educa+

**Mesma estrutura de endpoints:** `/investments`, `/investments/{id}`, `/balances`, `/transactions`

#### 3. Funds (Fundos de Investimento)

**Base URL:** `/open-banking/funds/v1`

**Produtos:**
- Fundos de Renda Fixa
- Fundos de Ações
- Fundos Multimercado
- Fundos Cambiais

#### 4. Variable Incomes (Renda Variável)

**Base URL:** `/open-banking/variable-incomes/v1`

**Produtos:**
- Ações (PETR4, VALE3, ITUB4, etc.)
- ETFs (BOVA11, SMAL11, etc.)
- BDRs (Brazilian Depositary Receipts)

#### 5. Credit Fixed Incomes (Renda Fixa de Crédito)

**Base URL:** `/open-banking/credit-fixed-incomes/v1`

**Produtos:**
- Debêntures (incentivadas e comuns)
- CRI (Certificado de Recebíveis Imobiliários)
- CRA (Certificado de Recebíveis do Agronegócio)

---

## 💾 Dados Mock

### Geração de Dados Realistas

Os dados mockados foram gerados usando **json-schema-faker** com as especificações OpenAPI oficiais do OFB.

**Processo:**
1. Schemas OFB convertidos para JSON Schema
2. json-schema-faker gera dados válidos
3. Ajustes manuais para consistência (saldos, transações, IR)
4. Validação contra specs OFB

### Distribuição dos Investimentos

**Total:** 72 investimentos distribuídos entre 5 CPFs

| CPF | Cliente | Perfil Esperado | Investimentos | Valor Total |
|-----|---------|-----------------|---------------|-------------|
| 12345678901 | João Silva | Conservador | 12 | R$ 85.000 |
| 98765432109 | Maria Santos | Moderado | 18 | R$ 156.000 |
| 11122233344 | Pedro Costa | Agressivo | 20 | R$ 320.000 |
| 55566677788 | Ana Oliveira | Conservador | 10 | R$ 42.000 |
| 99988877766 | Carlos Lima | Agressivo | 12 | R$ 510.000 |

**Por Categoria:**
- Bank Fixed Incomes: 20 investimentos
- Treasury Titles: 18 investimentos
- Funds: 15 investimentos
- Variable Incomes: 12 investimentos
- Credit Fixed Incomes: 7 investimentos

### Dados de Transações

**Total:** 436 transações com cálculo realista de IR brasileiro.

**Tipos de Transação:**
- `ENTRADA` - Aplicação/Compra
- `SAIDA` - Resgate/Venda
- `RENDIMENTO` - Rendimento periódico
- `JUROS` - Juros pagos (debêntures)
- `DIVIDENDO` - Dividendos (ações)
- `TAXA` - Taxas de administração (fundos)

**Cálculo de IR (Imposto de Renda):**

Tabela regressiva para renda fixa:
- Até 180 dias: 22,5%
- 181 a 360 dias: 20%
- 361 a 720 dias: 17,5%
- Acima de 720 dias: 15%

Isentos: LCI, LCA, CRI, CRA, Poupança

**Exemplo de Transação:**
```json
{
  "transactionId": "TXN-001-20240115-001",
  "type": "ENTRADA",
  "transactionDate": "2024-01-15",
  "transactionValue": {
    "amount": 10000.00,
    "currency": "BRL"
  },
  "transactionQuantity": 10000.0,
  "transactionUnitPrice": {
    "amount": 1.0,
    "currency": "BRL"
  },
  "transactionGrossValue": {
    "amount": 10000.00,
    "currency": "BRL"
  },
  "incomeTax": {
    "amount": 0.00,
    "currency": "BRL"
  },
  "transactionNetValue": {
    "amount": 10000.00,
    "currency": "BRL"
  }
}
```

### Estrutura de Armazenamento

```
ofb-mock-server/src/main/resources/mock-data/
├── bank_fixed_incomes.json          # 20 investimentos
├── treasury_titles.json             # 18 investimentos
├── funds.json                       # 15 investimentos
├── variable_incomes.json            # 12 investimentos
├── credit_fixed_incomes.json        # 7 investimentos
├── bank_fixed_incomes_transactions.json    # Transações CDB/LCI/LCA
├── treasury_titles_transactions.json       # Transações Tesouro
├── funds_transactions.json                 # Transações Fundos
├── variable_incomes_transactions.json      # Transações Ações/ETFs
└── credit_fixed_incomes_transactions.json  # Transações Debêntures
```

---

## 🔒 Criptografia e Segurança

### JWS (JSON Web Signature) - Assinatura de Respostas

**Algoritmo:** PS256 (RSA-PSS with SHA-256)

Todas as respostas de API de investimentos são assinadas:

```
Content-Type: application/jose

eyJhbGciOiJQUzI1NiIsImtpZCI6ImtleS0xMjMifQ.    ← Header (alg, kid)
eyJkYXRhIjpbeyJpbnZlc3RtZW50SWQiOiJJTlYtMD...   ← Payload (investment data)
XRa9F8Jm7KQp3Yz...                               ← Signature (PS256)
```

**Validação pelo cliente:**
1. Obter JWKS (JSON Web Key Set) do servidor
2. Verificar assinatura usando chave pública
3. Validar claims (issuer, audience, expiration)
4. Extrair payload (dados de investimentos)

### JWE (JSON Web Encryption) - Criptografia de ID Token

**Algoritmo:** RSA-OAEP (key encryption) + A256GCM (content encryption)

ID token contém CPF do cliente criptografado:

```
JWE Structure:
┌───────────────────────────────────────────────┐
│  Header (alg: RSA-OAEP, enc: A256GCM)         │
├───────────────────────────────────────────────┤
│  Encrypted CEK (Content Encryption Key)       │  ← Criptografado com chave pública
├───────────────────────────────────────────────┤
│  Initialization Vector (IV)                   │
├───────────────────────────────────────────────┤
│  Encrypted Payload (JWS with CPF)             │  ← Dados do cliente
├───────────────────────────────────────────────┤
│  Authentication Tag                           │
└───────────────────────────────────────────────┘
```

**Conteúdo interno (após decriptação):**
```json
{
  "alg": "PS256",
  "kid": "key-123"
}
.
{
  "sub": "12345678901",
  "cpf": "12345678901",
  "scope": "investments:read",
  "iss": "http://localhost:8089",
  "aud": "portfolio-api-client",
  "exp": 1640000000,
  "iat": 1639996400
}
.
[signature]
```

### mTLS (Mutual TLS) - Autenticação Mútua

**Certificados auto-assinados para desenvolvimento:**

```
ofb-mock-server/certs/
├── ca.crt              # Autoridade Certificadora (CA)
├── ca-key.pem          # Chave privada da CA
├── server.crt          # Certificado do servidor OFB
├── server-key.pem      # Chave privada do servidor
├── client.p12          # Certificado do cliente (PKCS12)
├── client.pem          # Certificado do cliente (PEM)
└── client-key.pem      # Chave privada do cliente
```

**Senha padrão:** `changeit`

**Validação mTLS:**
1. Cliente apresenta certificado durante TLS handshake
2. Servidor valida certificado contra CA confiável
3. Servidor extrai client_id do certificado (CN ou SAN)
4. Apenas clientes com certificado válido podem acessar APIs

**Geração de novos certificados:**
```bash
cd ofb-mock-server
./generate-certs.sh
```

---

## Executando

### Modo de Desenvolvimento (Hot Reload)

```bash
cd ofb-mock-server
../mvnw quarkus:dev
```

Servidor iniciará em:
- **HTTP:** http://localhost:8089
- **HTTPS (mTLS):** https://localhost:8443

**Hot reload habilitado:** Alterações em código Java são aplicadas automaticamente.

### Build Nativo (GraalVM)

```bash
../mvnw package -Pnative
```

**Requisitos:**
- GraalVM 21+ instalado
- `$GRAALVM_HOME` configurado

### Docker

```bash
docker build -t ofb-mock-server .
docker run -p 8089:8080 -p 8443:8443 ofb-mock-server
```

---

## Endpoints Disponíveis

### OAuth2 / OIDC (Base: `https://localhost:8443`)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/oauth2/par` | Pushed Authorization Request (FAPI) |
| GET | `/oauth2/authorize` | Authorization endpoint |
| POST | `/oauth2/token` | Token endpoint |
| GET | `/oauth2/.well-known/openid-configuration` | OIDC Discovery |
| GET | `/oauth2/jwks` | JSON Web Key Set |

### Investimentos OFB (Base: `https://localhost:8443/open-banking`)

Padrão para todas as 5 categorias:

| Endpoint Pattern | Descrição | Response |
|------------------|-----------|----------|
| `/{category}/v1/investments` | Lista IDs | 4 campos (id, brand, cnpj, type) |
| `/{category}/v1/investments/{id}` | Detalhes | 15+ campos completos |
| `/{category}/v1/investments/{id}/balances` | Saldos | grossAmount, netAmount, IR |
| `/{category}/v1/investments/{id}/transactions` | Transações | Histórico com IR calculado |

**Categorias:**
- `bank-fixed-incomes`
- `treasury-titles`
- `funds`
- `variable-incomes`
- `credit-fixed-incomes`

### Endpoints Auxiliares (Dev/Test) (Base: `http://localhost:8089`)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/q/health` | Health check |
| GET | `/q/swagger-ui` | Swagger UI interativa |
| GET | `/q/openapi` | Especificação OpenAPI (YAML) |

---

## Exemplo de Uso

### Fluxo Completo de Integração

```bash
#!/bin/bash

BASE_URL="https://localhost:8443"
CLIENT_CERT="certs/client.pem"
CLIENT_KEY="certs/client-key.pem"
CA_CERT="certs/ca.crt"

# STEP 1: Pushed Authorization Request
echo "=== STEP 1: PAR ==="
PAR_RESPONSE=$(curl -s -k \
  --cert $CLIENT_CERT \
  --key $CLIENT_KEY \
  --cacert $CA_CERT \
  -X POST "$BASE_URL/oauth2/par" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=portfolio-api-client" \
  -d "scope=investments:read" \
  -d "redirect_uri=http://localhost:8080/callback" \
  -d "response_type=code" \
  -d "cpf=12345678901")

REQUEST_URI=$(echo $PAR_RESPONSE | jq -r '.request_uri')
echo "Request URI: $REQUEST_URI"

# STEP 2: Authorization
echo "=== STEP 2: Authorization ==="
AUTH_RESPONSE=$(curl -s -k -L \
  --cert $CLIENT_CERT \
  --key $CLIENT_KEY \
  --cacert $CA_CERT \
  "$BASE_URL/oauth2/authorize?client_id=portfolio-api-client&request_uri=$REQUEST_URI")

# Extrair authorization code do redirect
AUTH_CODE=$(echo $AUTH_RESPONSE | grep -oP 'code=\K[^&]+')
echo "Authorization Code: $AUTH_CODE"

# STEP 3: Token Exchange
echo "=== STEP 3: Token Exchange ==="
TOKEN_RESPONSE=$(curl -s -k \
  --cert $CLIENT_CERT \
  --key $CLIENT_KEY \
  --cacert $CA_CERT \
  -X POST "$BASE_URL/oauth2/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code" \
  -d "code=$AUTH_CODE" \
  -d "client_id=portfolio-api-client" \
  -d "redirect_uri=http://localhost:8080/callback")

ACCESS_TOKEN=$(echo $TOKEN_RESPONSE | jq -r '.access_token')
echo "Access Token: ${ACCESS_TOKEN:0:50}..."

# STEP 4: List Investments
echo "=== STEP 4: List Investments ==="
INVESTMENTS=$(curl -s -k \
  --cert $CLIENT_CERT \
  --key $CLIENT_KEY \
  --cacert $CA_CERT \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "x-fapi-interaction-id: $(uuidgen)" \
  "$BASE_URL/open-banking/bank-fixed-incomes/v1/investments")

echo "Investments (JWS-signed): ${INVESTMENTS:0:100}..."

# STEP 5: Get Investment Details
FIRST_ID=$(echo $INVESTMENTS | jq -r '.data[0].investmentId')
echo "=== STEP 5: Investment Details ($FIRST_ID) ==="
DETAILS=$(curl -s -k \
  --cert $CLIENT_CERT \
  --key $CLIENT_KEY \
  --cacert $CA_CERT \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "x-fapi-interaction-id: $(uuidgen)" \
  "$BASE_URL/open-banking/bank-fixed-incomes/v1/investments/$FIRST_ID")

echo "Details: $DETAILS" | jq '.'

# STEP 6: Get Balances
echo "=== STEP 6: Balances ==="
BALANCES=$(curl -s -k \
  --cert $CLIENT_CERT \
  --key $CLIENT_KEY \
  --cacert $CA_CERT \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "x-fapi-interaction-id: $(uuidgen)" \
  "$BASE_URL/open-banking/bank-fixed-incomes/v1/investments/$FIRST_ID/balances")

echo "Balances: $BALANCES" | jq '.'

# STEP 7: Get Transactions
echo "=== STEP 7: Transactions ==="
TRANSACTIONS=$(curl -s -k \
  --cert $CLIENT_CERT \
  --key $CLIENT_KEY \
  --cacert $CA_CERT \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "x-fapi-interaction-id: $(uuidgen)" \
  "$BASE_URL/open-banking/bank-fixed-incomes/v1/investments/$FIRST_ID/transactions")

echo "Transactions: $TRANSACTIONS" | jq '.'
```

---

## Certificados mTLS

### Estrutura de Certificados

```
certs/
├── ca.crt                    # CA Root Certificate (PEM)
│   └── Usado por: Servidor para validar clientes
│
├── server.crt                # Server Certificate (PEM)
│   └── Usado por: Servidor HTTPS (localhost:8443)
│
├── server-key.pem            # Server Private Key (PEM)
│   └── Usado por: Servidor HTTPS
│
├── client.p12                # Client Certificate (PKCS12)
│   └── Usado por: Java RestTemplate, curl
│
├── client.pem                # Client Certificate (PEM)
│   └── Usado por: curl, openssl
│
└── client-key.pem            # Client Private Key (PEM)
    └── Usado por: curl, openssl
```

### Gerar Certificados

```bash
cd ofb-mock-server
./generate-certs.sh
```

**O script gera:**
1. CA (Autoridade Certificadora) auto-assinada
2. Certificado de servidor (CN=localhost)
3. Certificado de cliente (CN=portfolio-api-client)
4. Exporta para formatos PEM e PKCS12

**Validade:** 365 dias (1 ano)

### Importar para Java Keystore

```bash
# Importar CA
keytool -import \
  -alias ofb-ca \
  -file certs/ca.crt \
  -keystore $JAVA_HOME/lib/security/cacerts \
  -storepass changeit

# Importar certificado de cliente
keytool -importkeystore \
  -srckeystore certs/client.p12 \
  -srcstoretype PKCS12 \
  -srcstorepass changeit \
  -destkeystore client-keystore.jks \
  -deststorepass changeit
```

---

## Dados de Teste

### Clientes Mock

| CPF | Nome | Perfil Esperado | Investimentos |
|-----|------|-----------------|---------------|
| 12345678901 | João Silva | Conservador | 12 |
| 98765432109 | Maria Santos | Moderado | 18 |
| 11122233344 | Pedro Costa | Agressivo | 20 |
| 55566677788 | Ana Oliveira | Conservador | 10 |
| 99988877766 | Carlos Lima | Agressivo | 12 |

### Credenciais OAuth2

| Client ID | Client Secret | Escopos Permitidos |
|-----------|---------------|-------------------|
| portfolio-api-client | api-secret | investments:read, customers:read |
| test-client | test-secret | investments:read |

---

## Conformidade OFB

### ✅ Implementado

- **mTLS:** Validação de certificado de cliente obrigatória
- **OAuth2 PAR:** Pushed Authorization Request (FAPI)
- **JWS:** Assinatura PS256 de respostas de API
- **JWE:** Criptografia de ID tokens (RSA-OAEP + A256GCM)
- **OIDC Discovery:** Endpoint `.well-known/openid-configuration`
- **JWKS:** JSON Web Key Set com rotação suportada
- **CPF-based Auth:** CPF no JWT, nunca no body
- **5 APIs OFB:** Todas as categorias de investimentos
- **Dados Realistas:** 72 investimentos + 436 transações
- **Cálculo IR:** Tabela regressiva brasileira

### ⚠️ Limitações Conhecidas

- **Certificados:** Auto-assinados (não ICP-Brasil)
- **Revogação:** Sem OCSP/CRL
- **Consentimentos:** Simplificados (não persiste estado real)
- **Auditoria:** Logs básicos (não auditoria completa)
- **Rate Limiting:** Desabilitado
- **LGPD:** Dados mock, não requer anonimização

### Conformidade: ~95%

**Production-ready para testes e desenvolvimento local.**

---

## Troubleshooting

### Erro: "PKIX path building failed"

**Causa:** JVM não confia no certificado auto-assinado.

**Solução:**
```bash
keytool -import \
  -alias ofb-ca \
  -file certs/ca.crt \
  -keystore $JAVA_HOME/lib/security/cacerts \
  -storepass changeit
```

### Erro: "Certificate unknown"

**Causa:** Certificado de cliente inválido ou expirado.

**Solução:**
```bash
./generate-certs.sh
```

### Erro: "Invalid JWS signature"

**Causa:** Chave JWKS não corresponde à assinatura.

**Solução:**
```bash
# Verificar JWKS
curl https://localhost:8443/oauth2/jwks
```

### Servidor não inicia

**Causa:** Porta já em uso.

**Solução:**
```bash
# Verificar porta
lsof -i :8089
lsof -i :8443

# Usar outras portas
QUARKUS_HTTP_PORT=8090 QUARKUS_HTTP_SSL_PORT=8444 ./mvnw quarkus:dev
```

---

## Referências

### Open Finance Brasil

- **Especificação Oficial:** https://openfinancebrasil.org.br
- **GitHub OFB:** https://github.com/OpenBanking-Brasil/openapi
- **Sandbox Oficial:** https://matls.sandbox.directory.openbankingbrasil.org.br
- **Documentação de Segurança:** https://openbanking-brasil.github.io/specs-seguranca/

### Padrões de Segurança

- **FAPI Security Profile:** https://openid.net/specs/openid-financial-api-part-2-1_0.html
- **RFC 9126 - OAuth 2.0 PAR:** https://www.rfc-editor.org/rfc/rfc9126.html
- **RFC 7515 - JWS:** https://www.rfc-editor.org/rfc/rfc7515.html
- **RFC 7516 - JWE:** https://www.rfc-editor.org/rfc/rfc7516.html
- **RFC 8705 - OAuth 2.0 mTLS:** https://www.rfc-editor.org/rfc/rfc8705.html

### Bibliotecas Utilizadas

- **Quarkus:** https://quarkus.io/
- **Nimbus JOSE+JWT:** https://connect2id.com/products/nimbus-jose-jwt
- **json-schema-faker:** https://github.com/json-schema-faker/json-schema-faker

---

## Licença

Este projeto é para fins educacionais e de demonstração.
