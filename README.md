# API de Portfólio Dinâmico

Sistema de simulação de investimentos para produtos financeiros brasileiros incluindo CDBs, LCIs, LCAs, Tesouro Direto e fundos de investimento.

## Funcionalidades

- Simulação de investimentos com validação de produtos
- Perfilamento dinâmico de risco baseado no comportamento do cliente
- Recomendação de produtos por perfil de risco
- Histórico de investimentos
- Telemetria e métricas de desempenho
- Autenticação JWT
- API RESTful com documentação OpenAPI

## Stack Tecnológico

- Java 21
- Spring Boot 3.4.11
- SQL Server 2022
- Docker & Docker Compose
- Flyway para migrações de banco de dados
- JWT para autenticação
- Swagger/OpenAPI para documentação da API

## Pré-requisitos

### Para Docker (Recomendado)
- Docker 20.10+ e Docker Compose 2.0+

### Para Desenvolvimento Local
- Java 21
- Maven 3.9+
- **Recomendado**: Use [SDKMAN](https://sdkman.io/) para instalação fácil

#### Configuração Rápida com SDKMAN

Instalar SDKMAN:
```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
```

Instalar Java e Maven (projeto inclui `.sdkmanrc`):
```bash
sdk env install
```

Ou instalar manualmente:
```bash
sdk install java 21.0.8-amzn
sdk install maven 3.9.9
```

## Início Rápido (Configuração Automatizada)

**Forma mais fácil - comando único:**

```bash
./setup.sh && task setup
```

Isso automaticamente:
- Instala Task (executor de tarefas multiplataforma)
- Verifica todas as dependências
- Opcionalmente instala ferramentas de desenvolvimento (Java, Maven)
- Inicia todos os serviços Docker

A API estará disponível em `http://localhost:8080`

### Início Manual com Docker

Alternativamente, inicie os serviços manualmente:

```bash
docker compose up -d
```

**O que acontece durante a inicialização:**
1. Container SQL Server inicia com verificações de saúde
2. Container de inicialização cria o banco de dados `portfoliodb`
3. Container Flyway executa todas as migrações do banco de dados
4. Container da API inicia após as migrações serem concluídas com sucesso

O banco de dados é automaticamente criado, migrado e populado com dados de exemplo.

### Comandos Task

Após executar `./setup.sh`, use estes comandos:

```bash
task setup         # Configuração completa do projeto
task docker-up     # Iniciar todos os serviços
task docker-down   # Parar todos os serviços
task logs          # Visualizar todos os logs
task status        # Verificar status dos containers
task health        # Verificar saúde da API
task test          # Executar testes
task help          # Mostrar todos os comandos
```

Execute `task --list` para ver todas as tarefas disponíveis.

## Desenvolvimento Local

### Configuração do Banco de Dados

Iniciar SQL Server e executar migrações:

```bash
# Iniciar SQL Server
docker compose up sqlserver -d

# Aguardar SQL Server estar saudável, então executar init e migrações
docker compose up sqlserver-init
docker compose up flyway
```

Ou usar o banco de dados do stack completo Docker Compose:
```bash
docker compose up sqlserver sqlserver-init flyway -d
```

### Executar Aplicação Localmente

Com SDKMAN (usa automaticamente as versões corretas):
```bash
sdk env
mvn spring-boot:run
```

Sem SDKMAN:
```bash
mvn spring-boot:run
```

A API se conectará ao container SQL Server em `localhost:1433`.

## Endpoints da API

### Autenticação

- `POST /auth/login` - Gerar token JWT

### Operações de Investimento

- `POST /simular-investimento` - Simular investimento
- `GET /perfil-risco/{clienteId}` - Obter perfil de risco do cliente
- `GET /produtos-recomendados/{perfil}` - Obter produtos recomendados por perfil
- `GET /investimentos/{clienteId}` - Obter histórico de investimentos do cliente

### Métricas e Relatórios

- `GET /simulacoes` - Obter todas as simulações
- `GET /simulacoes/por-produto-dia` - Obter agregações diárias por produto
- `GET /telemetria` - Obter métricas de telemetria do serviço

## Testando a API

### 1. Acesse a Documentação Swagger (Sem Autenticação)

Uma vez que a aplicação esteja rodando, abra a interface Swagger no navegador:

```
http://localhost:8080/swagger-ui.html
```

ou diretamente:

```
http://localhost:8080/swagger-ui/index.html
```

**Swagger não requer autenticação** - você pode explorar todos os endpoints da API diretamente pelo navegador.

Especificação OpenAPI também disponível em:

```
http://localhost:8080/api-docs
```

### 2. Credenciais de Teste

Use estas credenciais para testar os endpoints autenticados:

**Usuário:** `demo`
**Senha:** não necessária (simplificado para demonstração)

### 3. Testando pelo Swagger UI

**Instruções passo a passo:**

1. Abra http://localhost:8080/swagger-ui.html
2. Expanda `POST /auth/login`
3. Clique em "Try it out"
4. Use o corpo da requisição:
   ```json
   {
     "username": "demo"
   }
   ```
5. Clique em "Execute"
6. Copie o valor do campo `token` da resposta
7. Clique no botão **"Authorize"** no topo da página
8. Cole o token no campo "Value" (no formato: `Bearer SEU_TOKEN`)
9. Clique em "Authorize" e depois em "Close"
10. Agora você pode testar todos os endpoints autenticados!

### 4. Testando via cURL

#### Obtendo um Token

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo"}'
```

Resposta:
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

#### Usando o Token

```bash
curl -X GET http://localhost:8080/perfil-risco/123 \
  -H "Authorization: Bearer SEU_TOKEN_JWT"
```

## Exemplos de Requisições

### Simular Investimento

```bash
curl -X POST http://localhost:8080/simular-investimento \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN" \
  -d '{
    "clienteId": 123,
    "valor": 10000.00,
    "prazoMeses": 12,
    "tipoProduto": "CDB"
  }'
```

### Obter Perfil de Risco

```bash
curl -X GET http://localhost:8080/perfil-risco/123 \
  -H "Authorization: Bearer SEU_TOKEN"
```

### Obter Produtos Recomendados

```bash
curl -X GET http://localhost:8080/produtos-recomendados/Moderado \
  -H "Authorization: Bearer SEU_TOKEN"
```

## Perfis de Risco

O sistema classifica clientes em três perfis de risco:

- **Conservador**: Baixa tolerância ao risco, focado em liquidez
- **Moderado**: Tolerância ao risco balanceada
- **Agressivo**: Alta tolerância ao risco, focado em retornos

A classificação é baseada em:
- Volume total de investimento
- Frequência de transações
- Preferências de produtos de investimento

## Schema do Banco de Dados

A aplicação usa Flyway para migrações de banco de dados. O schema é automaticamente criado na inicialização via container Flyway dedicado.

**Processo de Migração:**
1. `V1__create_products_table.sql` - Tabela de produtos
2. `V2__create_simulations_table.sql` - Tabela de simulações
3. `V3__create_investments_table.sql` - Tabela de investimentos
4. `V4__create_telemetry_table.sql` - Tabela de telemetria
5. `V5__seed_sample_products.sql` - Dados de produtos de exemplo

Tabelas principais:
- `produtos` - Produtos de investimento (CDB, LCI, LCA, Tesouro Direto, Fundos)
- `simulacoes` - Histórico de simulações
- `investimentos` - Histórico de investimentos do cliente
- `telemetria` - Métricas de desempenho

Todas as migrações estão localizadas em `src/main/resources/db/migration/`.

## Variáveis de Ambiente

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| DB_HOST | Host do banco de dados | localhost |
| DB_PORT | Porta do banco de dados | 1433 |
| DB_NAME | Nome do banco de dados | portfoliodb |
| DB_USER | Usuário do banco de dados | sa |
| DB_PASSWORD | Senha do banco de dados | YourStrong@Passw0rd |
| JWT_SECRET | Chave de assinatura JWT | (padrão codificado em base64) |

## Verificação de Saúde

```bash
curl http://localhost:8080/actuator/health
```

## Parando a Aplicação

```bash
docker compose down
```

Para remover volumes:

```bash
docker compose down -v
```

## Testes

O projeto inclui cobertura abrangente de testes:

### Executar Todos os Testes

```bash
mvn test
```

### Cobertura de Testes

- **Testes Unitários**: Camada de serviço, utilitários e lógica de negócio
- **Testes de Integração**: Endpoints da API com Spring Boot Test
- **Framework de Testes**: JUnit 5 + Mockito
- **Cobertura**: Lógica de negócio crítica incluindo:
  - Cálculos de investimento
  - Algoritmo de perfil de risco
  - Validação de produtos
  - Endpoints da API com autenticação

### Estrutura de Testes

```
src/test/java/
├── integration/    - Testes de integração completos da API
├── service/        - Testes unitários da camada de serviço
└── util/           - Testes unitários de classes utilitárias
```

## Construindo a Partir do Código Fonte

### Construir com Testes

```bash
mvn clean package
```

O arquivo JAR será criado em `target/dynamic-portfolio-api-1.0.0.jar`

### Construir sem Testes

```bash
mvn clean package -DskipTests
```

### Executar JAR Construído

```bash
java -jar target/dynamic-portfolio-api-1.0.0.jar
```

**Nota**: Certifique-se de que o banco de dados está rodando e as variáveis de ambiente estão configuradas antes de executar o JAR.

## Arquitetura Docker

O setup Docker Compose usa um processo de inicialização multi-estágio:

```
┌─────────────────┐
│  SQL Server     │ ◄── Verificações de saúde garantem estado pronto
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ sqlserver-init  │ ◄── Cria banco de dados portfoliodb
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│    Flyway       │ ◄── Executa todas as migrações (V1-V5)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│      API        │ ◄── Inicia após migrações completarem
└─────────────────┘
```

**Containers:**
- `portfolio-sqlserver` - SQL Server 2022 (persistente)
- `sqlserver-init` - Criação do banco de dados (executa uma vez)
- `flyway` - Executor de migrações (executa uma vez)
- `portfolio-api` - Aplicação Spring Boot (persistente)

## Solução de Problemas

### Problemas com Docker

**Containers não iniciando:**
```bash
# Verificar logs dos containers
docker compose logs

# Verificar serviço específico
docker compose logs sqlserver
docker compose logs flyway
docker compose logs api
```

**Problemas de conexão com banco de dados:**
```bash
# Verificar se SQL Server está saudável
docker compose ps

# Deve mostrar status "healthy" para portfolio-sqlserver
```

**Falhas de migração Flyway:**
```bash
# Verificar logs Flyway
docker compose logs flyway

# Resetar banco de dados e tentar novamente
docker compose down -v
docker compose up -d
```

### Problemas de Desenvolvimento Local

**Falha no build Maven:**
```bash
# Garantir versão correta do Java
java -version  # Deve ser 21

# Com SDKMAN
sdk env
mvn clean install
```

**Porta já em uso:**
```bash
# Verificar o que está usando a porta 8080
lsof -i :8080

# Ou usar porta diferente
SERVER_PORT=8081 mvn spring-boot:run
```

## Licença

Este projeto é para fins educacionais e de demonstração.
