# Test Cases Specification

## Enum Tests

| ID | Test Case | Expected |
|----|-----------|----------|
| SFD-001 to SFD-010 | StatementTypeCode values, fromValue(), null handling | Pass |
| SFD-011 to SFD-020 | StatementStatusCode values, fromValue(), null handling | Pass |
| SFD-021 to SFD-030 | DownloadLinkStatusCode values, fromValue(), null handling | Pass |
| SFD-031 to SFD-038 | StatementEventTypeCode values, fromValue(), null handling | Pass |

## Statement Service Tests

| ID | Test Case | Expected |
|----|-----------|----------|
| SFD-100 | Upload statement with valid data | Returns StatementDTO |
| SFD-101 | Find statement by ID | Returns StatementDTO |
| SFD-102 | Find statement - not found | Throws ResourceNotFoundException |
| SFD-103 | Find all statements with filters | Returns PaginatedListDTO |
| SFD-104 | Delete statement (soft delete) | Sets status to DELETED |
| SFD-105 | Upload statement publishes event | Event published to RabbitMQ |
| SFD-106 | Delete statement revokes download links | All active links revoked |
| SFD-107 | Download from blob storage | Returns byte array |
| SFD-108 to SFD-113 | Various edge cases and validations | As specified |
| SFD-114 | Find with invalid UUID format | Throws ValidationException |
| SFD-115 | Delete with invalid UUID format | Throws ValidationException |
| SFD-116 | Upload with non-PDF content (magic bytes) | Throws ValidationException |
| SFD-117 | Upload with path traversal in customerId | Sanitizes blob path (no ../) |
| SFD-118 | Upload with customerId > 50 chars | Throws ValidationException |
| SFD-119 | Upload with accountNumber > 20 chars | Throws ValidationException |
| SFD-120 | Upload with path traversal in filename | Sanitizes stored filename |

## Download Link Service Tests

| ID | Test Case | Expected |
|----|-----------|----------|
| SFD-130 | Generate download link | Returns DownloadLinkDTO with token |
| SFD-131 | Validate valid download token | Returns StatementEntity |
| SFD-132 | Validate expired token | Throws exception |
| SFD-133 | Validate used token (max downloads reached) | Throws exception |
| SFD-134 | Record download (increment count, set IP) | Updates entity |
| SFD-135 | Generate link for deleted statement | Throws exception |
| SFD-136 | Generate link for expired statement | Throws exception |
| SFD-137 | Generate link with invalid UUID format | Throws ValidationException |

## Transformer Tests

| ID | Test Case | Expected |
|----|-----------|----------|
| SFD-200 | Transform entity to DTO | Correct field mapping |
| SFD-201 | Transform DTO to entity | Correct field mapping |
| SFD-202 | Account number masking | Only last 4 digits shown |
| SFD-203 to SFD-208 | Null handling, edge cases | Null-safe |
| SFD-220 to SFD-224 | DownloadLink transformer tests | Correct mapping |

## Controller Tests

| ID | Test Case | Expected |
|----|-----------|----------|
| SFD-301 | GET /{id} returns 200 with DTO | 200 OK |
| SFD-302 | GET / returns 200 with paginated list | 200 OK |
| SFD-303 | POST / (multipart) returns 201 | 201 Created |
| SFD-304 | POST /{id}/download-links returns 201 | 201 Created |
| SFD-305 | GET /download/{token} returns 200 with PDF | 200 OK |
| SFD-306 | DELETE /{id} returns 204 | 204 No Content |
| SFD-351 | GET /{id} not found returns 404 | 404 Not Found |
| SFD-352 | DELETE /{id} not found returns 404 | 404 Not Found |
| SFD-353 | GET /download/{token} invalid returns 404 | 404 Not Found |

## Integration Tests - Authentication

| ID | Test Case | Expected |
|----|-----------|----------|
| SFD-AI-001 | Request with valid JWT | 200 OK |
| SFD-AI-002 | Request without JWT | 401 Unauthorized |
| SFD-AI-003 | Request with invalid JWT | 401 Unauthorized |
| SFD-AI-004 | Download endpoint without JWT | 200/404 (permitAll) |
| SFD-AI-005 | Request with expired JWT | 401 Unauthorized |

## Integration Tests - Authorization

| ID | Test Case | Expected |
|----|-----------|----------|
| SFD-AuI-001 | Admin role has full access | 200 OK |
| SFD-AuI-002 | Read-only scope can GET but not DELETE | 403 Forbidden on DELETE |
| SFD-AuI-003 | No scopes rejected | 403 Forbidden |

## E2E Tests

| ID | Test Case | Expected |
|----|-----------|----------|
| SFD-E2E-001 | Upload statement | 201 Created |
| SFD-E2E-002 | Reject unauthenticated upload | 401 |
| SFD-E2E-010 | Get statement by ID | 200 OK |
| SFD-E2E-011 | Get non-existent statement | 404 |
| SFD-E2E-020 | List statements with pagination | 200 OK |
| SFD-E2E-030 | Generate download link | 201 Created |
| SFD-E2E-040 | Download via token | 200 OK |
| SFD-E2E-050 | Soft delete statement | 204 |
| SFD-E2E-SMOKE-001 | Health check | 200 |
| SFD-E2E-SMOKE-002 | API base URL responds | 200/401 |
