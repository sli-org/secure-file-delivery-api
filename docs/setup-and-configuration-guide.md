# Setup and Configuration Guide

## Prerequisites

- Java 21 (OpenJDK or Oracle JDK)
- Maven 3.9+
- Access to the Maven repository (for parent POM and starters)
- RabbitMQ instance
- Database (Oracle/PostgreSQL)
- Azure Blob Storage account (or Azurite for local)

## Local Development Setup

### 1. Clone Repository

```bash
git clone <repo-url>
cd statement-api
```

### 2. Configure Local Environment

Copy the environment template and customize:
```bash
cp docs/application-env-template.yml src/main/resources/application-local.yml
```

Key settings to update:
- Database connection (`spring.datasource.*`)
- RabbitMQ connection (`spring.rabbitmq.*`)
- Azure Blob Storage URL (`external.azure-blob-storage.base-url`)
- HMAC secret (`download.hmac-secret`)
- Jasypt password (`jasypt.encryptor.password`)

### 3. Build

```bash
mvn clean compile
```

### 4. Run Tests

```bash
mvn test
```

### 5. Start Application

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## Configuration Properties

### Application Properties

| Property | Description | Default |
|----------|-------------|---------|
| `server.port` | HTTP port | 8093 |
| `spring.datasource.url` | Database JDBC URL | — |
| `spring.rabbitmq.host` | RabbitMQ host | localhost |
| `spring.rabbitmq.port` | RabbitMQ port | 5672 |
| `external.azure-blob-storage.base-url` | Blob storage URL | — |
| `external.azure-blob-storage.connection-string` | Jasypt-encrypted connection string | — |
| `download.hmac-secret` | HMAC signing key for download tokens | — |
| `download.base-url` | Base URL for download links | — |
| `the.events.exchange` | RabbitMQ exchange name | domain.events |

### Security Properties

| Property | Description |
|----------|-------------|
| `spring.security.oauth2.resourceserver.jwt.issuer-uri` | Azure AD issuer |
| `spring.security.oauth2.resourceserver.jwt.audiences` | Allowed audiences |

### Environment Profiles

| Profile | Purpose |
|---------|---------|
| `local` | Local development |
| `dev` | Development server |
| `test` | Test environment |
| `train` | Training environment |
| `prod` | Production |

## Secrets Management

Sensitive values are stored in HashiCorp Vault under `vault/data/statement-api`:

- Database credentials
- RabbitMQ password
- Azure Blob Storage connection string
- HMAC secret
- Jasypt encryptor password

Never hardcode secrets in application YAML files.
