# Statement API Architecture

## Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Framework | Spring Boot | 3.4.x |
| Language | Java | 21 |
| Build Tool | Maven | 3.9+ |
| API Spec | OpenAPI | 3.0.3 |
| Security | OAuth2 JWT | Azure AD |
| Messaging | RabbitMQ | Event publishing |
| Storage | Azure Blob Storage | PDF file storage |
| Database | JPA/Hibernate | Entity persistence |

## Layer Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                      │
│                    StatementController                        │
│  - REST endpoints (multipart upload, JSON, PDF stream)       │
│  - Request validation, response mapping                      │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                       BUSINESS LAYER                         │
│  StatementService          │  DownloadLinkService            │
│  - Upload, find, list,     │  - Generate HMAC tokens         │
│    delete logic             │  - Validate & record downloads  │
│  - Blob storage ops        │  - Expiry management            │
├─────────────────────────────┤                                │
│  StatementTransformer      │  DownloadLinkTransformer        │
│  - DTO ↔ Entity mapping    │  - DTO ↔ Entity mapping         │
│  - Account number masking  │                                 │
├─────────────────────────────┤                                │
│  StatementEventService                                       │
│  - RabbitMQ event publishing                                 │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    PERSISTENCE / INTEGRATION                 │
│  StatementRepository       │  Azure Blob Storage (REST)      │
│  DownloadLinkRepository    │  - Upload/download/delete PDFs  │
│  - JPA/Hibernate           │  - Via RestTemplate              │
└─────────────────────────────────────────────────────────────┘
```

## Security Architecture

```
Client ──→ Azure AD (OAuth2) ──→ API Gateway ──→ Statement API
                                                       │
                                          JWT validation │
                                          Role/scope check
                                                       │
                              ┌─────────────────────────┘
                              │
              Management endpoints: JWT required
              Download endpoint: HMAC token (public, no JWT)
```

### Authentication Flows

1. **Management Operations** (upload, get, list, delete, generate link):
   - Client obtains JWT from Azure AD
   - JWT included in Authorization header
   - Spring Security validates signature and claims
   - `@PreAuthorize` enforces role/scope requirements

2. **Download Operation** (public):
   - Client uses download URL with HMAC-signed token
   - No JWT required — token itself provides authorization
   - Server validates HMAC signature, expiry, and download count

## Event Architecture

```
StatementService ──→ StatementEventService ──→ RabbitMQ (domain.events)
                                                    │
                                    ┌───────────────┼───────────────┐
                                    │               │               │
                          statement.uploaded  download-link.created  statement.deleted
```

## Package Structure

```
za.co.api.statement/
├── Application.java
├── config/
│   ├── JacksonConfig.java
│   ├── OpenApiConfig.java
│   ├── SecurityConfig.java
│   ├── RestTemplateConfig.java
│   └── WebConfig.java
├── controller/
│   └── StatementController.java
├── dto/
│   ├── StatementDTO.java
│   ├── DownloadLinkDTO.java
│   ├── CreateDownloadLinkRequestDTO.java
│   ├── StatementEventDTO.java
│   └── code/
│       ├── StatementTypeCode.java
│       ├── StatementStatusCode.java
│       ├── DownloadLinkStatusCode.java
│       └── StatementEventTypeCode.java
├── entity/
│   ├── StatementEntity.java
│   └── DownloadLinkEntity.java
├── repository/
│   ├── StatementRepository.java
│   └── DownloadLinkRepository.java
├── service/
│   ├── StatementService.java
│   ├── DownloadLinkService.java
│   └── StatementEventService.java
└── transformer/
    ├── StatementTransformer.java
    └── DownloadLinkTransformer.java
```
