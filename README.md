# Dynamic Portfolio API

Investment simulation system for Brazilian financial products including CDBs, LCIs, LCAs, Treasury Direct, and investment funds.

## Features

- Investment simulation with product validation
- Dynamic risk profiling based on client behavior
- Product recommendations by risk profile
- Investment history tracking
- Telemetry and performance metrics
- JWT authentication
- RESTful API with OpenAPI documentation

## Technology Stack

- Java 21
- Spring Boot 3.4.11
- SQL Server 2022
- Docker & Docker Compose
- Flyway for database migrations
- JWT for authentication
- Swagger/OpenAPI for API documentation

## Prerequisites

### For Docker (Recommended)
- Docker and Docker Compose installed

### For Local Development
- Java 21
- Maven 3.9+
- **Recommended**: Use [SDKMAN](https://sdkman.io/) for easy setup

#### Quick Setup with SDKMAN

Install SDKMAN:
```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
```

Install Java and Maven (project includes `.sdkmanrc`):
```bash
sdk env install
```

Or install manually:
```bash
sdk install java 21.0.8-amzn
sdk install maven 3.9.9
```

## Quick Start with Docker

1. Clone the repository
2. Navigate to project directory
3. Start the application:

```bash
docker compose up -d
```

The API will be available at `http://localhost:8080`

**What happens during startup:**
1. SQL Server container starts with health checks
2. Database initialization container creates `portfoliodb` database
3. Flyway migration container runs all database migrations
4. API container starts after migrations complete successfully

The database is automatically created, migrated, and seeded with sample data.

## Local Development

### Database Setup

Start SQL Server and run migrations:

```bash
# Start SQL Server
docker compose up sqlserver -d

# Wait for SQL Server to be healthy, then run init and migrations
docker compose up sqlserver-init
docker compose up flyway
```

Or use the database from the full Docker Compose stack:
```bash
docker compose up sqlserver sqlserver-init flyway -d
```

### Run Application Locally

With SDKMAN (automatically uses correct versions):
```bash
sdk env
mvn spring-boot:run
```

Without SDKMAN:
```bash
mvn spring-boot:run
```

The API will connect to the SQL Server container on `localhost:1433`.

## API Endpoints

### Authentication

- `POST /auth/login` - Generate JWT token

### Investment Operations

- `POST /simular-investimento` - Simulate investment
- `GET /perfil-risco/{clienteId}` - Get client risk profile
- `GET /produtos-recomendados/{perfil}` - Get recommended products by profile
- `GET /investimentos/{clienteId}` - Get client investment history

### Metrics and Reports

- `GET /simulacoes` - Get all simulations
- `GET /simulacoes/por-produto-dia` - Get daily aggregations by product
- `GET /telemetria` - Get service telemetry metrics

## API Documentation

Once the application is running, access the Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI specification available at:

```
http://localhost:8080/api-docs
```

## Authentication

All endpoints except `/auth/login` require JWT authentication.

### Getting a Token

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo"}'
```

### Using the Token

```bash
curl -X GET http://localhost:8080/perfil-risco/123 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Example Requests

### Simulate Investment

```bash
curl -X POST http://localhost:8080/simular-investimento \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "clienteId": 123,
    "valor": 10000.00,
    "prazoMeses": 12,
    "tipoProduto": "CDB"
  }'
```

### Get Risk Profile

```bash
curl -X GET http://localhost:8080/perfil-risco/123 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Recommended Products

```bash
curl -X GET http://localhost:8080/produtos-recomendados/Moderado \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Risk Profiles

The system classifies clients into three risk profiles:

- **Conservador**: Low risk tolerance, focused on liquidity
- **Moderado**: Balanced risk tolerance
- **Agressivo**: High risk tolerance, focused on returns

Classification is based on:
- Total investment volume
- Transaction frequency
- Investment product preferences

## Database Schema

The application uses Flyway for database migrations. Schema is automatically created on startup via a dedicated Flyway container.

**Migration Process:**
1. `V1__create_products_table.sql` - Products table
2. `V2__create_simulations_table.sql` - Simulations table
3. `V3__create_investments_table.sql` - Investments table
4. `V4__create_telemetry_table.sql` - Telemetry table
5. `V5__seed_sample_products.sql` - Sample product data

Main tables:
- `produtos` - Investment products (CDB, LCI, LCA, Tesouro Direto, Fundos)
- `simulacoes` - Simulation history
- `investimentos` - Client investment history
- `telemetria` - Performance metrics

All migrations are located in `src/main/resources/db/migration/`.

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| DB_HOST | Database host | localhost |
| DB_PORT | Database port | 1433 |
| DB_NAME | Database name | portfoliodb |
| DB_USER | Database user | sa |
| DB_PASSWORD | Database password | YourStrong@Passw0rd |
| JWT_SECRET | JWT signing key | (base64 encoded default) |

## Health Check

```bash
curl http://localhost:8080/actuator/health
```

## Stopping the Application

```bash
docker compose down
```

To remove volumes:

```bash
docker compose down -v
```

## Testing

The project includes comprehensive test coverage:

### Run All Tests

```bash
mvn test
```

### Test Coverage

- **Unit Tests**: Service layer, utilities, and business logic
- **Integration Tests**: API endpoints with Spring Boot Test
- **Test Framework**: JUnit 5 + Mockito
- **Coverage**: Critical business logic including:
  - Investment calculations
  - Risk profile algorithm
  - Product validation
  - API endpoints with authentication

### Test Structure

```
src/test/java/
├── integration/    - Full API integration tests
├── service/        - Service layer unit tests
└── util/           - Utility class unit tests
```

## Building from Source

### Build with Tests

```bash
mvn clean package
```

The JAR file will be created in `target/dynamic-portfolio-api-1.0.0.jar`

### Build without Tests

```bash
mvn clean package -DskipTests
```

### Run Built JAR

```bash
java -jar target/dynamic-portfolio-api-1.0.0.jar
```

**Note**: Ensure database is running and environment variables are set before running the JAR.

## Docker Architecture

The Docker Compose setup uses a multi-stage initialization process:

```
┌─────────────────┐
│  SQL Server     │ ◄── Health checks ensure ready state
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ sqlserver-init  │ ◄── Creates portfoliodb database
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│    Flyway       │ ◄── Runs all migrations (V1-V5)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│      API        │ ◄── Starts after migrations complete
└─────────────────┘
```

**Containers:**
- `portfolio-sqlserver` - SQL Server 2022 (persistent)
- `sqlserver-init` - Database creation (runs once)
- `flyway` - Migration runner (runs once)
- `portfolio-api` - Spring Boot application (persistent)

## Troubleshooting

### Docker Issues

**Containers not starting:**
```bash
# Check container logs
docker compose logs

# Check specific service
docker compose logs sqlserver
docker compose logs flyway
docker compose logs api
```

**Database connection issues:**
```bash
# Verify SQL Server is healthy
docker compose ps

# Should show "healthy" status for portfolio-sqlserver
```

**Flyway migration failures:**
```bash
# Check Flyway logs
docker compose logs flyway

# Reset database and retry
docker compose down -v
docker compose up -d
```

### Local Development Issues

**Maven build fails:**
```bash
# Ensure correct Java version
java -version  # Should be 21

# With SDKMAN
sdk env
mvn clean install
```

**Port already in use:**
```bash
# Check what's using port 8080
lsof -i :8080

# Or use different port
SERVER_PORT=8081 mvn spring-boot:run
```

## License

This project is for educational and demonstration purposes.
