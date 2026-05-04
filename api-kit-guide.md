# API Kit Usage Guide

Follow these steps to generate your API. Phases auto-progress on success - just execute Phase 1 and watch the magic happen!

**What to expect:** Phases validate → generate → verify → auto-progress. If something fails, you'll get clear error messages.

---

## Step 1: Define Your API

Choose your path based on whether you're building new or migrating existing code:

### Option A: New API (Manual Definition)

**Customize** `kit/spec/api-definition.md` with your API requirements in natural language.

> 💡 **Pro Tips:**
> - The template generates with basic placeholders - **use Copilot to help fill it out!**
> - Paste user stories, Jira descriptions, or requirements docs and prompt: *"Help me define the API based on this"*
> - Share existing OpenAPI specs from external integrations, or wireframes for context
> - Describe your domain in plain English - Copilot will suggest endpoints, DTOs, and validations and complete the definition before progressing to build phase.

**Need examples?** See [docs/api-definition-examples/](docs/api-definition-examples/)
**Need help?** Read [docs/api-definition-guide.md](docs/api-definition-guide.md)

**After completing the definition → Continue to Step 2**

---

### Option B: Migrating Existing Service

If you have legacy code to modernize, the migration prompt will analyze it and generate the `api-definition.md` for you.

```
Migrate legacy service to new API:

LEGACY_PATH=C:\path\to\legacy-service

Follow instructions in kit/prompts/migrate-api-prompt.md
```

**What happens:**
- Copies legacy code to `/migrate` folder
- Discovers package structure and layer organization
- Scans DTOs/entities for fields, types, and validation
- Scans controllers for endpoints and security annotations
- Detects events, integrations, database patterns
- Generates complete `kit/spec/api-definition.md`
- Creates `MIGRATION-REPORT.md` with review items

**After migration completes:** Review `api-definition.md` and `MIGRATION-REPORT.md`, then **Continue to Step 2**

📖 **Full migration documentation:** [docs/migration-guide.md](docs/migration-guide.md)

---

## Step 2: Run Phase 1 (Scaffold)

Generates project structure and config files.

```
Follow every instruction in kit/prompts/build-api-phase1-scaffold.md. Start with step 1 now.
```

**✨ Auto-progresses to Phase 2** if `mvn clean compile` succeeds.

[What Phase 1 does in detail →](docs/generation-workflow-guide.md#phase-1-scaffolding)

---

## Step 3: Run Phase 2 (Code)

Generates business logic layers (DTOs, Service, Controller, etc.).

```
Follow every instruction in kit/prompts/build-api-phase2-develop.md. Start with step 1 now.
```

**✨ Auto-progresses to Phase 3** if `mvn clean compile` succeeds with zero warnings.

[What Phase 2 does in detail →](docs/generation-workflow-guide.md#phase-2-business-logic)

---

## Step 4: Run Phase 3 (Tests)

Generates test suite (Unit, Integration, E2E framework).

```
Follow every instruction in kit/prompts/build-api-phase3-test.md. Start with step 1 now.
```

**✨ Auto-progresses to Phase 4** if all tests pass with ≥80% coverage.

[What Phase 3 does in detail →](docs/generation-workflow-guide.md#phase-3-tests)

---

## Step 5: Run Phase 4 (Docs)

Generates README, 10 documentation files in /docs folder, and the attestation report.

```
Follow every instruction in kit/prompts/build-api-phase4-document.md. Start with step 1 now.
```

**🎉 Your API is now complete!** Review `GENERATION-REPORT.md` for attestation details.

[What Phase 4 does in detail →](docs/generation-workflow-guide.md#phase-4-documentation) | [Generation Report Guide →](docs/generation-report-guide.md)

---

## Step 6: Review Generation Report

Before configuring, review the attestation report:

1. **Open** `GENERATION-REPORT.md` in the project root
2. **Verify** the parsed configuration matches your intent
3. **Review** any warnings or recommendations
4. **Check** that all expected files were generated

📖 **See:** [Generation Report Guide](docs/generation-report-guide.md)

---

## Step 7: Post-Generation Setup

Your API is generated! Now let's get it running. Use this interactive prompt:

```
@workspace I need help with post-generation setup for this API.

Check the generated files and guide me through:
1. Configuring required secrets (Jasypt encryption, .env files)
2. Setting up RabbitMQ exchanges/queues (if events enabled)
3. Verifying the build and running tests
4. Starting the API and validating endpoints
5. Running E2E tests

Provide specific commands and values based on what was generated.
```

**What Copilot will do:**
- Scan `application.yml` to detect enabled features (security, events, integrations)
- Identify TODOs that need configuration
- Provide Jasypt encryption commands for your specific secrets
- Generate RabbitMQ setup commands for your actual exchanges/queues
- Guide you through validation steps with correct ports and endpoints

**Manual approach:** See detailed guides:
- [Configuration Guide →](standards/configuration.md#-jasypt-encryption)
- [Testing Guide →](standards/testing.md#environment-files)

---

## Step 8: Clean Up

**When ready** (API deployed, patterns established):

```powershell
Remove-Item -Recurse -Force kit/
```

**Keep the patterns** - they're now embedded in your code.

---

**Need more help?** [Generation Workflow Guide](docs/generation-workflow-guide.md) | [Kit Overview](docs/kit-overview.md) | [Standards](standards/standards.md)
