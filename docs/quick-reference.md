# Statement API Quick Reference

## Common Commands

### Build & Run

```bash
mvn clean compile                              # Build
mvn test                                       # Run tests
mvn test jacoco:report                         # With coverage
mvn spring-boot:run -Dspring-boot.run.profiles=local  # Start locally
mvn clean package -DskipTests                  # Package
```

### Environment URLs

| Environment | API URL | Swagger UI |
|-------------|---------|------------|
| Local | http://localhost:8093 | http://localhost:8093/swagger-ui.html |
| Dev | https://devapi.local/gateway/statement-api | /swagger-ui.html |
| Test | https://testapi.local/gateway/statement-api | /swagger-ui.html |

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/statements` | Upload statement (multipart) |
| GET | `/api/v1/statements/{id}` | Get by ID |
| GET | `/api/v1/statements` | List (paginated) |
| POST | `/api/v1/statements/{id}/download-links` | Generate download link |
| GET | `/api/v1/statements/download/{token}` | Download via token (public) |
| DELETE | `/api/v1/statements/{id}` | Soft delete |

## Quick curl Examples

```bash
export TOKEN="your-jwt-token"
export BASE="http://localhost:8093/api/v1"

# Upload statement
curl -X POST "$BASE/statements" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@statement.pdf" \
  -F "customerId=CUST-001" \
  -F "statementDate=2024-01-31T00:00:00" \
  -F "statementType=MONTHLY" \
  -F "accountNumber=1234567890"

# Get by ID
curl "$BASE/statements/ID" -H "Authorization: Bearer $TOKEN"

# List
curl "$BASE/statements?customerId=CUST-001&limit=10&offset=0" -H "Authorization: Bearer $TOKEN"

# Generate download link
curl -X POST "$BASE/statements/ID/download-links" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"expiryMinutes": 60}'

# Download (no auth needed)
curl "$BASE/statements/download/TOKEN" -o statement.pdf

# Delete
curl -X DELETE "$BASE/statements/ID" -H "Authorization: Bearer $TOKEN"
```

## Health Checks

```bash
curl http://localhost:8093/actuator/health
curl http://localhost:8093/actuator/info
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| 401 Unauthorized | Check JWT token validity and expiry |
| 403 Forbidden | Verify role/scope in JWT claims |
| 404 Not Found | Confirm statement ID exists |
| 410 Gone | Download link expired; generate new one |
| 500 Internal Error | Check application logs and Sentry |
