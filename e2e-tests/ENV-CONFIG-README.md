# Environment Configuration Guide

## Setup

1. Copy `.env.template` to `.env`
2. Fill in your OAuth2 client credentials for each environment
3. Non-sensitive defaults are in `.env.defaults` (committed to repo)

## File Structure

| File | Purpose | Committed? |
|------|---------|------------|
| `.env.defaults` | Non-sensitive URLs and config | Yes |
| `.env.template` | Template for secrets | Yes |
| `.env` | Actual secrets | **No** |

## Running Tests

```bash
# Install dependencies
npm install

# Run against local
npm run test:local

# Run against dev
npm run test:dev

# Run smoke tests only
npm run test:smoke

# View report
npm run report
```
