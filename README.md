# API de Portfólio Dinâmico

Sistema de simulação de investimentos para produtos financeiros brasileiros incluindo CDBs, LCIs, LCAs, Tesouro Direto e fundos de investimento.

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

## 🏦 Servidor Mock OFB

Este projeto inclui um **servidor mock completo de Open Finance Brasil** para desenvolvimento e testes.

**Documentação**: [`ofb-mock-server/README.md`](ofb-mock-server/README.md)

**Swagger UI**: http://localhost:8089/q/swagger-ui

O mock implementa:
- ✅ OAuth2 PAR (Pushed Authorization Request)
- ✅ mTLS (autenticação mútua)
- ✅ JWS (assinatura de respostas PS256)
- ✅ JWE (criptografia de ID tokens)
- ✅ APIs de investimentos OFB

⚠️ **Uso educacional apenas** - não usar em produção.

---

## Índice

- [Início Rápido](#-início-rápido-um-único-comando)
- [Como Testar a API](#como-testar-a-api)
- [Como Rodar a Aplicação](#como-rodar-a-aplicação)
- [Funcionalidades](#funcionalidades)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Endpoints da API](#endpoints-da-api)
- [Banco de Dados](#banco-de-dados)
- [Desenvolvimento Local](#desenvolvimento-local)
- [Testes](#testes)
- [Solução de Problemas](#solução-de-problemas)

---

## Como Rodar a Aplicação

### Opção 1: Comando Único (Mais Simples)

```bash
./run.cmd
```

Este comando funciona em:
- ✅ Linux
- ✅ macOS
- ✅ Windows (CMD, PowerShell, Git Bash)

Este script automaticamente:
1. Instala Task (se necessário)
2. Verifica Docker e Docker Compose
3. Compila a aplicação (Maven build dentro do Docker)
4. Inicia todos os serviços (SQL Server, Flyway, API)
5. Aguarda até tudo estar pronto (healthcheck)
6. Detecta porta disponível (se 8080 estiver ocupada)

---

### Opção 2: Docker Direto (Se você já tem Docker)

**Funciona em todos os sistemas operacionais (Windows, macOS, Linux)**

Você só precisa ter Docker instalado. Execute um único comando:

```bash
docker compose up -d --wait
```

Pronto! A API estará rodando em `http://localhost:8080`

**O que acontece automaticamente:**
1. Aplicação é compilada (Maven build dentro do Docker)
2. SQL Server é iniciado e fica pronto
3. Banco de dados `portfoliodb` é criado
4. Todas as tabelas são criadas (migrations Flyway)
5. Dados de exemplo são inseridos
6. API inicia e aguarda ficar saudável (healthcheck)
7. Pronto para uso!

Para parar tudo:
```bash
docker compose down
```

Para parar e apagar os dados do banco:
```bash
docker compose down -v
```

---

### Comandos Úteis

Depois de rodar o setup, você pode usar estes comandos:

```bash
task docker-up     # Subir todos os serviços
task docker-down   # Parar todos os serviços
task logs          # Ver logs
task status        # Ver status dos containers
task health        # Verificar se a API está funcionando
task test          # Rodar testes
task help          # Ver todos os comandos disponíveis
```

---

## Como Testar a API

### Forma 1: Usando o Swagger (Mais Fácil - Recomendado)

**Swagger não precisa de autenticação!** Você pode testar tudo direto pelo navegador.

1. Certifique-se que a aplicação está rodando
2. Abra no navegador: http://localhost:8080/api/v1/swagger-ui.html
3. Pronto! Você pode testar todos os endpoints visualmente

**Como autenticar no Swagger para testar endpoints protegidos:**

A API usa **OAuth2 Authorization Code Flow** com login de usuário.

1. Abra o Swagger UI: http://localhost:8080/api/v1/swagger-ui/index.html
2. Clique no botão **"Authorize"** (cadeado verde no topo da página)
3. Na janela que abrir, preencha:
   - **client_id**: `portfolio-web-app`
   - **client_secret**: `webapp-secret`
   - Marque os escopos: `read`, `write`, `openid`, `profile`
4. Clique em **"Authorize"**
5. Você será redirecionado para a página de login
6. Faça login com uma das credenciais abaixo:
   - **Cliente**: `joao.silva@example.com` / `customer123`
   - **Admin**: `admin@demo.local` / `admin123`
7. Após o login, você será redirecionado de volta ao Swagger
8. O botão "Authorize" agora mostra **"Logout"** - você está autenticado!
9. Agora você pode testar todos os endpoints protegidos!

**Nota:** O token JWT é gerado automaticamente após o login e renovado automaticamente quando expira.

---

### Forma 2: Linha de Comando (curl)

**Estes comandos funcionam em:**
- ✅ Windows 10/11 (CMD ou PowerShell - curl já vem instalado)
- ✅ Windows com Git Bash
- ✅ macOS
- ✅ Linux

**⚠️ IMPORTANTE:** A autenticação via linha de comando é **mais complexa** porque requer OAuth2 Authorization Code Flow com login no navegador. **Recomendamos usar o Swagger UI para testes** (muito mais fácil).

**Se você realmente precisa usar curl:**

O fluxo completo envolve:
1. Iniciar o fluxo OAuth2 (`/oauth2/authorize`)
2. Fazer login no navegador
3. Capturar o código de autorização do redirect
4. Trocar o código por um access token

**Alternativa simples (apenas para testes):** Use o token gerado pelo Swagger:

1. Autentique-se no Swagger UI (veja instruções acima)
2. Abra o Developer Tools do navegador (F12)
3. Vá em Application → Local Storage → http://localhost:8080
4. Copie o valor do token OAuth2
5. Use o token nos comandos curl abaixo

**Passo: Usar o token para chamar os endpoints**

Substitua `SEU_TOKEN_AQUI` pelo token que você copiou:

```bash
curl -X GET http://localhost:8080/api/v1/perfil-risco/1 -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

---

### Exemplos de Requisições

**Simular um investimento:**
```bash
curl -X POST http://localhost:8080/api/v1/simular-investimento -H "Content-Type: application/json" -H "Authorization: Bearer SEU_TOKEN_AQUI" -d "{\"clienteId\":123,\"valor\":10000.00,\"prazoMeses\":12,\"tipoProduto\":\"CDB\"}"
```

**Consultar perfil de risco de um cliente:**
```bash
curl -X GET http://localhost:8080/api/v1/perfil-risco/123 -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

**Ver produtos recomendados por perfil:**
```bash
curl -X GET http://localhost:8080/api/v1/produtos-recomendados/Moderado -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

**Ver histórico de investimentos:**
```bash
curl -X GET http://localhost:8080/api/v1/investimentos/123 -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

**Ver todas as simulações:**
```bash
curl -X GET http://localhost:8080/api/v1/simulacoes -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

**Verificar se a API está funcionando (sem autenticação):**
```bash
curl http://localhost:8080/api/v1/actuator/health
```

---

## Funcionalidades

- ✅ Simulação de investimentos com validação de produtos
- ✅ Perfilamento dinâmico de risco baseado no comportamento do cliente
- ✅ Recomendação de produtos por perfil de risco
- ✅ Histórico de investimentos
- ✅ Telemetria e métricas de desempenho
- ✅ Autenticação OAuth2
- ✅ API RESTful com documentação OpenAPI (Swagger)

### Perfis de Risco

O sistema classifica clientes automaticamente em três perfis:

- **Conservador**: Baixa tolerância ao risco, foco em liquidez
- **Moderado**: Equilíbrio entre liquidez e rentabilidade
- **Agressivo**: Foco em alta rentabilidade, aceita mais risco

A classificação considera:
- Volume total de investimentos
- Frequência das transações
- Preferências de produtos

---

## Tecnologias Utilizadas

- Java 21
- Spring Boot 3.4.11
- SQL Server 2022
- Docker & Docker Compose
- Flyway (migrações de banco de dados)
- OAuth2 (Spring Authorization Server)
- Swagger/OpenAPI (documentação)
- JUnit 5 + Mockito (testes)

---

## Endpoints da API

### Autenticação
- `POST /oauth2/token` - Obter access token OAuth2
- `POST /oauth2/introspect` - Validar token
- `POST /oauth2/revoke` - Revogar token

### Simulações e Investimentos
- `POST /simular-investimento` - Simular um investimento
- `GET /investimentos/{clienteId}` - Consultar histórico de investimentos
- `GET /simulacoes` - Listar todas as simulações
- `GET /simulacoes/por-produto-dia` - Agregação de simulações por produto/dia

### Perfil e Recomendações
- `GET /perfil-risco/{clienteId}` - Consultar perfil de risco
- `GET /produtos-recomendados/{perfil}` - Produtos recomendados por perfil

### Monitoramento
- `GET /telemetria` - Métricas de telemetria do serviço
- `GET /actuator/health` - Verificação de saúde da API
- `GET /actuator/prometheus` - Métricas em formato Prometheus

**Observabilidade**: Prometheus coleta métricas automaticamente em http://localhost:9090

**Documentação completa:** http://localhost:8080/api/v1/swagger-ui.html

---

## Banco de Dados

### Schema

O banco de dados é criado automaticamente quando você roda o Docker.

**Tabelas principais:**
- `produtos` - Produtos de investimento (CDB, LCI, LCA, Tesouro Direto, Fundos)
- `simulacoes` - Histórico de simulações realizadas
- `investimentos` - Histórico de investimentos dos clientes
- `telemetria` - Métricas de uso e performance

### Migrações (Flyway)

As migrações são executadas automaticamente na ordem:

1. `V1__create_products_table.sql` - Cria tabela de produtos
2. `V2__create_simulations_table.sql` - Cria tabela de simulações
3. `V3__create_investments_table.sql` - Cria tabela de investimentos
4. `V4__create_telemetry_table.sql` - Cria tabela de telemetria
5. `V5__seed_sample_products.sql` - Insere dados de exemplo

Arquivos em: `src/main/resources/db/migration/`

### Variáveis de Ambiente

Você pode customizar a conexão com o banco:

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| DB_HOST | Host do banco | localhost |
| DB_PORT | Porta do banco | 1433 |
| DB_NAME | Nome do banco | portfoliodb |
| DB_USER | Usuário | sa |
| DB_PASSWORD | Senha | YourStrong@Passw0rd |

---

## Desenvolvimento Local

### Pré-requisitos

- Java 21
- Maven 3.9+
- Docker (para o banco de dados)

### Instalando Java e Maven

**Opção fácil - SDKMAN (Linux/macOS/Git Bash):**
```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env install
```

**Opção manual:** Baixe e instale Java 21 e Maven 3.9+ manualmente.

---

### Rodando localmente (sem Docker para a API)

**Todos os sistemas operacionais:**

**Passo 1:** Suba o banco de dados com Docker:
```bash
docker compose up sqlserver sqlserver-init flyway -d
```

**Passo 2:** Rode a aplicação:

Se instalou com SDKMAN:
```bash
sdk env
mvn spring-boot:run
```

Se instalou manualmente:
```bash
mvn spring-boot:run
```

A API vai rodar em `http://localhost:8080` e conectar no SQL Server em `localhost:1433`.

---

## Testes

### Rodar todos os testes

**Todos os sistemas operacionais:**
```bash
mvn test
```

### Cobertura

- **Testes Unitários:** Serviços, utilitários e lógica de negócio
- **Testes de Integração:** Endpoints da API completos
- **Framework:** JUnit 5 + Mockito

**Cobertura inclui:**
- Cálculos de investimento
- Algoritmo de perfil de risco
- Validação de produtos
- Autenticação OAuth2

**Estrutura:**
```
src/test/java/
├── integration/    - Testes de integração da API
├── service/        - Testes unitários de serviços
└── util/           - Testes unitários de utilitários
```

---

## Build da Aplicação

**Todos os sistemas operacionais:**

**Com testes:**
```bash
mvn clean package
```

**Sem testes (mais rápido):**
```bash
mvn clean package -DskipTests
```

O arquivo JAR será gerado em: `target/dynamic-portfolio-api-1.0.0.jar`

**Para rodar o JAR:**
```bash
java -jar target/dynamic-portfolio-api-1.0.0.jar
```

**Importante:** O banco de dados precisa estar rodando antes de executar o JAR.

---

## Arquitetura Docker

O Docker Compose orquestra 5 containers:

```
┌─────────────────┐
│  SQL Server     │ ◄── Verificações de saúde garantem que está pronto
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ sqlserver-init  │ ◄── Cria o banco de dados portfoliodb
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│    Flyway       │ ◄── Executa todas as migrações (V1-V5)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│      API        │ ◄── Inicia após as migrações completarem
└────────┬────────┘     Expõe métricas em /actuator/prometheus
         │
         ▼
┌─────────────────┐
│   Prometheus    │ ◄── Coleta métricas da API a cada 15s
└─────────────────┘     Armazena histórico de métricas
```

**Containers:**
- `portfolio-sqlserver` - SQL Server 2022 (persistente)
- `sqlserver-init` - Inicialização do banco (temporário)
- `flyway` - Executor de migrações (temporário)
- `portfolio-api` - Aplicação Spring Boot (persistente)
- `portfolio-prometheus` - Coleta e armazena métricas (persistente)

---

## Solução de Problemas

### A aplicação não sobe

**Ver os logs:**
```bash
docker compose logs
```

**Ver log de um serviço específico:**
```bash
docker compose logs api
docker compose logs sqlserver
docker compose logs flyway
```

---

### Erro de conexão com banco de dados

**Verificar se o SQL Server está saudável:**
```bash
docker compose ps
```

Deve mostrar `healthy` para `portfolio-sqlserver`.

**Se estiver "unhealthy", reinicie tudo:**
```bash
docker compose down -v
docker compose up -d
```

---

### Migrações Flyway falharam

**Ver o que aconteceu:**
```bash
docker compose logs flyway
```

**Resetar tudo e começar de novo:**
```bash
docker compose down -v
docker compose up -d
```

**Atenção:** Isso apaga todos os dados do banco!

---

### Porta 8080 já está em uso

**Descobrir o que está usando a porta 8080:**

**Linux/macOS:**
```bash
lsof -i :8080
```

**Windows (CMD ou PowerShell):**
```cmd
netstat -ano | findstr :8080
```

**Usar outra porta:**

**Linux/macOS/Git Bash:**
```bash
SERVER_PORT=8081 mvn spring-boot:run
```

**Windows CMD:**
```cmd
set SERVER_PORT=8081 && mvn spring-boot:run
```

**Windows PowerShell:**
```powershell
$env:SERVER_PORT=8081; mvn spring-boot:run
```

---

### Problema ao compilar (mvn package falha)

**Verificar versão do Java:**
```bash
java -version
```

Tem que ser **Java 21**. Se não for, instale Java 21.

**Com SDKMAN (Linux/macOS/Git Bash):**
```bash
sdk env
mvn clean install
```

**Sem SDKMAN (qualquer SO):**
```bash
mvn clean install
```

---

## Credenciais OAuth2

### Cliente Web (Swagger UI)
**Client ID:** `portfolio-web-app`
**Client Secret:** `webapp-secret`
**Grant Type:** `authorization_code`, `refresh_token`
**Scopes:** `openid`, `profile`, `read`, `write`

### Credenciais de Login
**Cliente Regular:**
- Email: `joao.silva@example.com`
- Senha: `customer123`
- Perfil: CUSTOMER (acesso apenas aos próprios dados)

**Administrador:**
- Email: `admin@demo.local`
- Senha: `admin123`
- Perfil: ADMIN (acesso a todos os dados - apenas dev)

---

## Licença

Este projeto é para fins educacionais e de demonstração.
