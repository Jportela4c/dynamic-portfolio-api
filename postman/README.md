# Newman API Contract Tests

Comprehensive API contract testing using Postman/Newman for validating Dynamic Portfolio API against THE SPEC.

## Overview

- **27 requests** with **100+ assertions**
- Tests all 7 API endpoints from the challenge specification
- Validates authentication, request/response schemas, calculations, and performance
- Supports multiple environments (local, test, CI/CD)

## Quick Start

### Prerequisites

```bash
# Install Newman globally
npm install -g newman newman-reporter-htmlextra

# Or use via Docker
docker pull postman/newman
```

### Run Tests

```bash
# Against local Docker (recommended)
task newman:local

# With completely fresh database
task newman:fresh

# Generate detailed HTML report
task newman:report
```

## Test Collection Structure

### 01 - Authentication (3 requests)
- ✅ Login with valid credentials → Saves JWT token to environment
- ✅ Login with invalid credentials → Expects 401
- ✅ Access protected endpoint without token → Expects 401/403

### 02 - Investment Simulation (7 requests)
- ✅ Simulate CDB investment → Validates response schema
- ✅ Simulate LCI investment → Validates product type
- ✅ Invalid value (negative) → Expects 400
- ✅ Invalid term (zero) → Expects 400
- ✅ Invalid product type → Expects 400/404
- ✅ Get all simulations → Validates array response
- ✅ Get daily aggregations → Validates aggregation fields

### 03 - Risk Profile (6 requests)
- ✅ Get risk profile (conservative client) → Validates profile calculation
- ✅ Get risk profile (aggressive client) → Validates profile
- ✅ Get risk profile (non-existent client) → Expects 404 or default
- ✅ Get recommended products (conservative) → Filters by low risk
- ✅ Get recommended products (aggressive) → All risk levels allowed
- ✅ Get investment history → Validates history data

### 04 - Telemetry (1 request)
- ✅ Get telemetry data → Validates metrics format

### 05 - Edge Cases & Performance (3 requests)
- ✅ Boundary value (minimum) → Tests edge cases
- ✅ Large value → Validates calculation precision
- ✅ Response time check → Ensures < 500ms

### Global Assertions (on every request)
- Response time < 2000ms
- Content-Type: application/json
- No unexpected server errors

## Environments

### Local Docker (`local-docker.postman_environment.json`)
```json
{
  "baseUrl": "http://localhost:8080",
  "username": "admin",
  "password": "admin123"
}
```

### Test Environment (`test.postman_environment.json`)
```json
{
  "baseUrl": "http://localhost:8081",
  "username": "testuser",
  "password": "testpass123"
}
```

### CI/CD Pipeline (`ci.postman_environment.json`)
```json
{
  "baseUrl": "http://api:8080",
  "username": "admin",
  "password": "admin123"
}
```

## Test Data Setup

The `scripts/setup-test-data.sql` file seeds the database with:

### Users
- `admin` / `admin123` - Admin user for testing
- `testuser` / `testpass123` - Regular user for testing

### Products (7 total)
- **CDB Banco Líder 120% CDI** - Conservative, R$ 5,000 minimum
- **LCI Imobiliário** - Conservative, R$ 10,000 minimum
- **LCA Agronegócio** - Conservative, R$ 10,000 minimum
- **Tesouro Direto Selic** - Moderate, R$ 1,000 minimum
- **CDB Banco Digital 130% CDI** - Moderate, R$ 10,000 minimum
- **Fundo Multimercado** - Aggressive, R$ 50,000 minimum
- **Fundo Ações** - Aggressive, R$ 100,000 minimum

### Test Clients
- **Client 1** - Conservative profile (low volume, low frequency)
- **Client 2** - Moderate profile (medium volume, balanced)
- **Client 3** - Aggressive profile (high volume, high frequency)

### Investment History
- 15 historical investments across 3 clients
- Designed to produce expected risk profile classifications

### Sample Data
- 4 simulations for testing GET endpoints
- 9 telemetry records for metrics validation

## Running Tests

### Via Taskfile (Recommended)

```bash
# Quick test against running Docker
task newman:local

# Full fresh environment test
task newman:fresh

# Test against specific environment
task newman:test

# CI/CD mode (fail fast)
task newman:ci

# Generate detailed report with browser preview
task newman:report
```

### Via Newman CLI

```bash
# Basic run
newman run Dynamic-Portfolio-API.postman_collection.json \
  -e environments/local-docker.postman_environment.json

# With HTML report
newman run Dynamic-Portfolio-API.postman_collection.json \
  -e environments/local-docker.postman_environment.json \
  --reporters cli,htmlextra \
  --reporter-htmlextra-export ../reports/newman-report.html

# CI/CD mode (fail on first error)
newman run Dynamic-Portfolio-API.postman_collection.json \
  -e environments/ci.postman_environment.json \
  --bail \
  --reporters cli,json \
  --reporter-json-export ../reports/newman-results.json
```

### Via Docker

```bash
# Run tests in Newman Docker container
docker run -t --network host \
  -v $(pwd):/etc/newman \
  postman/newman run Dynamic-Portfolio-API.postman_collection.json \
  -e environments/local-docker.postman_environment.json
```

## Seeding Test Data

### Manual Seeding

```bash
# Seed via Task
task seed-test-data

# Seed directly via Docker exec
docker compose exec -T sqlserver /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P YourStrong@Passw0rd -C \
  -i /scripts/setup-test-data.sql
```

### Automatic Seeding

The `task newman:local` and `task newman:fresh` commands automatically seed test data before running tests.

## CI/CD Integration

### GitHub Actions

The `.github/workflows/newman-tests.yml` workflow runs on:
- Push to `main` or `develop` branches
- Pull requests to `main` or `develop`
- Manual trigger via workflow dispatch

**Workflow Steps:**
1. Checkout code
2. Set up Java 21 and Node.js 20
3. Install Newman and reporters
4. Build application
5. Start Docker Compose stack
6. Verify services are healthy
7. Seed test data
8. Run Newman tests (fail fast mode)
9. Upload HTML and JSON reports as artifacts
10. Show logs on failure
11. Cleanup containers

**Artifacts:**
- `newman-report.html` - Visual test report (30-day retention)
- `newman-results.json` - Machine-readable results (30-day retention)

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

## Test Assertions

### Schema Validation Example

```javascript
pm.test("Response matches specification schema", () => {
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

### Calculation Validation Example

```javascript
pm.test("Calculation is correct", () => {
    const resultado = pm.response.json().resultadoSimulacao;
    const valorInicial = parseFloat(resultado.valorInicial);
    const valorFinal = parseFloat(resultado.valorFinal);
    const lucro = parseFloat(resultado.lucro);

    pm.expect(valorFinal).to.be.greaterThan(valorInicial);
    pm.expect(lucro).to.equal(valorFinal - valorInicial);
});
```

### JWT Token Management Example

```javascript
// Save token from login response
if (pm.response.code === 200) {
    const json = pm.response.json();
    pm.environment.set('jwtToken', json.token);
    console.log('JWT token saved to environment');
}
```

## Troubleshooting

### Tests Fail with Connection Error

```bash
# Ensure Docker is running
docker compose ps

# Check API health
curl http://localhost:8080/api/v1/actuator/health

# Restart services
task docker-down
task docker-up
```

### Test Data Not Found

```bash
# Re-seed test data
task seed-test-data

# Verify data exists
docker compose exec sqlserver /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P YourStrong@Passw0rd -C \
  -Q "SELECT COUNT(*) FROM portfoliodb.dbo.products"
```

### Authentication Fails

```bash
# Check if users exist in database
docker compose exec sqlserver /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P YourStrong@Passw0rd -C \
  -Q "SELECT * FROM portfoliodb.dbo.users"

# Verify JWT secret matches application config
docker compose exec api env | grep JWT_SECRET
```

### Tests Time Out

```bash
# Increase wait time in Taskfile.yml
# Change: sleep 10
# To:     sleep 20

# Or check if database migrations completed
task logs-flyway
```

## Reports

### HTML Report Features

The HTML report (`newman-report.html`) includes:
- ✅ Test summary with pass/fail counts
- ✅ Response time statistics
- ✅ Request/response details for each test
- ✅ Assertions with failure details
- ✅ Dark theme support
- ✅ Filterable by pass/fail status

### JSON Results Structure

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

## Best Practices

### 1. Always Use Fresh Data for Contract Tests
```bash
task newman:fresh  # Tears down, rebuilds, seeds, tests
```

### 2. Run Tests Before Deployment
```bash
# In CI/CD pipeline
task newman:ci
```

### 3. Generate Reports for Debugging
```bash
task newman:report  # Opens HTML report in browser
```

### 4. Test Against Multiple Environments
```bash
task newman:local   # Development
task newman:test    # Test environment
task newman:ci      # CI/CD environment
```

### 5. Monitor Performance Trends
```bash
# Check response times in reports
# Alert if average > 500ms
```

## Maintenance

### Adding New Tests

1. Open collection in Postman GUI
2. Add new request under appropriate folder
3. Add test scripts (JavaScript)
4. Export updated collection
5. Replace `Dynamic-Portfolio-API.postman_collection.json`

### Updating Test Data

1. Edit `scripts/setup-test-data.sql`
2. Run `task seed-test-data` to verify
3. Commit changes

### Updating Environments

1. Edit `environments/*.postman_environment.json`
2. Update baseUrl, credentials, or variables
3. Test with `task newman:local` or `task newman:test`

## Resources

- [Newman Documentation](https://learning.postman.com/docs/running-collections/using-newman-cli/command-line-integration-with-newman/)
- [Postman Collection Format](https://schema.postman.com/)
- [newman-reporter-htmlextra](https://github.com/DannyDainton/newman-reporter-htmlextra)
- [Challenge Specification](../local-docs/00-challenge-specification-original.md)

## Support

For issues with:
- **Test failures**: Check `reports/newman-report.html` for details
- **Environment issues**: Verify Docker containers are healthy
- **Data issues**: Re-run `task seed-test-data`
- **CI/CD issues**: Check GitHub Actions logs and artifacts
