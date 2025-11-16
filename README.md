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
- **Recomendado**: Use [SDKMAN](https://sdkman.io/) para instalação facilitada

#### Configuração Rápida com SDKMAN

Instalar SDKMAN:
```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
```

Instalar Java e Maven (o projeto inclui `.sdkmanrc`):
```bash
sdk env install
```

Ou instalar manualmente:
```bash
sdk install java 21.0.8-amzn
sdk install maven 3.9.9
```

## Início Rápido com Docker

1. Clone o repositório
2. Navegue até o diretório do projeto
3. Inicie a aplicação:

```bash
docker compose up -d
```

A API estará disponível em `http://localhost:8080`

**O que acontece durante a inicialização:**
1. Container do SQL Server inicia com verificações de saúde
2. Container de inicialização cria o banco de dados `portfoliodb`
3. Container do Flyway executa todas as migrações do banco de dados
4. Container da API inicia após a conclusão bem-sucedida das migrações

O banco de dados é criado, migrado e populado com dados de exemplo automaticamente.

## Desenvolvimento Local

### Configuração do Banco de Dados

Inicie o SQL Server e execute as migrações:

```bash
# Iniciar SQL Server
docker compose up sqlserver -d

# Aguarde o SQL Server ficar saudável, então execute init e migrações
docker compose up sqlserver-init
docker compose up flyway
```

Ou use o banco de dados da stack completa do Docker Compose:
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

A API se conectará ao container do SQL Server em `localhost:1433`.

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

## Documentação da API

Com a aplicação em execução, acesse o Swagger UI em:

```
http://localhost:8080/swagger-ui.html
```

Especificação OpenAPI disponível em:

```
http://localhost:8080/api-docs
```

## Autenticação

Todos os endpoints exceto `/auth/login` requerem autenticação JWT.

### Obtendo um Token

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo"}'
```

### Usando o Token

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

- **Conservador**: Baixa tolerância ao risco, foco em liquidez
- **Moderado**: Tolerância equilibrada ao risco
- **Agressivo**: Alta tolerância ao risco, foco em rentabilidade

A classificação é baseada em:
- Volume total de investimentos
- Frequência de transações
- Preferências de produtos de investimento

## Esquema do Banco de Dados

A aplicação usa Flyway para migrações de banco de dados. O esquema é criado automaticamente na inicialização via container dedicado do Flyway.

**Processo de Migração:**
1. `V1__create_products_table.sql` - Tabela de produtos
2. `V2__create_simulations_table.sql` - Tabela de simulações
3. `V3__create_investments_table.sql` - Tabela de investimentos
4. `V4__create_telemetry_table.sql` - Tabela de telemetria
5. `V5__seed_sample_products.sql` - Dados de exemplo de produtos

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

## Compilando do Código-fonte

### Compilar com Testes

```bash
mvn clean package
```

O arquivo JAR será criado em `target/dynamic-portfolio-api-1.0.0.jar`

### Compilar sem Testes

```bash
mvn clean package -DskipTests
```

### Executar JAR Compilado

```bash
java -jar target/dynamic-portfolio-api-1.0.0.jar
```

**Nota**: Certifique-se de que o banco de dados está em execução e as variáveis de ambiente estão configuradas antes de executar o JAR.

## Arquitetura Docker

A configuração do Docker Compose usa um processo de inicialização multi-estágio:

```
┌─────────────────┐
│  SQL Server     │ ◄── Verificações de saúde garantem estado pronto
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
│      API        │ ◄── Inicia após conclusão das migrações
└─────────────────┘
```

**Containers:**
- `portfolio-sqlserver` - SQL Server 2022 (persistente)
- `sqlserver-init` - Criação do banco de dados (executa uma vez)
- `flyway` - Executor de migrações (executa uma vez)
- `portfolio-api` - Aplicação Spring Boot (persistente)

## Solução de Problemas

### Problemas com Docker

**Containers não iniciam:**
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

**Falhas de migração do Flyway:**
```bash
# Verificar logs do Flyway
docker compose logs flyway

# Resetar banco de dados e tentar novamente
docker compose down -v
docker compose up -d
```

### Problemas de Desenvolvimento Local

**Falha na compilação Maven:**
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
