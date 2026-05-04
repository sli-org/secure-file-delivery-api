# Secure File Statement Delivery API

Stores customer account statements as PDF files in cloud blob storage, manages statement metadata, generates secure time-limited download links with HMAC-signed tokens, and provides both authenticated management endpoints and token-based public download endpoints.

## Overview

The Statement API is a Spring Boot RESTful service that provides:

- RESTful API design with proper HTTP methods and status codes
- Comprehensive validation and error handling
- OpenAPI/Swagger documentation
- JWT-based authentication and authorization (OAuth2)
- Event-driven architecture with RabbitMQ
- Database persistence with JPA
- External API integration with Azure Blob Storage

## Architecture

```
┌─────────────────────────────────────┐
│   Controller Layer (REST API)       │  ← HTTP endpoints, validation
├─────────────────────────────────────┤
│   Service Layer (Business Logic)    │  ← Core business logic
├─────────────────────────────────────┤
│   Transformer Layer (Data Mapping)  │  ← DTO ↔ Entity mapping
├─────────────────────────────────────┤
│   Repository Layer (Persistence)    │  ← Database access
│   External Integration Layer        │  ← Azure Blob Storage
└─────────────────────────────────────┘
```

## Getting Started

### Prerequisites

| Tool | Version | Required For |
|------|---------|-------------|
| Java JDK | 21+ | Build and run |
| Maven | 3.9+ | Build tool |
| Docker Desktop | Latest | Full-stack deployment (optional) |

### Zero-Config Local Development

After cloning, the service runs immediately with **no configuration needed**:

```powershell
git clone <your-repo-url>
cd secure-file-delivery-api

# Run all tests (H2 in-memory, Flyway auto-creates schema)
mvn clean verify

# Start the service locally
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**What happens automatically:**
- **Flyway** creates the database schema from `src/main/resources/db/migration/V1__*.sql`
- **H2 in-memory database** — no database installation needed
- **H2 Console** available at http://localhost:8093/h2-console (JDBC URL: `jdbc:h2:mem:statementdb`)
- **Swagger UI** available at http://localhost:8093/swagger-ui/index.html
- **Health Check** at http://localhost:8093/actuator/health

> **Note:** RabbitMQ and Azure Blob Storage calls will fail without those services running.
> For unit/integration tests, these are excluded via auto-configuration — tests run fully offline.

### Database Schema (Flyway Managed)

Schema migrations live in `src/main/resources/db/migration/`:

| Migration | Description |
|-----------|-------------|
| `V1__add_version_column.sql` | Creates STATEMENT and DOWNLOAD_LINK tables with indexes |

Flyway runs automatically on startup. For local/test profiles, H2 in-memory is used (fresh schema each restart).

### Full-Stack with Docker Compose

For a complete environment with PostgreSQL, RabbitMQ, and Azurite:

```powershell
.\deploy.ps1
```

That's it. The script will:
1. Generate `.env` from `.env.example` (pre-configured defaults, zero editing)
2. Build the Docker image
3. Start PostgreSQL + RabbitMQ + Azurite + API
4. Wait for the health check and print access URLs

**Other commands:**
```powershell
.\deploy.ps1 -Down           # Tear down everything
.\deploy.ps1 -Target k8s     # Build image + deploy to Kubernetes
.\deploy.ps1 -Target all     # Docker Compose + Kubernetes
```

### Testing with Postman

Import from the `postman/` folder:
- `Statement-API.postman_collection.json` — 10 requests with test assertions
- `Statement-API.postman_environment.json` — pre-configured environment variables

Set `bearer_token` to a valid JWT for authenticated requests.

### Access Points

   - API Base: http://localhost:8093/api/v1/statements
   - Swagger UI: http://localhost:8093/swagger-ui/index.html
   - Health Check: http://localhost:8093/actuator/health

### Running Tests

**Unit and Integration Tests**:
```bash
mvn test
mvn verify
mvn test jacoco:report
```

**E2E Tests** (after deployment):
```bash
cd e2e-tests
npm install
npm test
```

## Documentation

| Document | Description |
|----------|-------------|
| [API Specification](docs/api-specification.md) | Detailed API endpoints, request/response formats |
| [API Usage Guide](docs/api-usage-guide.md) | Common usage patterns and examples |
| [Architecture](docs/architecture.md) | System design and component interactions |
| [Setup & Configuration](docs/setup-and-configuration-guide.md) | Environment setup and configuration |
| [Security Guide](docs/security-guide.md) | Authentication and authorization details |
| [Testing Guide](docs/testing-guide.md) | Testing strategies and how to run tests |
| [Test Cases](docs/test-cases-specification.md) | Detailed test case specifications |
| [Production Readiness](docs/production-readiness-checklist.md) | Deployment checklist |
| [Quick Reference](docs/quick-reference.md) | Common commands and tips |
| [Environment Template](docs/application-env-template.yml) | Configuration template |

## API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/api/v1/statements` | Upload statement (multipart) | JWT |
| `GET` | `/api/v1/statements/{id}` | Get statement by ID | JWT |
| `GET` | `/api/v1/statements` | List statements (paginated) | JWT |
| `POST` | `/api/v1/statements/{id}/download-links` | Generate download link | JWT |
| `GET` | `/api/v1/statements/download/{token}` | Download via token | Public |
| `DELETE` | `/api/v1/statements/{id}` | Soft delete statement | JWT |

## Testing

| Test Type | Location | Run Command |
|-----------|----------|-------------|
| Unit Tests | `src/test/java/**/controller/`, `**/service/`, `**/transformer/` | `mvn test` |
| Integration Tests | `src/test/java/**/integration/` | `mvn verify` |
| E2E Tests | `e2e-tests/` | `npm test` (in e2e-tests folder) |

**Coverage Target**: 80%+

## Security

This API uses JWT-based authentication with OAuth2 (Azure AD). Key features:

- JWT token validation
- Role-based access control (scoped permissions)
- Token-based public download endpoint (HMAC-signed)
- Input validation and sanitization

See [Security Guide](docs/security-guide.md) for details.

## Events

| Event | Exchange | Routing Key |
|-------|----------|-------------|
| Statement Uploaded | `domain.events` | `statement.uploaded` |
| Download Link Created | `domain.events` | `download-link.created` |
| Statement Deleted | `domain.events` | `statement.deleted` |

## Monitoring

- **Health Check**: `GET /actuator/health`
- **Info**: `GET /actuator/info`
- **Metrics**: `GET /actuator/metrics`

---

**Generated by API Generator Kit** | See [GENERATION-REPORT.md](GENERATION-REPORT.md) for generation details
