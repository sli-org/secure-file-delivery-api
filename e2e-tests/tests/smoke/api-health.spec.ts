import { test, expect } from '@playwright/test';
import { getEnvironmentConfig } from '../../config/environment-config';

test.describe('API Health Check @smoke', () => {
  let baseUrl: string;

  test.beforeAll(async ({}, testInfo) => {
    const environmentName = testInfo.project.name || 'local';
    const envConfig = getEnvironmentConfig(environmentName);
    baseUrl = envConfig.baseUrl;
  });

  test('[SFD-E2E-SMOKE-001] should respond to health check @smoke', async ({ request }) => {
    // Try actuator health endpoint (Spring Boot default)
    const healthUrl = baseUrl.replace('/api/v1', '/actuator/health');
    const response = await request.get(healthUrl);
    expect([200, 401, 403]).toContain(response.status());
  });

  test('[SFD-E2E-SMOKE-002] should respond to API base URL @smoke', async ({ request }) => {
    const response = await request.get(`${baseUrl}/statements?offset=0&limit=1`);
    // Expect 401 (no auth) or 200 (if auth not required for list)
    expect([200, 401, 403]).toContain(response.status());
  });
});
