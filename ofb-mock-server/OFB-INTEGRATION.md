# OFB Mock Server - Open Finance Brasil Integration

## Overview

This mock server provides 100% conformant implementations of Open Finance Brasil (OFB) investment APIs by auto-generating Java models directly from the official OpenAPI specifications maintained by the Brazilian Central Bank.

## Architecture

### Build-Time Spec Download

OpenAPI specifications are **NOT** committed to the repository. Instead, they are downloaded directly from the official GitHub repository at build time:

```
Source: https://github.com/OpenBanking-Brasil/openapi
```

**Maven Build Process:**

1. **Initialize Phase** - `download-maven-plugin` fetches latest specs from GitHub
   - Downloads to `target/ofb-specs/` (git-ignored)
   - Ensures specs are always from official source
   - No licensing concerns

2. **Generate Sources Phase** - `openapi-generator-maven-plugin` generates Java models
   - Reads specs from `target/ofb-specs/`
   - Generates 206 model classes
   - Output: `target/generated-sources/openapi/`

## Supported Investment Types

| Investment Type | OFB Spec Version | Package |
|----------------|------------------|---------|
| Bank Fixed Incomes (CDB, RDB, LCI, LCA) | 1.0.4 | `com.ofb.api.model.bankfixedincome` |
| Investment Funds | 1.0.2 | `com.ofb.api.model.fund` |
| Treasury Titles (Tesouro Direto) | 1.0.2 | `com.ofb.api.model.treasuretitle` |
| Variable Incomes (Stocks, ETFs, BDRs) | 1.2.1 | `com.ofb.api.model.variableincome` |
| Credit Fixed Incomes (Debentures, CRIs, CRAs) | 1.0.3 | `com.ofb.api.model.creditfixedincome` |

## OFB Taxonomy Mapping

### Bank Fixed Incomes

```java
com.ofb.api.model.bankfixedincome.EnumInvestmentType:
- CDB (Certificado de Depósito Bancário)
- RDB (Recibo de Depósito Bancário)
- LCI (Letra de Crédito Imobiliário)
- LCA (Letra de Crédito do Agronegócio)
```

### Investment Funds

```java
com.ofb.api.model.fund.AnbimaCategoryEnum:
- RENDA_FIXA (Fixed Income)
- ACOES (Stocks)
- MULTIMERCADO (Multi-Market)
- CAMBIAL (Foreign Exchange)
```

## Building the Mock Server

### Requirements

- Java 21+ (required by OpenAPI Generator 7.2.0)
- Maven 3.8+
- Internet connection (to download specs from GitHub)

### Build Command

```bash
mvn clean compile
```

This will:
1. Download all 5 OFB OpenAPI specifications from GitHub
2. Generate 206 Java model classes
3. Compile the mock server

### Generated Models Location

```
target/
├── ofb-specs/                    # Downloaded OpenAPI specs (git-ignored)
│   ├── bank-fixed-incomes-1.0.4.yml
│   ├── funds-1.0.2.yml
│   ├── treasure-titles-1.0.2.yml
│   ├── variable-incomes-1.2.1.yml
│   └── credit-fixed-incomes-1.0.3.yml
└── generated-sources/
    └── openapi/                  # Generated Java models
        └── src/main/java/com/ofb/api/model/
            ├── bankfixedincome/
            ├── fund/
            ├── treasuretitle/
            ├── variableincome/
            └── creditfixedincome/
```

## Key Benefits

1. **100% Conformance** - Models generated directly from official OFB specs
2. **No License Issues** - Specs downloaded at build time, never committed
3. **Always Up-to-Date** - Easy to update by changing version numbers in pom.xml
4. **Type-Safe** - Compile-time validation ensures API contract adherence
5. **Zero Maintenance** - No manual POJO updates needed

## Updating OFB Spec Versions

To use newer versions of OFB specs, update the URLs in `pom.xml`:

```xml
<url>https://raw.githubusercontent.com/OpenBanking-Brasil/openapi/main/swagger-apis/bank-fixed-incomes/1.0.5.yml</url>
```

Then rebuild:

```bash
mvn clean compile
```

## Testing

Run tests with:

```bash
mvn test
```

## Official References

- **OFB GitHub Repository**: https://github.com/OpenBanking-Brasil/openapi
- **Brazilian Central Bank**: https://www.bcb.gov.br/estabilidadefinanceira/openbanking
- **OpenAPI Generator**: https://openapi-generator.tech/
