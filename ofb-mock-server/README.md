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
