# Statement API Usage Guide

> Consumer integration guide for the statement-api

---

## Overview

The Statement API manages customer account statements stored as PDFs in Azure Blob Storage. It provides secure upload, retrieval, and time-limited download link generation.

---

## Quick Start

### 1. Get an Access Token

```bash
curl -X POST "https://login.microsoftonline.com/95d6a08d-8d38-495b-9c70-ab0cacd42d64/oauth2/v2.0/token" \
  -d "client_id=$CLIENT_ID" \
  -d "client_secret=$CLIENT_SECRET" \
  -d "scope=api://eb596b75-d742-4786-8325-2a55591c69d9/.default" \
  -d "grant_type=client_credentials"
```

### 2. Upload a Statement

```bash
curl -X POST "http://localhost:8093/api/v1/statements" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@statement.pdf" \
  -F "customerId=CUST-001" \
  -F "statementDate=2024-01-31T00:00:00" \
  -F "statementType=MONTHLY" \
  -F "accountNumber=1234567890"
```

### 3. Generate a Download Link

```bash
curl -X POST "http://localhost:8093/api/v1/statements/$STATEMENT_ID/download-links" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"expiryMinutes": 60}'
```

### 4. Share the Download URL

The response contains a `downloadUrl` that can be shared with the customer. No authentication is needed to download — the HMAC-signed token provides authorization.

---

## Common Workflows

### Upload and Share Statement

```bash
# Step 1: Upload
RESPONSE=$(curl -s -X POST "http://localhost:8093/api/v1/statements" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@jan-2024.pdf" \
  -F "customerId=CUST-001" \
  -F "statementDate=2024-01-31T00:00:00" \
  -F "statementType=MONTHLY" \
  -F "accountNumber=1234567890")

STATEMENT_ID=$(echo $RESPONSE | jq -r '.id')

# Step 2: Generate download link
LINK=$(curl -s -X POST "http://localhost:8093/api/v1/statements/$STATEMENT_ID/download-links" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"expiryMinutes": 1440}')

DOWNLOAD_URL=$(echo $LINK | jq -r '.downloadUrl')
echo "Share this URL with customer: $DOWNLOAD_URL"
```

### List Customer Statements

```bash
curl "http://localhost:8093/api/v1/statements?customerId=CUST-001&limit=20&offset=0" \
  -H "Authorization: Bearer $TOKEN"
```

---

## Code Examples

### Python

```python
import requests

BASE_URL = "http://localhost:8093/api/v1"
TOKEN = "your-jwt-token"
headers = {"Authorization": f"Bearer {TOKEN}"}

# Upload statement
with open("statement.pdf", "rb") as f:
    response = requests.post(
        f"{BASE_URL}/statements",
        headers=headers,
        files={"file": ("statement.pdf", f, "application/pdf")},
        data={
            "customerId": "CUST-001",
            "statementDate": "2024-01-31T00:00:00",
            "statementType": "MONTHLY",
            "accountNumber": "1234567890"
        }
    )
statement_id = response.json()["id"]

# Generate download link
link_response = requests.post(
    f"{BASE_URL}/statements/{statement_id}/download-links",
    headers={**headers, "Content-Type": "application/json"},
    json={"expiryMinutes": 60}
)
download_url = link_response.json()["downloadUrl"]
```

### JavaScript/TypeScript

```typescript
const BASE_URL = 'http://localhost:8093/api/v1';
const headers = { 'Authorization': `Bearer ${TOKEN}` };

// Upload statement
const formData = new FormData();
formData.append('file', pdfBlob, 'statement.pdf');
formData.append('customerId', 'CUST-001');
formData.append('statementDate', '2024-01-31T00:00:00');
formData.append('statementType', 'MONTHLY');
formData.append('accountNumber', '1234567890');

const uploadResponse = await fetch(`${BASE_URL}/statements`, {
  method: 'POST',
  headers: { 'Authorization': `Bearer ${TOKEN}` },
  body: formData
});
const { id } = await uploadResponse.json();

// Generate download link
const linkResponse = await fetch(`${BASE_URL}/statements/${id}/download-links`, {
  method: 'POST',
  headers: { ...headers, 'Content-Type': 'application/json' },
  body: JSON.stringify({ expiryMinutes: 60 })
});
const { downloadUrl } = await linkResponse.json();
```

---

## Error Handling

| Status | Meaning | Action |
|--------|---------|--------|
| 401 | Token expired/invalid | Refresh your JWT token |
| 403 | Insufficient permissions | Check your scopes |
| 404 | Statement not found | Verify the ID exists |
| 410 | Download link expired | Generate a new download link |
| 422 | Validation failed | Check request body |

---

## Best Practices

1. **Cache JWT tokens** — Don't request a new token for every API call
2. **Handle download link expiry** — Generate links close to when they're needed
3. **Set appropriate expiry** — Use short expiry for sensitive documents
4. **Validate locally** — Check required fields before API calls
