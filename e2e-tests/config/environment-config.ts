import { config as dotenvConfig } from 'dotenv';
import * as path from 'path';

dotenvConfig({ path: path.join(__dirname, '..', '.env.defaults') });
dotenvConfig({ path: path.join(__dirname, '..', '.env') });

export interface EnvironmentConfig {
  name: string;
  baseUrl: string;
  authUrl: string;
  clientId: string;
  clientSecret: string;
}

export const ENVIRONMENT_CONFIGS: Record<string, EnvironmentConfig> = {
  local: {
    name: 'local',
    baseUrl: process.env.BASE_URL_LOCAL || '',
    authUrl: process.env.AUTH_URL_LOCAL || '',
    clientId: process.env.API_TEST_CLIENT_ID_DEV || '',
    clientSecret: process.env.API_TEST_CLIENT_SECRET_DEV || ''
  },
  dev: {
    name: 'dev',
    baseUrl: process.env.BASE_URL_DEV || '',
    authUrl: process.env.AUTH_URL_DEV || '',
    clientId: process.env.API_TEST_CLIENT_ID_DEV || '',
    clientSecret: process.env.API_TEST_CLIENT_SECRET_DEV || ''
  },
  test: {
    name: 'test',
    baseUrl: process.env.BASE_URL_TEST || '',
    authUrl: process.env.AUTH_URL_TEST || '',
    clientId: process.env.API_TEST_CLIENT_ID_TEST || '',
    clientSecret: process.env.API_TEST_CLIENT_SECRET_TEST || ''
  },
  staging: {
    name: 'staging',
    baseUrl: process.env.BASE_URL_STAGING || '',
    authUrl: process.env.AUTH_URL_STAGING || '',
    clientId: process.env.API_TEST_CLIENT_ID_STAGING || '',
    clientSecret: process.env.API_TEST_CLIENT_SECRET_STAGING || ''
  }
};

export function getCurrentEnvironmentName(): string {
  const projectArg = process.argv.find(arg => arg.startsWith('--project='))?.split('=')[1];
  return projectArg || process.env.TEST_ENV || 'local';
}

export function getCurrentEnvironmentConfig(): EnvironmentConfig {
  const envName = getCurrentEnvironmentName();
  const config = ENVIRONMENT_CONFIGS[envName];
  if (!config) throw new Error(`Unknown environment: ${envName}`);
  if (!config.baseUrl || !config.authUrl) {
    throw new Error(`Missing configuration for '${envName}' in .env.defaults`);
  }
  return config;
}

export function getEnvironmentConfig(environmentName: string): EnvironmentConfig {
  const config = ENVIRONMENT_CONFIGS[environmentName];
  if (!config) throw new Error(`Unknown environment: ${environmentName}`);
  return config;
}
