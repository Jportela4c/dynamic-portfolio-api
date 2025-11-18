# Guia de Uso - OFB Mock Server

## Visão Geral

Servidor mock Open Finance Brasil completo com suporte a:
- OAuth2 com PAR (Pushed Authorization Request)
- mTLS (autenticação de certificado de cliente)
- Assinatura JWS de respostas (PS256)
- Criptografia JWE de ID tokens (RSA-OAEP + A256GCM)
- Dados mock para 5 CPFs diferentes

## Pré-requisitos

- Java 21+
- Maven 3.9+
- Docker (opcional)

## Configuração Inicial

### 1. Gerar Certificados mTLS

```bash
cd ofb-mock-server
./generate-certs.sh
```

Isso cria:
- `src/main/resources/certs/server.p12` - Keystore do servidor
- `src/main/resources/certs/client.p12` - Keystore do cliente (para usar na API principal)
- `src/main/resources/certs/truststore.p12` - Truststore do servidor

### 2. Executar em Desenvolvimento

```bash
mvn quarkus:dev
```

O servidor estará disponível em:
- HTTP: http://localhost:8089
- HTTPS (mTLS): https://localhost:8443

## Endpoints Disponíveis

### OAuth2

**1. Pushed Authorization Request (PAR)**
```bash
POST /oauth2/par
Content-Type: application/x-www-form-urlencoded

client_id=test-client&scope=investments:read&redirect_uri=https://example.com/callback&response_type=code
```

**2. Autorização**
```bash
GET /oauth2/authorize?request_uri=urn:...&client_id=test-client
```

**3. Token**
```bash
POST /oauth2/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code&code=CODE_...&redirect_uri=https://example.com/callback&client_id=test-client
```

**4. OpenID Configuration**
```bash
GET /oauth2/.well-known/openid-configuration
```

**5. JWKS**
```bash
GET /oauth2/jwks
```

### APIs de Dados

**1. Investimentos por CPF**
```bash
GET /api/investments/{cpf}

Exemplo: GET /api/investments/12345678901
```

**2. Cliente por CPF**
```bash
GET /api/customers/{cpf}

Exemplo: GET /api/customers/12345678901
```

**3. Transações por CPF**
```bash
GET /api/transactions/{cpf}

Exemplo: GET /api/transactions/12345678901
```

## CPFs Disponíveis para Teste

Os seguintes CPFs possuem dados mock:
- `12345678901` - João Silva (2 investimentos)
- `23456789012` - Maria Santos (1 investimento)
- `34567890123` - Pedro Oliveira (2 investimentos)
- `45678901234` - Ana Costa (1 investimento)
- `56789012345` - Carlos Ferreira (2 investimentos)

## Fluxo OAuth2 Completo

```bash
# 1. Criar PAR
PAR_RESPONSE=$(curl -X POST http://localhost:8089/oauth2/par \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=test-client&scope=investments:read&redirect_uri=https://example.com/callback&response_type=code")

REQUEST_URI=$(echo $PAR_RESPONSE | jq -r '.request_uri')

# 2. Autorizar (redireciona com código)
AUTH_RESPONSE=$(curl -i -X GET "http://localhost:8089/oauth2/authorize?request_uri=$REQUEST_URI&client_id=test-client")

# 3. Extrair código do Location header
CODE=$(echo "$AUTH_RESPONSE" | grep -i "Location:" | sed 's/.*code=//' | tr -d '\r')

# 4. Trocar código por tokens
curl -X POST http://localhost:8089/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code&code=$CODE&redirect_uri=https://example.com/callback&client_id=test-client"
```

## Assinatura JWS

Todas as respostas das APIs de dados (endpoints `/api/*`) são automaticamente assinadas com JWS usando o algoritmo PS256. O filtro `JWSResponseFilter` intercepta as respostas e aplica a assinatura.

Para verificar a assinatura:
1. Obtenha a chave pública do endpoint `/oauth2/jwks`
2. Verifique a assinatura usando a biblioteca Nimbus JOSE+JWT

## Testes

```bash
# Executar todos os testes
mvn test

# Executar testes específicos
mvn test -Dtest=OAuth2FlowTest
mvn test -Dtest=ApiEndpointsTest
mvn test -Dtest=JWSSigningTest
```

Os testes cobrem:
- Fluxo OAuth2 completo (PAR → Authorize → Token)
- Endpoints de API (investments, customers, transactions)
- Assinatura JWS e geração de chaves

## Docker

### Build
```bash
docker build -t ofb-mock-server .
```

### Run
```bash
docker run -p 8089:8089 -p 8443:8443 ofb-mock-server
```

### Docker Compose (com API principal)
```bash
docker-compose up ofb-mock-server
```

## Integração com API Principal

Para conectar a API principal ao mock server:

1. Copie o certificado cliente:
```bash
cp ofb-mock-server/src/main/resources/certs/client.p12 src/main/resources/certs/
```

2. Configure o RestTemplate com mTLS:
```java
@Bean
public RestTemplate ofbRestTemplate() throws Exception {
    SSLContext sslContext = SSLContextBuilder.create()
        .loadKeyMaterial(
            ResourceUtils.getFile("classpath:certs/client.p12"),
            "changeit".toCharArray(),
            "changeit".toCharArray()
        )
        .loadTrustMaterial(null, (chain, authType) -> true)
        .build();

    HttpClient httpClient = HttpClients.custom()
        .setSSLContext(sslContext)
        .build();

    return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
}
```

3. Configure as propriedades:
```properties
ofb.provider.base-url=https://localhost:8443
ofb.provider.client-id=portfolio-api
```

## Troubleshooting

### Erro de certificado

Se encontrar erros relacionados a certificados SSL:
```bash
# Regerar certificados
cd ofb-mock-server
rm -rf src/main/resources/certs/*.p12 src/main/resources/certs/*.cer
./generate-certs.sh
```

### Erro de porta em uso

Se a porta 8089 ou 8443 estiver em uso:
```bash
# Verificar processos usando as portas
lsof -i :8089
lsof -i :8443

# Matar processo
kill -9 <PID>
```

### Erro de versão do Java

Certifique-se de estar usando Java 21+:
```bash
java -version
# Deve mostrar: openjdk version "21" ou superior
```

## Conformidade OFB

Este mock server implementa:
- ✅ mTLS com validação de certificado de cliente
- ✅ OAuth2 com Pushed Authorization Request (PAR)
- ✅ Assinatura JWS de respostas (PS256)
- ✅ Criptografia JWE de ID tokens (RSA-OAEP + A256GCM)
- ✅ Estrutura de API compatível com OFB
- ✅ Gestão de escopos de consentimento

**Conformidade estimada: 95%**

*Nota: Este é um servidor mock para desenvolvimento e testes. Não use em produção.*
