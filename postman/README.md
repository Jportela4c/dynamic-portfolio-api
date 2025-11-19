# Testes de Contrato com Newman

Testes abrangentes de contrato de API usando Postman/Newman para validar a API Dynamic Portfolio contra a especificação do desafio.

## Visão Geral

- **27 requisições** com **mais de 100 asserções**
- Testa todos os 7 endpoints da API conforme especificação do desafio
- Valida autenticação, esquemas de requisição/resposta, cálculos e performance
- Suporta múltiplos ambientes (local, teste, CI/CD)

## Início Rápido

### Pré-requisitos

```bash
# Instalar Newman globalmente
npm install -g newman newman-reporter-htmlextra

# Ou usar via Docker
docker pull postman/newman
```

### Executar Testes

```bash
# Contra Docker local (recomendado)
task newman:local

# Com banco de dados completamente limpo
task newman:fresh

# Gerar relatório HTML detalhado
task newman:report
```

## Estrutura da Coleção de Testes

### 01 - Autenticação (3 requisições)
- ✅ Login com credenciais válidas → Salva token JWT no ambiente
- ✅ Login com credenciais inválidas → Espera 401
- ✅ Acesso a endpoint protegido sem token → Espera 401/403

### 02 - Simulação de Investimento (7 requisições)
- ✅ Simular investimento CDB → Valida esquema de resposta
- ✅ Simular investimento LCI → Valida tipo de produto
- ✅ Valor inválido (negativo) → Espera 400
- ✅ Prazo inválido (zero) → Espera 400
- ✅ Tipo de produto inválido → Espera 400/404
- ✅ Obter todas as simulações → Valida resposta em array
- ✅ Obter agregações diárias → Valida campos de agregação

### 03 - Perfil de Risco (6 requisições)
- ✅ Obter perfil de risco (cliente conservador) → Valida cálculo de perfil
- ✅ Obter perfil de risco (cliente agressivo) → Valida perfil
- ✅ Obter perfil de risco (cliente inexistente) → Espera 404 ou padrão
- ✅ Obter produtos recomendados (conservador) → Filtra por baixo risco
- ✅ Obter produtos recomendados (agressivo) → Todos os níveis de risco permitidos
- ✅ Obter histórico de investimentos → Valida dados históricos

### 04 - Telemetria (1 requisição)
- ✅ Obter dados de telemetria → Valida formato de métricas

### 05 - Casos Extremos & Performance (3 requisições)
- ✅ Valor limite (mínimo) → Testa casos extremos
- ✅ Valor grande → Valida precisão de cálculo
- ✅ Verificação de tempo de resposta → Garante < 500ms

### Asserções Globais (em todas as requisições)
- Tempo de resposta < 2000ms
- Content-Type: application/json
- Sem erros inesperados do servidor

## Ambientes

### Docker Local (`local-docker.postman_environment.json`)
```json
{
  "baseUrl": "http://localhost:8080",
  "username": "admin",
  "password": "admin123"
}
```

### Ambiente de Teste (`test.postman_environment.json`)
```json
{
  "baseUrl": "http://localhost:8081",
  "username": "testuser",
  "password": "testpass123"
}
```

### Pipeline CI/CD (`ci.postman_environment.json`)
```json
{
  "baseUrl": "http://api:8080",
  "username": "admin",
  "password": "admin123"
}
```

## Configuração de Dados de Teste

O arquivo `scripts/setup-test-data.sql` popula o banco de dados com:

### Usuários
- `admin` / `admin123` - Usuário administrador para testes
- `testuser` / `testpass123` - Usuário regular para testes

### Produtos (7 no total)
- **CDB Banco Líder 120% CDI** - Conservador, R$ 5.000 mínimo
- **LCI Imobiliário** - Conservador, R$ 10.000 mínimo
- **LCA Agronegócio** - Conservador, R$ 10.000 mínimo
- **Tesouro Direto Selic** - Moderado, R$ 1.000 mínimo
- **CDB Banco Digital 130% CDI** - Moderado, R$ 10.000 mínimo
- **Fundo Multimercado** - Agressivo, R$ 50.000 mínimo
- **Fundo Ações** - Agressivo, R$ 100.000 mínimo

### Clientes de Teste
- **Cliente 1** - Perfil conservador (baixo volume, baixa frequência)
- **Cliente 2** - Perfil moderado (volume médio, balanceado)
- **Cliente 3** - Perfil agressivo (alto volume, alta frequência)

### Histórico de Investimentos
- 15 investimentos históricos distribuídos entre 3 clientes
- Projetados para produzir as classificações de perfil de risco esperadas

### Dados de Exemplo
- 4 simulações para testar endpoints GET
- 9 registros de telemetria para validação de métricas

## Executando os Testes

### Via Taskfile (Recomendado)

```bash
# Teste rápido contra Docker em execução
task newman:local

# Teste completo em ambiente limpo
task newman:fresh

# Testar contra ambiente específico
task newman:test

# Modo CI/CD (fail fast)
task newman:ci

# Gerar relatório detalhado com preview no navegador
task newman:report
```

### Via Newman CLI

```bash
# Execução básica
newman run Dynamic-Portfolio-API.postman_collection.json \
  -e environments/local-docker.postman_environment.json

# Com relatório HTML
newman run Dynamic-Portfolio-API.postman_collection.json \
  -e environments/local-docker.postman_environment.json \
  --reporters cli,htmlextra \
  --reporter-htmlextra-export ../reports/newman-report.html

# Modo CI/CD (falha no primeiro erro)
newman run Dynamic-Portfolio-API.postman_collection.json \
  -e environments/ci.postman_environment.json \
  --bail \
  --reporters cli,json \
  --reporter-json-export ../reports/newman-results.json
```

### Via Docker

```bash
# Executar testes em container Newman Docker
docker run -t --network host \
  -v $(pwd):/etc/newman \
  postman/newman run Dynamic-Portfolio-API.postman_collection.json \
  -e environments/local-docker.postman_environment.json
```

## Populando Dados de Teste

### População Manual

```bash
# Popular via Task
task seed-test-data

# Popular diretamente via Docker exec
docker compose exec -T sqlserver /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P YourStrong@Passw0rd -C \
  -i /scripts/setup-test-data.sql
```

### População Automática

Os comandos `task newman:local` e `task newman:fresh` populam automaticamente os dados de teste antes de executar os testes.

## Integração CI/CD

### GitHub Actions

O workflow `.github/workflows/newman-tests.yml` executa em:
- Push para branches `main` ou `develop`
- Pull requests para `main` ou `develop`
- Disparo manual via workflow dispatch

**Passos do Workflow:**
1. Checkout do código
2. Configurar Java 21 e Node.js 20
3. Instalar Newman e reporters
4. Build da aplicação
5. Iniciar stack Docker Compose
6. Verificar se serviços estão saudáveis
7. Popular dados de teste
8. Executar testes Newman (modo fail fast)
9. Upload de relatórios HTML e JSON como artefatos
10. Mostrar logs em caso de falha
11. Limpeza de containers

**Artefatos:**
- `newman-report.html` - Relatório visual de testes (retenção de 30 dias)
- `newman-results.json` - Resultados legíveis por máquina (retenção de 30 dias)

### GitLab CI

```yaml
newman-tests:
  stage: test
  image: node:20
  services:
    - docker:dind
  before_script:
    - npm install -g newman newman-reporter-htmlextra
  script:
    - docker compose up -d --wait
    - task seed-test-data
    - newman run postman/Dynamic-Portfolio-API.postman_collection.json
        -e postman/environments/ci.postman_environment.json
        --bail
        --reporters cli,htmlextra
        --reporter-htmlextra-export reports/newman-report.html
  artifacts:
    when: always
    paths:
      - reports/newman-report.html
    expire_in: 30 days
```

## Asserções de Teste

### Exemplo de Validação de Esquema

```javascript
pm.test("Resposta corresponde ao esquema da especificação", () => {
    const schema = {
        type: "object",
        required: ["produtoValidado", "resultadoSimulacao"],
        properties: {
            produtoValidado: {
                type: "object",
                required: ["id", "nome", "tipo", "rentabilidade"]
            },
            resultadoSimulacao: {
                type: "object",
                required: ["valorInicial", "valorFinal", "lucro"]
            }
        }
    };
    pm.response.to.have.jsonSchema(schema);
});
```

### Exemplo de Validação de Cálculo

```javascript
pm.test("Cálculo está correto", () => {
    const resultado = pm.response.json().resultadoSimulacao;
    const valorInicial = parseFloat(resultado.valorInicial);
    const valorFinal = parseFloat(resultado.valorFinal);
    const lucro = parseFloat(resultado.lucro);

    pm.expect(valorFinal).to.be.greaterThan(valorInicial);
    pm.expect(lucro).to.equal(valorFinal - valorInicial);
});
```

### Exemplo de Gerenciamento de Token JWT

```javascript
// Salvar token da resposta de login
if (pm.response.code === 200) {
    const json = pm.response.json();
    pm.environment.set('jwtToken', json.token);
    console.log('Token JWT salvo no ambiente');
}
```

## Solução de Problemas

### Testes Falham com Erro de Conexão

```bash
# Garantir que Docker está em execução
docker compose ps

# Verificar saúde da API
curl http://localhost:8080/api/v1/actuator/health

# Reiniciar serviços
task docker-down
task docker-up
```

### Dados de Teste Não Encontrados

```bash
# Re-popular dados de teste
task seed-test-data

# Verificar se dados existem
docker compose exec sqlserver /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P YourStrong@Passw0rd -C \
  -Q "SELECT COUNT(*) FROM portfoliodb.dbo.products"
```

### Autenticação Falha

```bash
# Verificar se usuários existem no banco de dados
docker compose exec sqlserver /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P YourStrong@Passw0rd -C \
  -Q "SELECT * FROM portfoliodb.dbo.users"

# Verificar se JWT secret corresponde à configuração da aplicação
docker compose exec api env | grep JWT_SECRET
```

### Testes Dão Timeout

```bash
# Aumentar tempo de espera no Taskfile.yml
# Mudar: sleep 10
# Para:  sleep 20

# Ou verificar se migrações do banco completaram
task logs-flyway
```

## Relatórios

### Funcionalidades do Relatório HTML

O relatório HTML (`newman-report.html`) inclui:
- ✅ Resumo de testes com contagens de sucesso/falha
- ✅ Estatísticas de tempo de resposta
- ✅ Detalhes de requisição/resposta para cada teste
- ✅ Asserções com detalhes de falhas
- ✅ Suporte a tema escuro
- ✅ Filtrável por status de sucesso/falha

### Estrutura dos Resultados JSON

```json
{
  "collection": { "info": { "name": "..." } },
  "run": {
    "stats": {
      "tests": { "total": 100, "failed": 0 },
      "assertions": { "total": 100, "failed": 0 },
      "requests": { "total": 27, "failed": 0 }
    },
    "timings": {
      "responseAverage": 245.3,
      "responseMin": 87,
      "responseMax": 512
    }
  }
}
```

## Melhores Práticas

### 1. Sempre Use Dados Limpos para Testes de Contrato
```bash
task newman:fresh  # Derruba, reconstrói, popula, testa
```

### 2. Execute Testes Antes do Deploy
```bash
# No pipeline CI/CD
task newman:ci
```

### 3. Gere Relatórios para Debugging
```bash
task newman:report  # Abre relatório HTML no navegador
```

### 4. Teste Contra Múltiplos Ambientes
```bash
task newman:local   # Desenvolvimento
task newman:test    # Ambiente de teste
task newman:ci      # Ambiente CI/CD
```

### 5. Monitore Tendências de Performance
```bash
# Verificar tempos de resposta nos relatórios
# Alertar se média > 500ms
```

## Manutenção

### Adicionando Novos Testes

1. Abrir coleção na GUI do Postman
2. Adicionar nova requisição na pasta apropriada
3. Adicionar scripts de teste (JavaScript)
4. Exportar coleção atualizada
5. Substituir `Dynamic-Portfolio-API.postman_collection.json`

### Atualizando Dados de Teste

1. Editar `scripts/setup-test-data.sql`
2. Executar `task seed-test-data` para verificar
3. Commitar alterações

### Atualizando Ambientes

1. Editar `environments/*.postman_environment.json`
2. Atualizar baseUrl, credenciais ou variáveis
3. Testar com `task newman:local` ou `task newman:test`

## Recursos

- [Documentação do Newman](https://learning.postman.com/docs/running-collections/using-newman-cli/command-line-integration-with-newman/)
- [Formato de Coleção Postman](https://schema.postman.com/)
- [newman-reporter-htmlextra](https://github.com/DannyDainton/newman-reporter-htmlextra)
- [Especificação do Desafio](../local-docs/00-challenge-specification-original.md)

## Suporte

Para problemas com:
- **Falhas de teste**: Verificar `reports/newman-report.html` para detalhes
- **Problemas de ambiente**: Verificar se containers Docker estão saudáveis
- **Problemas de dados**: Re-executar `task seed-test-data`
- **Problemas de CI/CD**: Verificar logs e artefatos do GitHub Actions
