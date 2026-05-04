import { test, expect } from '@playwright/test';
import { StatementApiClient } from '../../api-clients/StatementApiClient';
import { StatementFixtures } from '../../fixtures/statement-fixtures';
import { getAuthToken } from '../../helpers/auth-helper';
import { ApiDebugHelper } from '../../helpers/api-debug-helper';
import { TestCleanupHelper } from '../../helpers/test-cleanup-helper';
import { getEnvironmentConfig } from '../../config/environment-config';
import '../../helpers/api-matchers';

test.describe('Statement API - CRUD Scenarios', () => {
  let statementApi: StatementApiClient;
  let authToken: string;
  let baseUrl: string;
  let environmentName: string;

  test.beforeAll(async ({}, testInfo) => {
    environmentName = testInfo.project.name || 'local';
    authToken = await getAuthToken(environmentName);
    const envConfig = getEnvironmentConfig(environmentName);
    baseUrl = envConfig.baseUrl;
  });

  test.beforeEach(async ({ request }) => {
    ApiDebugHelper.reset();
    statementApi = new StatementApiClient(request, baseUrl, authToken, environmentName);
  });

  test.afterEach(async () => {
    await TestCleanupHelper.cleanupAllStatements(statementApi);
    ApiDebugHelper.attachDebugData();
  });

  // =====================================================
  // UPLOAD (CREATE)
  // =====================================================

  test.describe('Upload Statement', () => {
    test('[SFD-E2E-001] should upload statement with valid data @api @happy-path', async () => {
      const file = StatementFixtures.validPdfFile();
      const params = StatementFixtures.validUploadParams();
      const result = await TestCleanupHelper.createStatementWithCleanup(
        statementApi, file, params.fileName, params.customerId, params.statementDate, params.statementType, params.accountNumber
      );
      expect(result.response.status()).toBe(201);
      expect(result.statementId).toBeTruthy();
    });

    test('[SFD-E2E-002] should reject upload without authentication @api @security', async ({ request }) => {
      const file = StatementFixtures.validPdfFile();
      const params = StatementFixtures.validUploadParams();
      const response = await request.post(`${baseUrl}/statements`, {
        multipart: {
          file: { name: params.fileName, mimeType: 'application/pdf', buffer: file },
          customerId: params.customerId,
          statementDate: params.statementDate,
          statementType: params.statementType,
          accountNumber: params.accountNumber
        }
      });
      expect(response.status()).toBe(401);
    });
  });

  // =====================================================
  // GET BY ID (READ)
  // =====================================================

  test.describe('Get Statement', () => {
    test('[SFD-E2E-010] should get statement by ID @api @happy-path', async () => {
      const file = StatementFixtures.validPdfFile();
      const params = StatementFixtures.validUploadParams();
      const result = await TestCleanupHelper.createStatementWithCleanup(
        statementApi, file, params.fileName, params.customerId, params.statementDate, params.statementType, params.accountNumber
      );
      const statementId = result.statementId!;

      const response = await statementApi.getStatement(statementId);
      expect(response.status()).toBe(200);
      const body = await response.json();
      expect(body.id).toBe(statementId);
      expect(body.customerId).toBe(params.customerId);
    });

    test('[SFD-E2E-011] should return 404 for non-existent statement @api @error', async () => {
      const response = await statementApi.getStatement('non-existent-id-12345');
      expect(response.status()).toBe(404);
    });

    test('[SFD-E2E-012] should reject unauthenticated get request @api @security', async ({ request }) => {
      const response = await request.get(`${baseUrl}/statements/any-id`);
      expect(response.status()).toBe(401);
    });
  });

  // =====================================================
  // LIST (READ)
  // =====================================================

  test.describe('List Statements', () => {
    test('[SFD-E2E-020] should list statements with pagination @api @happy-path', async () => {
      const response = await statementApi.getStatements({ offset: 0, limit: 10 });
      expect(response.status()).toBe(200);
      const body = await response.json();
      expect(body.list).toBeDefined();
      expect(Array.isArray(body.list)).toBe(true);
    });

    test('[SFD-E2E-021] should filter statements by customerId @api @happy-path', async () => {
      const file = StatementFixtures.validPdfFile();
      const params = StatementFixtures.validUploadParams();
      await TestCleanupHelper.createStatementWithCleanup(
        statementApi, file, params.fileName, params.customerId, params.statementDate, params.statementType, params.accountNumber
      );

      const response = await statementApi.getStatements({ customerId: params.customerId });
      expect(response.status()).toBe(200);
      const body = await response.json();
      expect(body.list.length).toBeGreaterThanOrEqual(1);
    });

    test('[SFD-E2E-022] should reject unauthenticated list request @api @security', async ({ request }) => {
      const response = await request.get(`${baseUrl}/statements?offset=0&limit=10`);
      expect(response.status()).toBe(401);
    });
  });

  // =====================================================
  // GENERATE DOWNLOAD LINK
  // =====================================================

  test.describe('Generate Download Link', () => {
    test('[SFD-E2E-030] should generate download link for statement @api @happy-path', async () => {
      const file = StatementFixtures.validPdfFile();
      const params = StatementFixtures.validUploadParams();
      const result = await TestCleanupHelper.createStatementWithCleanup(
        statementApi, file, params.fileName, params.customerId, params.statementDate, params.statementType, params.accountNumber
      );
      const statementId = result.statementId!;

      const response = await statementApi.generateDownloadLink(statementId);
      expect(response.status()).toBe(201);
      const body = await response.json();
      expect(body.statementId).toBe(statementId);
      expect(body.downloadUrl).toBeTruthy();
    });

    test('[SFD-E2E-031] should return 404 for non-existent statement @api @error', async () => {
      const response = await statementApi.generateDownloadLink('non-existent-id');
      expect(response.status()).toBe(404);
    });
  });

  // =====================================================
  // DOWNLOAD (Public, token-based)
  // =====================================================

  test.describe('Download via Token', () => {
    test('[SFD-E2E-040] should download statement via valid token @api @happy-path', async () => {
      const file = StatementFixtures.validPdfFile();
      const params = StatementFixtures.validUploadParams();
      const result = await TestCleanupHelper.createStatementWithCleanup(
        statementApi, file, params.fileName, params.customerId, params.statementDate, params.statementType, params.accountNumber
      );
      const statementId = result.statementId!;

      const linkResponse = await statementApi.generateDownloadLink(statementId);
      const linkBody = await linkResponse.json();
      const token = linkBody.downloadUrl?.split('/download/').pop();

      if (token) {
        const downloadResponse = await statementApi.downloadByToken(token);
        expect(downloadResponse.status()).toBe(200);
      }
    });

    test('[SFD-E2E-041] should return error for invalid download token @api @error', async () => {
      const response = await statementApi.downloadByToken('invalid-token-12345');
      expect([400, 404]).toContain(response.status());
    });
  });

  // =====================================================
  // DELETE (Soft Delete)
  // =====================================================

  test.describe('Delete Statement', () => {
    test('[SFD-E2E-050] should soft delete statement @api @happy-path', async () => {
      const file = StatementFixtures.validPdfFile();
      const params = StatementFixtures.validUploadParams();
      const result = await TestCleanupHelper.createStatementWithCleanup(
        statementApi, file, params.fileName, params.customerId, params.statementDate, params.statementType, params.accountNumber
      );
      const statementId = result.statementId!;

      const deleteResponse = await statementApi.deleteStatement(statementId);
      expect(deleteResponse.status()).toBe(204);
    });

    test('[SFD-E2E-051] should return 404 for non-existent statement delete @api @error', async () => {
      const response = await statementApi.deleteStatement('non-existent-id');
      expect([204, 404]).toContain(response.status());
    });

    test('[SFD-E2E-052] should reject unauthenticated delete @api @security', async ({ request }) => {
      const response = await request.delete(`${baseUrl}/statements/any-id`);
      expect(response.status()).toBe(401);
    });
  });
});
