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
- Spring Boot 3.2.0
- SQL Server 2022
- Docker & Docker Compose
- Flyway for database migrations
- JWT for authentication
- Swagger/OpenAPI for API documentation

## Prerequisites

- Docker and Docker Compose installed
- Java 21 (for local development)
- Maven 3.9+ (for local development)

## Quick Start with Docker

1. Clone the repository
2. Navigate to project directory
3. Start the application:

```bash
docker-compose up -d
```

The API will be available at `http://localhost:8080`

The database will be automatically created and seeded with sample data.

## Local Development

### Database Setup

Start SQL Server container:

```bash
docker-compose up sqlserver -d
```

### Run Application

```bash
mvn spring-boot:run
```

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

The application uses Flyway for database migrations. Schema is automatically created on startup.

Main tables:
- `produtos` - Investment products
- `simulacoes` - Simulation history
- `investimentos` - Client investment history
- `telemetria` - Performance metrics

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
docker-compose down
```

To remove volumes:

```bash
docker-compose down -v
```

## Building from Source

```bash
mvn clean package
```

The JAR file will be created in `target/` directory.

## License

This project is for educational and demonstration purposes.
