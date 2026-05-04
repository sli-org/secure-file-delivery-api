# API Specification - Statement API

## Base URL

| Environment | URL |
|-------------|-----|
| Local | `http://localhost:8093` |
| Development | `https://devapi.local/gateway/statement-api` |
| Test | `https://testapi.local/gateway/statement-api` |
| Production | `https://api.local/gateway/statement-api` |

## Authentication

All management endpoints require a valid JWT bearer token (Azure AD OAuth2). The download endpoint uses token-based auth (no JWT).

```bash
# Azure AD OAuth2 Client Credentials Flow
curl -X POST "https://login.microsoftonline.com/95d6a08d-8d38-495b-9c70-ab0cacd42d64/oauth2/v2.0/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id={client-id}&client_secret={client-secret}&scope=api://eb596b75-d742-4786-8325-2a55591c69d9/.default&grant_type=client_credentials"
```

---

## Endpoints

### Upload Statement

**POST** `/api/v1/statements`

Uploads a new customer statement PDF via multipart form data. Stores the file in Azure Blob Storage and creates statement metadata.

**Authorization:** `hasRole('api.admin') or hasAuthority('SCOPE_statement:create')`

**Request:** Multipart form data

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| file | binary | Yes | PDF file (max 10MB) |
| customerId | string | Yes | Customer identifier (max 50 chars) |
| statementDate | datetime | Yes | Statement period date (ISO 8601) |
| statementType | string | Yes | MONTHLY, ANNUAL, TAX, or AD_HOC |
| accountNumber | string | Yes | Account number (max 20 chars) |

```bash
curl -X POST "http://localhost:8093/api/v1/statements" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@statement.pdf" \
  -F "customerId=CUST-001" \
  -F "statementDate=2024-01-31T00:00:00" \
  -F "statementType=MONTHLY" \
  -F "accountNumber=1234567890"
```

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customerId": "CUST-001",
  "statementDate": "2024-01-31T00:00:00",
  "statementType": "MONTHLY",
  "accountNumber": "****7890",
  "fileName": "statement.pdf",
  "fileSize": 125432,
  "status": "AVAILABLE"
}
```

---

### Get Statement by ID

**GET** `/api/v1/statements/{id}`

**Authorization:** `hasRole('api.admin') or hasAuthority('SCOPE_statement:read')`

```bash
curl "http://localhost:8093/api/v1/statements/{id}" -H "Authorization: Bearer $TOKEN"
```

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customerId": "CUST-001",
  "statementDate": "2024-01-31T00:00:00",
  "statementType": "MONTHLY",
  "accountNumber": "****7890",
  "fileName": "statement.pdf",
  "fileSize": 125432,
  "contentHash": "sha256-abc123...",
  "status": "AVAILABLE",
  "retentionDays": 365,
  "createdAt": "2024-01-15T10:30:00Z",
  "createdBy": "client-id"
}
```

---

### List Statements

**GET** `/api/v1/statements`

**Authorization:** `hasRole('api.admin') or hasAuthority('SCOPE_statement:read')`

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| customerId | string | required | Customer identifier |
| statementType | string | optional | Filter by type |
| status | string | optional | Filter by status |
| fromDate | datetime | optional | Filter from date |
| toDate | datetime | optional | Filter to date |
| limit | integer | 10 | Max results (max 100) |
| offset | integer | 0 | Pagination offset |

```bash
curl "http://localhost:8093/api/v1/statements?customerId=CUST-001&limit=10&offset=0" \
  -H "Authorization: Bearer $TOKEN"
```

**Response (200 OK):**
```json
{
  "list": [...],
  "paging": {
    "limit": 10,
    "offset": 0,
    "total": 25
  }
}
```

---

### Generate Download Link

**POST** `/api/v1/statements/{id}/download-links`

Generates a secure, time-limited, HMAC-signed download link.

**Authorization:** `hasRole('api.admin') or hasAuthority('SCOPE_statement:create')`

```bash
curl -X POST "http://localhost:8093/api/v1/statements/{id}/download-links" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"expiryMinutes": 60}'
```

**Response (201 Created):**
```json
{
  "id": "link-uuid",
  "statementId": "stmt-uuid",
  "downloadUrl": "http://localhost:8093/api/v1/statements/download/eyJ...",
  "expiresAt": "2024-01-16T10:30:00Z",
  "maxDownloads": 1,
  "status": "ACTIVE"
}
```

---

### Download Statement (Token-Based, Public)

**GET** `/api/v1/statements/download/{token}`

Downloads a statement PDF using a secure HMAC-signed token. No JWT required.

```bash
curl "http://localhost:8093/api/v1/statements/download/eyJ..." -o statement.pdf
```

**Response (200 OK):** PDF binary stream with headers:
```
Content-Type: application/pdf
Content-Disposition: attachment; filename="statement-2024-01.pdf"
```

**Error Responses:**
- 401 — Invalid or tampered token
- 410 — Token expired or download limit reached
- 404 — Statement not found or deleted

---

### Delete Statement (Soft Delete)

**DELETE** `/api/v1/statements/{id}`

Soft-deletes a statement and revokes all active download links.

**Authorization:** `hasRole('api.admin') or hasAuthority('SCOPE_statement:delete')`

```bash
curl -X DELETE "http://localhost:8093/api/v1/statements/{id}" -H "Authorization: Bearer $TOKEN"
```

**Response:** 204 No Content

---

## Data Models

### StatementDTO

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| id | string | Read-only | UUID |
| customerId | string | OnCreate | Customer identifier (max 50) |
| statementDate | datetime | OnCreate | Statement period date |
| statementType | StatementTypeCode | OnCreate | MONTHLY, ANNUAL, TAX, AD_HOC |
| accountNumber | string | OnCreate | Masked in responses (****1234) |
| fileName | string | OnCreate | Original PDF filename |
| fileSize | integer | Read-only | File size in bytes |
| contentHash | string | Read-only | SHA-256 hash |
| blobPath | string | Read-only | Storage path |
| status | StatementStatusCode | Read-only | AVAILABLE, EXPIRED, DELETED |
| retentionDays | integer | Optional | Default 365 |
| createdAt | datetime | Read-only | Creation timestamp |
| createdBy | string | Read-only | Creator |

### DownloadLinkDTO

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| id | string | Read-only | UUID |
| statementId | string | Required | Parent statement ID |
| downloadUrl | string | Read-only | Full download URL with token |
| expiresAt | datetime | Required | Expiration timestamp |
| maxDownloads | integer | Optional | Default 1 |
| downloadCount | integer | Read-only | Times downloaded |
| status | DownloadLinkStatusCode | Read-only | ACTIVE, USED, EXPIRED, REVOKED |

---

## Error Responses

| Code | Meaning |
|------|---------|
| 200 | OK |
| 201 | Created |
| 204 | No Content |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 410 | Gone (expired token) |
| 422 | Validation failure |
| 500 | Internal Server Error |

```json
{
  "message": "Error summary",
  "errors": [
    { "field": "customerId", "message": "Customer ID is required" }
  ]
}
```
