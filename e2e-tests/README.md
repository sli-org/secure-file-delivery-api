# Statement API - E2E Tests

## Overview
Playwright-based E2E test framework for the Statement API.

## Setup

```bash
cd e2e-tests
npm install
cp .env.template .env
# Edit .env with your OAuth2 credentials
```

## Running Tests

```bash
# Local environment
npm run test:local

# Development environment
npm run test:dev

# Smoke tests only
npm run test:smoke

# View HTML report
npm run report
```

## Structure

```
e2e-tests/
├── api-clients/          # API client wrappers
│   ├── BaseApiClient.ts
│   └── StatementApiClient.ts
├── config/               # Environment configuration
│   └── environment-config.ts
├── fixtures/             # Test data
│   └── statement-fixtures.ts
├── helpers/              # Test utilities
│   ├── api-debug-helper.ts
│   ├── api-matchers.ts
│   ├── auth-helper.ts
│   └── test-cleanup-helper.ts
├── tests/
│   ├── statement/        # Statement CRUD tests
│   │   └── statement-crud.spec.ts
│   └── smoke/            # Health check tests
│       └── api-health.spec.ts
├── .env.defaults         # Non-sensitive config (committed)
├── .env.template         # Secrets template (committed)
├── playwright.config.ts  # Playwright configuration
└── package.json
```

## Environments
- **local** - http://localhost:8093
- **dev** - devapi.local
- **test** - testapi.local
- **staging** - stagingapi.local
