# API de Portfólio Dinâmico

Sistema de simulação de investimentos para produtos financeiros brasileiros incluindo CDBs, LCIs, LCAs, Tesouro Direto e fundos de investimento.

## Índice

- [Como Rodar a Aplicação](#como-rodar-a-aplicação)
- [Como Testar a API](#como-testar-a-api)
- [Funcionalidades](#funcionalidades)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Desenvolvimento Local](#desenvolvimento-local)
- [Endpoints da API](#endpoints-da-api)
- [Banco de Dados](#banco-de-dados)
- [Testes](#testes)
- [Solução de Problemas](#solução-de-problemas)

---

## Como Rodar a Aplicação

### Opção 1: Docker (Mais Simples - Recomendado)

**Funciona em todos os sistemas operacionais (Windows, macOS, Linux)**

Você só precisa ter Docker instalado. Execute um único comando:

```bash
docker compose up -d
```

Pronto! A API estará rodando em `http://localhost:8080`

**O que acontece automaticamente:**
1. SQL Server é iniciado
2. Banco de dados `portfoliodb` é criado
3. Todas as tabelas são criadas (migrations)
4. Dados de exemplo são inseridos
5. API inicia e fica disponível

Para parar tudo:
```bash
docker compose down
```

Para parar e apagar os dados do banco:
```bash
docker compose down -v
```

---

### Opção 2: Setup Automático com Task

**Linux/macOS/Git Bash (Windows):**
```bash
./setup.sh && task setup
```

**Usuários Windows sem Git Bash:**
1. Baixe e instale o Git Bash: https://git-scm.com/downloads
2. Abra o Git Bash
3. Execute: `./setup.sh && task setup`

Isso instala tudo automaticamente e inicia a aplicação.

**Comandos úteis após o setup:**
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
2. Abra no navegador: http://localhost:8080/swagger-ui.html
3. Pronto! Você pode testar todos os endpoints visualmente

**Como autenticar no Swagger para testar endpoints protegidos:**

1. No Swagger, encontre o endpoint `POST /auth/login`
2. Clique em "Try it out"
3. Use este JSON:
   ```json
   {
     "username": "demo"
   }
   ```
4. Clique em "Execute"
5. Copie o valor do campo `token` que aparece na resposta
6. Clique no botão **"Authorize"** no topo da página
7. Cole o token no campo (no formato: `Bearer SEU_TOKEN_AQUI`)
8. Clique em "Authorize" e depois "Close"
9. Agora você pode testar todos os endpoints protegidos!

---

### Forma 2: Linha de Comando (curl)

**Estes comandos funcionam em:**
- ✅ Windows 10/11 (CMD ou PowerShell - curl já vem instalado)
- ✅ Windows com Git Bash
- ✅ macOS
- ✅ Linux

**Passo 1: Pegar um token de autenticação**

```bash
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d "{\"username\":\"demo\"}"
```

**Resposta:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "username": "demo"
  },
  "message": "Login successful"
}
```

Copie o valor do `token`.

**Passo 2: Usar o token para chamar os endpoints**

Substitua `SEU_TOKEN_AQUI` pelo token que você copiou:

```bash
curl -X GET http://localhost:8080/perfil-risco/123 -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

---

### Exemplos de Requisições

**Simular um investimento:**
```bash
curl -X POST http://localhost:8080/simular-investimento -H "Content-Type: application/json" -H "Authorization: Bearer SEU_TOKEN_AQUI" -d "{\"clienteId\":123,\"valor\":10000.00,\"prazoMeses\":12,\"tipoProduto\":\"CDB\"}"
```

**Consultar perfil de risco de um cliente:**
```bash
curl -X GET http://localhost:8080/perfil-risco/123 -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

**Ver produtos recomendados por perfil:**
```bash
curl -X GET http://localhost:8080/produtos-recomendados/Moderado -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

**Ver histórico de investimentos:**
```bash
curl -X GET http://localhost:8080/investimentos/123 -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

**Ver todas as simulações:**
```bash
curl -X GET http://localhost:8080/simulacoes -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

**Verificar se a API está funcionando (sem autenticação):**
```bash
curl http://localhost:8080/actuator/health
```

---

## Funcionalidades

- ✅ Simulação de investimentos com validação de produtos
- ✅ Perfilamento dinâmico de risco baseado no comportamento do cliente
- ✅ Recomendação de produtos por perfil de risco
- ✅ Histórico de investimentos
- ✅ Telemetria e métricas de desempenho
- ✅ Autenticação JWT
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
- JWT (autenticação)
- Swagger/OpenAPI (documentação)
- JUnit 5 + Mockito (testes)

---

## Endpoints da API

### Autenticação
- `POST /auth/login` - Gerar token JWT

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

**Documentação completa:** http://localhost:8080/swagger-ui.html

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
| JWT_SECRET | Chave JWT | (gerada automaticamente) |

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
- Autenticação JWT

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

O Docker Compose orquestra 4 containers:

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
└─────────────────┘
```

**Containers:**
- `portfolio-sqlserver` - SQL Server 2022 (persistente)
- `sqlserver-init` - Inicialização do banco (temporário)
- `flyway` - Executor de migrações (temporário)
- `portfolio-api` - Aplicação Spring Boot (persistente)

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

## Credenciais de Teste

**Usuário:** `demo`
**Senha:** não é necessária (autenticação simplificada para demonstração)

---

## Licença

Este projeto é para fins educacionais e de demonstração.
