import { defineConfig, devices } from '@playwright/test';
import { ENVIRONMENT_CONFIGS } from './config/environment-config';

// Disable API debugging by default
if (!process.env.DEBUG || process.env.DEBUG === 'off') {
  process.env.DEBUG = 'off';
}

export default defineConfig({
  testDir: './tests',
  globalSetup: './global-setup.ts',
  globalTeardown: './global-teardown.ts',
  timeout: 30 * 1000,
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  
  reporter: [
    ['html', { outputFolder: 'playwright-report', open: 'never' }],
    ['json', { outputFile: 'test-results/results.json' }],
    ['junit', { outputFile: 'test-results/junit.xml' }],
    ...(process.env.CI ? [['list'] as const] : [['line'] as const])
  ],

  use: {
    baseURL: ENVIRONMENT_CONFIGS.local.baseUrl,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    extraHTTPHeaders: { 'Content-Type': 'application/json' },
    actionTimeout: 15 * 1000,
  },

  projects: [
    {
      name: 'local',
      use: { baseURL: ENVIRONMENT_CONFIGS.local.baseUrl },
    },
    {
      name: 'dev',
      use: { baseURL: ENVIRONMENT_CONFIGS.dev.baseUrl },
    },
    {
      name: 'test',
      use: { baseURL: ENVIRONMENT_CONFIGS.test.baseUrl },
    },
    {
      name: 'staging',
      use: { baseURL: ENVIRONMENT_CONFIGS.staging.baseUrl },
    },
  ],
});
