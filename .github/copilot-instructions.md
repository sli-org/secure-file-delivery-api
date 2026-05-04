# GitHub Copilot Instructions for secure-file-delivery-api

## 🎯 Quick Start (Read This First!)

**Critical Rules:**
1. **Copy existing patterns** - See Architecture Patterns section below
2. **Extend Base Test Classes** - Use BaseControllerTest, BaseServiceTest, etc.
3. **Check test-cases-specification.md** - Before and after creating tests

---

## 🔴 Project Context

- **Domain**: Statement Management API
- **Framework**: Spring Boot 3.4.x with Java 21
- **Architecture**: REST API + RabbitMQ + OAuth2 + Monitoring
- **Package**: za.co.api.statement
- **Endpoint**: /api/v1/statements
- **Version**: 1.0.0-SNAPSHOT
- **Parent POM**: common-api-starter-parent 1.0.0-SNAPSHOT

---

## 🔴 Architecture Patterns

### Template Files (ALWAYS Copy These Patterns)

| Component | File to Copy |
|-----------|--------------|
| Controllers | `StatementController.java` |
| Services | `StatementService.java` |
| DTOs | `StatementDTO.java` (extend BaseDTO) |
| Enums | `StatementTypeCode.java` (implement BaseEnum<String>) |
| Transformers | `StatementTransformer.java` |
| Events | `StatementEventService.java` |
| Unit Tests | `StatementControllerTest.java`, `StatementServiceTest.java` |
| Integration Tests | `StatementControllerIntegrationTest.java` |

### Dependencies (Parent POM Provided)

From `common-starter-common`:
- IdResponseDTO - for POST responses
- PaginatedListDTO<T> - for paginated lists

From `common-starter-exception`:
- GlobalExceptionHandler - for error handling
- ErrorResponse - for error responses
- ValidationError - for validation errors
- ServiceException, ValidationException, ResourceNotFoundException

---

## 🔴 Critical Patterns (Must Maintain)

### DTOs
- Extend `BaseDTO`
- Use plain `String` for ID field
- Validation groups: `OnCreate.class`, `OnUpdate.class`
- `@Schema` annotations for OpenAPI
- Use `Optional<T>` for optional fields

### Enums
- Implement `BaseEnum<String>`
- Include `fromValue()` static method
- Support external code mapping
- Use `@JsonValue` for serialization

### Controllers
- Thin controllers, delegate to service
- `@PreAuthorize` with roles and scopes
- Comprehensive `@Operation` annotations
- Accept `Authentication` parameter for audit

### Services
- Concrete `@Service` classes (NO interface pattern)
- Use `ExceptionUtil` for throwing errors
- Validate before save
- Publish events via `EventService`
- Set audit fields (createdBy, createdAt, etc.)

### Communication
- RabbitMQ event publishing via `CommunicationService`
- NO direct SMTP/JavaMailSender - all email/SMS via RabbitMQ
- Centralized via common-comms-api

### Transformers
- Null-safe transformations
- `Optional.orElse(null)` for optional fields
- Never update audit fields in transformer

### Tests
- `@DisplayName` with `[SFD-###]` format
- Arrange-Act-Assert pattern
- Extend appropriate base test class
- 80%+ coverage target

---

## 🔴 Testing Standards

### Base Test Classes (MUST Use)

| Class | Purpose |
|-------|---------|
| `BaseControllerTest` | Controller unit tests |
| `BaseServiceTest` | Service unit tests |
| `BaseTransformerTest` | Transformer tests |
| `BaseIntegrationTest` | Full-stack integration tests |
| `BaseDTOTest` | DTO validation tests |

### Test Organization

| Suffix | Purpose | Response Codes |
|--------|---------|----------------|
| `*Test.java` | Happy path | 2xx responses |
| `*NegativeTest.java` | Errors and exceptions | 4xx/5xx |
| `*IntegrationTest.java` | Full-stack testing | All |

### Before Creating Tests

1. Check `docs/test-cases-specification.md` for duplicates
2. Identify next available Test ID for `[SFD-###]`
3. Choose appropriate base class

### After Creating Tests

Update `docs/test-cases-specification.md` immediately

---

## 🔴 Data Scoping (If Enabled)

Data scoping is NOT enabled for this API.

---

## 🔴 External Integrations

### Azure Blob Storage API
- **Auth Type:** API_KEY
- **Config prefix:** `external.azure-blob-storage`
- **Properties:** `base-url`, `connection-string` (Jasypt encrypted)

See `application*.yml` for external API properties.

---

## 🔴 Validation Rules

### Must Follow ✅
- Return `ResponseEntity<PaginatedListDTO<T>>` for lists
- Return `ResponseEntity<T>` for single items
- Return `ResponseEntity<IdResponseDTO>` for creation
- Return `ResponseEntity<Void>` for update/delete
- Use `ValidationError` lists for input validation
- Publish events for state changes
- Include `GlobalExceptionHandler` in tests

### Must Avoid ❌
- Don't return entities directly - only DTOs
- Don't create new patterns - copy existing
- Don't skip validation in service layer
- Don't ignore exception handling
- Don't create interface/impl pattern for services - use concrete classes
- Don't use JavaMailSender directly - use CommunicationService for RabbitMQ

---

## 🔴 Coding Standards (CRITICAL)

### Null Type Safety
- **ALWAYS use null-safe operations** - Check for null before accessing objects
- Add `if (x == null) { return null; }` guards at method entry where applicable
- Use `Optional<T>` for return types that may be absent
- Use `Objects.requireNonNull()` for parameters that must not be null
- Avoid raw null returns - use `Optional.empty()` or `Optional.ofNullable()`

### No Deprecated Code
- **NEVER use deprecated classes, methods, or annotations**
- Check for deprecation warnings and use recommended replacements
- Examples of deprecated patterns to avoid:
  - `new Date()` → use `java.time.Instant`, `LocalDateTime`, etc.
  - `StringBuffer` → use `StringBuilder`
