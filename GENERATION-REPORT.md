# Generation Report — Statement API (statement-api)

**Generated:** 2025-01-31
**API Definition:** kit/spec/api-definition.md
**Parent POM:** common-api-starter-parent 1.0.0-SNAPSHOT
**Framework:** Spring Boot 3.4.x / Java 21

---

## Summary

| Phase | Status | Files | Notes |
|-------|--------|-------|-------|
| Phase 1 — Scaffold | ✅ Complete | ~20 | pom.xml, configs, YAML, static files |
| Phase 2 — Develop | ✅ Complete | ~18 | Entities, DTOs, services, controller, transformers, events |
| Phase 3a — Unit Tests | ✅ Complete | ~15 | All passing (80%+ coverage target) |
| Phase 3b — Integration Tests | ✅ Complete | 4 | 8 tests, all passing |
| Phase 3c — E2E Framework | ✅ Complete | 20 | Playwright + TypeScript |
| Phase 4 — Documentation | ✅ Complete | 12 | Full documentation suite |

## Entities

| Entity | Fields | Operations |
|--------|--------|------------|
| Statement | 16 | Upload, Get, List, Delete |
| DownloadLink | 12 | Generate Link, Download (public) |

## Feature Flags

| Feature | Enabled |
|---------|---------|
| Database (JPA) | ✅ |
| Security (OAuth2 JWT) | ✅ |
| Events (RabbitMQ) | ✅ |
| Integrations (Azure Blob Storage) | ✅ |
| Data Scoping | ❌ |

## Decisions

- Parsed two entities: Statement (primary, 16 fields, 6 operations) and DownloadLink (child, 12 fields)
- Feature flags: DB=true, Security=true, Events=true, Integrations=true
- Created separate DownloadLinkService for HMAC token logic (not part of StatementService)
- Download endpoint uses `permitAll` — HMAC token provides authorization

## Deviations from Kit Templates

| Deviation | Reason |
|-----------|--------|
| Used `new ServiceException(msg, code)` | `ExceptionUtil.createServiceError()` does not exist in common-starter-exception |
| Used `getClientId(auth).orElse("system")` | `ClaimsService.getUserId()` does not exist in common-starter-security |
| Added `@Profile("!test")` on SecurityConfig | Prevents dual SecurityFilterChain in integration tests |
| Created DownloadLinkService as separate class | HMAC token generation/validation logic warranted separation |

## Problems Encountered and Resolved

| Phase | Problem | Resolution |
|-------|---------|------------|
| 2 | `ExceptionUtil.createServiceError` doesn't exist | Used `new ServiceException(msg, code)` |
| 2 | `ClaimsService.getUserId` doesn't exist | Used `getClientId(auth).orElse("system")` |
| 3a | Controller tests return 500 — `.principal(() -> "test-user")` not resolving as Authentication | Used `@Mock Authentication mockAuthentication` with `.principal(mockAuthentication)` |
| 3a | Unnecessary stubbing in delete negative test | Removed unused `when(statementService.find(...))` stub |
| 3b | `Could not resolve placeholder 'local.server.port'` | Used static URL in test YAML |
| 3b | Dual SecurityFilterChain (production + test) | Added `@Profile("!test")` on SecurityConfig |
| 3b | AMQPProperties `@NotBlank virtualHost` | Added `virtual-host: test` to test YAML |
| 3b | SentryProperties `@NotBlank dsn` | Set dummy DSN `https://test@sentry.io/0` |
| 3b | Test JwtDecoder accepts invalid tokens | Added `token.contains("invalid")` rejection |

## Test Coverage

| Category | Test Count | ID Range |
|----------|-----------|----------|
| Enum tests | ~38 | SFD-001 to SFD-038 |
| Service tests | 14 | SFD-100 to SFD-113 |
| Download link service | 7 | SFD-130 to SFD-136 |
| Event service | 11 | SFD-EV-001 to SFD-EV-011 |
| Transformer tests | 14 | SFD-200 to SFD-224 |
| Controller tests | 10 | SFD-300 to SFD-353 |
| Integration tests | 8 | SFD-AI/AuI |
| **Total** | **~102** | |

## Warnings

- `download.hmac-secret` and `download.base-url` are custom properties added to YAML and spring-configuration-metadata.json
- E2E tests require a deployed API instance — not run as part of Maven build
