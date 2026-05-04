# Production Readiness Checklist

## Statement API — statement-api v1.0.0-SNAPSHOT

### Build & Deploy

- [ ] Application builds without errors (`mvn clean package`)
- [ ] All unit tests pass (80%+ coverage)
- [ ] All integration tests pass
- [ ] Docker image builds and runs correctly
- [ ] CI/CD pipeline configured and passing
- [ ] Deployment manifests verified for all environments

### Configuration

- [ ] All secrets stored in Vault (`vault/data/statement-api`)
- [ ] No hardcoded credentials in source code
- [ ] Jasypt encryption password set per environment
- [ ] Database connection pool sized appropriately
- [ ] RabbitMQ connection configured with retry/backoff
- [ ] Azure Blob Storage connection validated
- [ ] HMAC secret is strong (256-bit minimum)
- [ ] CORS origins restricted per environment

### Security

- [ ] OAuth2 JWT validation enabled
- [ ] Correct Azure AD audience per environment
- [ ] All management endpoints require authentication
- [ ] Role/scope authorization enforced
- [ ] Download token expiry configured
- [ ] Download count limits set
- [ ] Account number masking verified
- [ ] HTTPS enforced in non-local environments
- [ ] Security headers configured (via gateway or app)

### Observability

- [ ] Health endpoint active (`/actuator/health`)
- [ ] Metrics endpoint enabled (`/actuator/metrics`)
- [ ] Sentry DSN configured for error tracking
- [ ] Application logs structured and include correlation IDs
- [ ] Log level set appropriately (INFO for prod)

### Database

- [ ] DDL scripts applied and verified
- [ ] Indexes created for query patterns (customerId, accountNumber)
- [ ] Connection pool limits match expected load
- [ ] Database migrations versioned (if applicable)

### Messaging

- [ ] RabbitMQ exchange `domain.events` created
- [ ] Event consumers subscribed
- [ ] Dead letter queue configured
- [ ] Message retry policy set

### Performance

- [ ] File upload size limits configured
- [ ] API rate limiting enabled (gateway level)
- [ ] Database query performance validated
- [ ] Blob storage upload/download tested under load

### Documentation

- [ ] API specification complete
- [ ] Swagger UI accessible
- [ ] Runbook/operational guide available
- [ ] Architecture diagram current
