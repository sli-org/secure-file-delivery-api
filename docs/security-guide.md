# Statement API Security Guide

## Authentication

### OAuth2 JWT (Azure AD)

All management endpoints require a valid JWT from Azure AD.

**Issuer:** `https://login.microsoftonline.com/95d6a08d-8d38-495b-9c70-ab0cacd42d64/v2.0`

**Audience (non-prod):** `eb596b75-d742-4786-8325-2a55591c69d9`
**Audience (prod):** `58b42ace-949f-42ad-9c7c-d24e563c3c68`

### Token-Based Download (HMAC)

The download endpoint (`/api/v1/statements/download/{token}`) uses HMAC-signed tokens instead of JWT. This allows secure, time-limited sharing of download links without requiring the recipient to have OAuth credentials.

**Token Format:** `Base64URL({statementId}:{expiresEpochSec}:{hmacSignature})`

## Authorization

### Roles

| Role | Access Level |
|------|--------------|
| `api.admin` | Global admin — full access |

### Scopes

| Scope | Endpoints |
|-------|-----------|
| `statement:create` | POST /statements, POST /statements/{id}/download-links |
| `statement:read` | GET /statements, GET /statements/{id} |
| `statement:delete` | DELETE /statements/{id} |

### Endpoint Authorization Matrix

| Endpoint | Authorization |
|----------|--------------|
| `POST /statements` | `hasRole('api.admin') or hasAuthority('SCOPE_statement:create')` |
| `GET /statements/{id}` | `hasRole('api.admin') or hasAuthority('SCOPE_statement:read')` |
| `GET /statements` | `hasRole('api.admin') or hasAuthority('SCOPE_statement:read')` |
| `POST /statements/{id}/download-links` | `hasRole('api.admin') or hasAuthority('SCOPE_statement:create')` |
| `GET /statements/download/{token}` | `permitAll` (token provides auth) |
| `DELETE /statements/{id}` | `hasRole('api.admin') or hasAuthority('SCOPE_statement:delete')` |

## Security Features

- **JWT signature validation** — Azure AD RSA keys
- **HMAC token integrity** — Server-side secret prevents tampering
- **Token expiry** — Download links have configurable TTL
- **Download count limits** — Prevents unlimited re-downloads
- **Account number masking** — Only last 4 digits shown in API responses
- **Soft delete** — Revokes all active download links on deletion
- **Input validation** — File type, size, and field constraints enforced
- **CORS configuration** — Controlled cross-origin access

## Data Sanitization

### Upload Input Validation
| Check | Rule | Response |
|-------|------|----------|
| PDF magic bytes | First 4 bytes must be `%PDF` (0x25504446) | 400 ValidationException |
| File size | Max 10MB (enforced at application + servlet level) | 400 ValidationException |
| Content type | Must be `application/pdf` | 400 ValidationException |
| Customer ID length | Max 50 characters | 400 ValidationException |
| Account number length | Max 20 characters | 400 ValidationException |
| Path traversal (customerId) | `../`, `..\` stripped; non-alphanumeric replaced with `_` | Sanitized silently |
| Path traversal (filename) | Control chars and traversal sequences removed | Sanitized silently |
| Filename length | Max 255 characters (truncated) | Truncated silently |

### ID Format Validation
| Check | Rule | Response |
|-------|------|----------|
| Statement ID (find/delete) | Must be valid UUID format | 400 ValidationException |
| Statement ID (download link) | Must be valid UUID format | 400 ValidationException |

### HMAC Secret Validation
- Startup warning if using default `change-me-in-production` value
- Startup warning if secret is shorter than 32 characters
- Production deployments must set `DOWNLOAD_HMAC_SECRET` environment variable

### Servlet Multipart Limits
- `max-file-size: 10MB` — rejects oversized files at container level
- `max-request-size: 12MB` — allows for multipart overhead

## Configuration

Security-sensitive properties stored in Vault:

| Property | Source |
|----------|--------|
| `spring.security.oauth2.resourceserver.jwt.issuer-uri` | application YAML |
| `download.hmac-secret` | Vault (`vault/data/statement-api`) |
| `external.azure-blob-storage.connection-string` | Vault (Jasypt encrypted) |
