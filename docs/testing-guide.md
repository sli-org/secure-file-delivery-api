# Testing Guide

## Test Strategy

The Statement API uses a three-tier testing strategy:

| Tier | Type | Purpose | Coverage Target |
|------|------|---------|-----------------|
| 1 | Unit Tests | Isolated component testing | 80%+ |
| 2 | Integration Tests | Full Spring context with H2 | Authentication/Authorization |
| 3 | E2E Tests | Playwright against deployed API | Smoke + CRUD |

## Running Tests

```bash
# Unit tests only
mvn test

# All tests including integration
mvn verify

# With coverage report
mvn test jacoco:report
# Report at: target/site/jacoco/index.html

# Specific test class
mvn test -Dtest=StatementServiceTest

# E2E tests (requires deployed API)
cd e2e-tests && npm install && npm run test:local
```

## Test Structure

```
src/test/java/za/co/api/statement/
├── test/
│   ├── base/
│   │   ├── BaseControllerTest.java      # Standalone MockMvc base
│   │   ├── BaseServiceTest.java         # Mockito-based service base
│   │   ├── BaseTransformerTest.java     # Transformer test base
│   │   └── BaseIntegrationTest.java     # Full context with TestRestTemplate
│   └── data/
│       ├── StatementTestFixtures.java   # Test data factories
│       └── JwtTestFixture.java          # JWT token helpers
├── controller/
│   ├── StatementControllerTest.java     # Happy path [SFD-300 to SFD-306]
│   └── StatementControllerNegativeTest.java  # Error paths [SFD-350 to SFD-353]
├── service/
│   ├── StatementServiceTest.java        # [SFD-100 to SFD-113]
│   ├── DownloadLinkServiceTest.java     # [SFD-130 to SFD-136]
│   └── StatementEventServiceTest.java   # [SFD-EV-001 to SFD-EV-011]
├── transformer/
│   ├── StatementTransformerTest.java    # [SFD-200 to SFD-208]
│   └── DownloadLinkTransformerTest.java # [SFD-220 to SFD-224]
├── dto/code/
│   ├── StatementTypeCodeTest.java
│   ├── StatementStatusCodeTest.java
│   ├── DownloadLinkStatusCodeTest.java
│   └── StatementEventTypeCodeTest.java
├── integration/
│   ├── StatementAuthenticationIntegrationTest.java  # [SFD-AI-001 to SFD-AI-005]
│   └── StatementAuthorizationIntegrationTest.java   # [SFD-AuI-001 to SFD-AuI-003]
└── config/
    └── IntegrationTestSecurityConfig.java
```

## Test ID Convention

All tests use `[SFD-###]` identifiers in `@DisplayName`:

| Range | Category |
|-------|----------|
| SFD-001 to SFD-038 | Enum tests |
| SFD-100 to SFD-113 | Statement service tests |
| SFD-130 to SFD-136 | Download link service tests |
| SFD-200 to SFD-224 | Transformer tests |
| SFD-300 to SFD-306 | Controller happy path |
| SFD-350 to SFD-353 | Controller negative tests |
| SFD-AI-001 to SFD-AI-005 | Authentication integration |
| SFD-AuI-001 to SFD-AuI-003 | Authorization integration |
| SFD-EV-001 to SFD-EV-011 | Event service tests |
| SFD-E2E-### | E2E tests |

## Base Test Classes

| Class | Purpose | Annotations |
|-------|---------|-------------|
| `BaseControllerTest` | Standalone MockMvc with GlobalExceptionHandler | `@ApiControllerTest`, `@ExtendWith(MockitoExtension)` |
| `BaseServiceTest` | Pure Mockito service tests | `@ApiServiceTest`, `@ExtendWith(MockitoExtension)` |
| `BaseTransformerTest` | Transformer unit tests | `@ApiTransformerTest` |
| `BaseIntegrationTest` | Full Spring Boot context, H2, TestRestTemplate | `@ApiIntegrationTest`, `@ActiveProfiles("test")` |

## Integration Test Configuration

Integration tests use:
- **H2 in-memory database** (create-drop)
- **Test security config** with synthetic JWT decoder
- **Mocked RabbitTemplate** (no real RabbitMQ needed)
- **Profile**: `test` (activates `application-test.yml`)
