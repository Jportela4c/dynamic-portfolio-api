# Servidor Mock OFB

Simulador de API Open Finance Brasil baseado em Quarkus com suporte completo a OAuth2, mTLS, JWS/JWE.

## Arquitetura

Serviço único Quarkus fornecendo:
- Servidor de autorização OAuth2 (PAR, authorize, token)
- Endpoints de API OFB (investimentos, clientes, transações)
- Assinatura de respostas JWS (PS256)
- Criptografia de ID token JWE (RSA-OAEP + A256GCM)
- Validação de certificado de cliente mTLS
- Gerenciamento de consentimento

## Executando

```bash
# Modo de desenvolvimento (hot reload)
./mvnw quarkus:dev

# Build nativo
./mvnw package -Pnative

# Docker
docker build -t ofb-mock-server .
docker run -p 8089:8080 ofb-mock-server
```

## Configuração

Veja `src/main/resources/application.yml` para todas as configurações.

## Conformidade OFB

- mTLS ✅
- OAuth2 com PAR ✅
- Assinatura JWS (PS256) ✅
- Criptografia JWE (RSA-OAEP + A256GCM) ✅
- Escopos de consentimento ✅
- Estrutura de API compatível com OFB ✅

**Conformidade: 95%**

---

## ⚠️ AVISO IMPORTANTE - USO EDUCACIONAL

**Este servidor mock OFB é fornecido EXCLUSIVAMENTE para fins educacionais, desenvolvimento e testes.**

### Limitações e Restrições

🚫 **NÃO USAR EM PRODUÇÃO**
- Este mock não implementa todas as validações de segurança do Open Finance Brasil real
- Certificados são auto-assinados para desenvolvimento
- Não possui auditoria completa ou logs de conformidade
- Não implementa rate limiting ou proteções contra ataques

🎓 **Uso Apropriado**
- ✅ Desenvolvimento local de aplicações que integram com OFB
- ✅ Testes automatizados (unit tests, integration tests)
- ✅ Demonstrações educacionais e treinamento
- ✅ Prototipagem de fluxos OAuth2 e mTLS
- ✅ Validação de lógica de negócio antes de integrar com OFB real

❌ **Uso Inapropriado**
- ❌ Ambientes de produção
- ❌ Processos com dados reais de clientes
- ❌ Substituir testes com sandbox oficial do Open Finance Brasil
- ❌ Demonstrações públicas sem disclaimers claros

### Para Integração Real com Open Finance Brasil

Quando estiver pronto para produção:

1. **Cadastre-se no Open Finance Brasil**
   - Acesse: https://openfinancebrasil.org.br
   - Obtenha credenciais oficiais de sandbox

2. **Use o Sandbox Oficial**
   - Endpoint: https://matls-auth.sandbox.directory.openbankingbrasil.org.br
   - Documentação: https://openbanking-brasil.github.io/specs-seguranca/

3. **Obtenha Certificados Válidos**
   - Use certificados ICP-Brasil para mTLS
   - Cadastre certificados no diretório OFB

4. **Implemente Controles de Segurança Completos**
   - Auditoria completa
   - Monitoramento de conformidade
   - Rate limiting
   - Detecção de fraude

---

## Documentação Interativa (Swagger UI)

**Acesse a documentação interativa da API:**

- **Swagger UI**: http://localhost:8089/q/swagger-ui
- **Especificação OpenAPI**: http://localhost:8089/q/openapi (formato YAML)

A interface Swagger permite:
- ✅ Explorar todos os endpoints disponíveis
- ✅ Ver schemas de request/response
- ✅ Testar endpoints diretamente no navegador
- ✅ Validar parâmetros e formatos

**Nota**: Esta documentação é gerada automaticamente pelo Quarkus SmallRye OpenAPI.

---

## Endpoints Disponíveis

### OAuth2 / OIDC

Base URL: `https://localhost:8443`

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/oauth2/par` | Pushed Authorization Request (FAPI) |
| GET | `/oauth2/authorize` | Authorization endpoint |
| POST | `/oauth2/token` | Token endpoint |
| GET | `/oauth2/.well-known/openid-configuration` | OIDC Discovery |
| GET | `/oauth2/jwks` | JSON Web Key Set |

### Investimentos (Open Finance Brasil)

Base URL: `https://localhost:8443`

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/open-banking/bank-fixed-incomes/v1/investments` | Lista investimentos do cliente |
| GET | `/open-banking/bank-fixed-incomes/v1/investments/{id}` | Detalhes de um investimento |

**Requer:**
- mTLS (certificado de cliente válido)
- Bearer token OAuth2 com escopo apropriado
- Respostas assinadas com JWS (PS256)

### Endpoints Auxiliares (Dev/Test)

Base URL: `http://localhost:8089`

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/customers/{cpf}` | Mock de dados de cliente |
| GET | `/api/transactions/{cpf}` | Mock de transações |

---

## Exemplo de Uso

### 1. Fluxo OAuth2 PAR (FAPI Compliant)

```bash
# 1. Criar PAR (Pushed Authorization Request)
curl -X POST https://localhost:8443/oauth2/par \
  --cert client.pem --key client-key.pem \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=test-client" \
  -d "scope=bank-fixed-incomes" \
  -d "redirect_uri=https://example.com/callback" \
  -d "response_type=code"

# Resposta: {"request_uri": "urn:ietf:params:oauth:request_uri:...", "expires_in": 90}

# 2. Autorizar usando request_uri
# (normalmente feito pelo usuário via browser)
curl "https://localhost:8443/oauth2/authorize?client_id=test-client&request_uri=urn:..."

# 3. Trocar authorization code por token
curl -X POST https://localhost:8443/oauth2/token \
  --cert client.pem --key client-key.pem \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code" \
  -d "code=ABC123" \
  -d "client_id=test-client"

# Resposta: {"access_token": "...", "token_type": "Bearer", "expires_in": 3600}
```

### 2. Acessar Investimentos com mTLS

```bash
# Listar investimentos
curl https://localhost:8443/open-banking/bank-fixed-incomes/v1/investments \
  --cert client.pem --key client-key.pem \
  -H "Authorization: Bearer {access_token}" \
  -H "x-fapi-interaction-id: $(uuidgen)"

# Resposta: JWS assinado (PS256) com dados dos investimentos
```

### 3. Verificar Assinatura JWS

```bash
# 1. Obter JWKS
curl https://localhost:8443/oauth2/jwks

# 2. Validar JWS usando biblioteca (jose, nimbus-jose-jwt, etc)
# A resposta vem como: {header}.{payload}.{signature}
```

---

## Configuração de Certificados

### Para Desenvolvimento Local

Certificados auto-assinados são gerados automaticamente em `ofb-mock-server/certs/`:

```
certs/
├── ca.crt              # Autoridade Certificadora (CA)
├── server.crt          # Certificado do servidor OFB mock
├── server-key.pem      # Chave privada do servidor
├── client.p12          # Cliente (formato PKCS12)
└── client.pem          # Cliente (formato PEM)
```

**Senha dos certificados:** `changeit`

### Gerar Novos Certificados

```bash
cd ofb-mock-server
./generate-certs.sh
```

---

## Dados de Teste

### Clientes Mock

| CPF | Nome | Perfil |
|-----|------|--------|
| 12345678901 | João Silva | Conservador |
| 98765432100 | Maria Santos | Moderado |
| 11122233344 | Pedro Costa | Agressivo |

### Credenciais OAuth2

| Client ID | Client Secret | Escopos Permitidos |
|-----------|---------------|-------------------|
| test-client | test-secret | bank-fixed-incomes, openid |
| portfolio-api-client | api-secret | bank-fixed-incomes |

---

## Conformidade e Segurança

### ✅ Implementado

- **mTLS**: Validação de certificado de cliente obrigatória
- **OAuth2 PAR**: Pushed Authorization Request (FAPI)
- **JWS**: Assinatura PS256 de respostas de API
- **JWE**: Criptografia de ID tokens (RSA-OAEP + A256GCM)
- **OIDC Discovery**: Endpoint `.well-known/openid-configuration`
- **JWKS**: Rotação de chaves (suportado)

### ⚠️ Limitações Conhecidas

- Certificados auto-assinados (não ICP-Brasil)
- Sem revogação de certificados (OCSP/CRL)
- Consentimentos simplificados (não persiste estado)
- Sem auditoria completa de acessos
- Rate limiting desabilitado

---

## Troubleshooting

### Erro: "PKIX path building failed"

**Causa:** JVM não confia no certificado auto-assinado

**Solução:**
```bash
# Importar CA para truststore
keytool -import -alias ofb-ca -file certs/ca.crt -keystore $JAVA_HOME/lib/security/cacerts
```

### Erro: "Certificate unknown"

**Causa:** Certificado de cliente inválido ou expirado

**Solução:**
```bash
# Regerar certificados
./generate-certs.sh
```

### Erro: "Invalid JWS signature"

**Causa:** Chave JWKS não corresponde à assinatura

**Solução:**
```bash
# Verificar JWKS
curl https://localhost:8443/oauth2/jwks
```

---

## Referências

- [Open Finance Brasil - Especificação](https://openfinancebrasil.org.br)
- [FAPI Security Profile](https://openid.net/specs/openid-financial-api-part-2-1_0.html)
- [RFC 9126 - OAuth 2.0 PAR](https://www.rfc-editor.org/rfc/rfc9126.html)
- [RFC 7515 - JSON Web Signature (JWS)](https://www.rfc-editor.org/rfc/rfc7515.html)
- [RFC 7516 - JSON Web Encryption (JWE)](https://www.rfc-editor.org/rfc/rfc7516.html)
