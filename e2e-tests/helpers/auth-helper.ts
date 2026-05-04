import { getEnvironmentConfig } from '../config/environment-config';
import * as fs from 'fs';
import * as path from 'path';

export type CredentialType = 'admin' | 'limited';

interface TokenCache {
  token: string;
  expiresAt: number;
}

interface TokenResponse {
  access_token: string;
  expires_in?: number;
}

const tokenCacheMap: Map<string, TokenCache> = new Map();
const activeRequests: Map<string, Promise<string>> = new Map();
const CACHE_DIR = path.join(process.cwd(), '.token-cache');

function ensureCacheDir(): void {
  if (!fs.existsSync(CACHE_DIR)) {
    fs.mkdirSync(CACHE_DIR, { recursive: true });
  }
}

function getCacheKey(authUrl: string, clientId: string, credentialType: CredentialType = 'admin'): string {
  return `${authUrl}:${clientId}:${credentialType}`;
}

function getCacheFilePath(cacheKey: string): string {
  const safeKey = cacheKey.replace(/[^a-zA-Z0-9]/g, '_');
  return path.join(CACHE_DIR, `oauth2-token-${safeKey}.json`);
}

function loadTokenFromFile(cacheKey: string): TokenCache | null {
  try {
    const filePath = getCacheFilePath(cacheKey);
    if (!fs.existsSync(filePath)) return null;
    const data = fs.readFileSync(filePath, 'utf8');
    const tokenCache = JSON.parse(data) as TokenCache;
    if (Date.now() < tokenCache.expiresAt - 300000) {
      return tokenCache;
    }
    fs.unlinkSync(filePath);
    return null;
  } catch {
    return null;
  }
}

function saveTokenToFile(cacheKey: string, tokenCache: TokenCache): void {
  try {
    ensureCacheDir();
    const filePath = getCacheFilePath(cacheKey);
    fs.writeFileSync(filePath, JSON.stringify(tokenCache), 'utf8');
  } catch (error) {
    console.log(`Warning: Could not save token to cache: ${error}`);
  }
}

export async function getAuthToken(
  environmentName?: string,
  credentialType: CredentialType = 'admin'
): Promise<string> {
  const envName = environmentName || 'local';
  const envConfig = getEnvironmentConfig(envName);
  const authUrl = envConfig.authUrl;
  
  const suffix = credentialType === 'limited' ? '_LIMITED' : '';
  const clientId = credentialType === 'admin' 
    ? envConfig.clientId 
    : process.env[`API_TEST_CLIENT_ID_${envName.toUpperCase()}${suffix}`] || '';
  const clientSecret = credentialType === 'admin'
    ? envConfig.clientSecret
    : process.env[`API_TEST_CLIENT_SECRET_${envName.toUpperCase()}${suffix}`] || '';

  if (!clientId || !clientSecret) {
    throw new Error(`Credentials not configured for '${envName}' (${credentialType})`);
  }

  const cacheKey = getCacheKey(authUrl, clientId, credentialType);
  
  let cachedToken = tokenCacheMap.get(cacheKey);
  if (!cachedToken) {
    cachedToken = loadTokenFromFile(cacheKey) || undefined;
    if (cachedToken) tokenCacheMap.set(cacheKey, cachedToken);
  }
  
  if (cachedToken && Date.now() < cachedToken.expiresAt - 300000) {
    return cachedToken.token;
  }

  const activeRequest = activeRequests.get(cacheKey);
  if (activeRequest) return activeRequest;

  const tokenPromise = requestNewToken(envName, authUrl, clientId, clientSecret, cacheKey, credentialType);
  activeRequests.set(cacheKey, tokenPromise);
  
  try {
    return await tokenPromise;
  } finally {
    activeRequests.delete(cacheKey);
  }
}

async function requestNewToken(
  envName: string,
  authUrl: string,
  clientId: string,
  clientSecret: string,
  cacheKey: string,
  credentialType: CredentialType
): Promise<string> {
  console.log(`Requesting OAuth2 token for ${envName} (${credentialType})`);
  
  const isAzureAD = authUrl.includes('microsoftonline.com');
  let response: Response;
  
  if (isAzureAD) {
    const params = new URLSearchParams({
      grant_type: 'client_credentials',
      client_id: clientId,
      client_secret: clientSecret,
      scope: 'https://graph.microsoft.com/.default'
    });
    response = await fetch(authUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: params.toString()
    });
  } else {
    response = await fetch(authUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ clientId, clientSecret, grantType: 'client_credentials' })
    });
  }

  if (!response.ok) {
    const errorBody = await response.text();
    throw new Error(`OAuth2 token request failed: ${response.status} - ${errorBody}`);
  }

  const data = await response.json() as TokenResponse;
  if (!data.access_token) throw new Error('Missing access_token in response');

  const expiresIn = data.expires_in || 3600;
  const newTokenCache: TokenCache = {
    token: data.access_token,
    expiresAt: Date.now() + (expiresIn * 1000)
  };
  
  tokenCacheMap.set(cacheKey, newTokenCache);
  saveTokenToFile(cacheKey, newTokenCache);
  
  console.log(`OAuth2 token obtained (expires in ${expiresIn}s)`);
  return newTokenCache.token;
}

export function clearTokenCache(environmentName?: string): void {
  if (environmentName) {
    const envConfig = getEnvironmentConfig(environmentName);
    const cacheKey = getCacheKey(envConfig.authUrl, envConfig.clientId);
    tokenCacheMap.delete(cacheKey);
    try {
      const filePath = getCacheFilePath(cacheKey);
      if (fs.existsSync(filePath)) fs.unlinkSync(filePath);
    } catch {}
  } else {
    tokenCacheMap.clear();
    try {
      if (fs.existsSync(CACHE_DIR)) {
        const files = fs.readdirSync(CACHE_DIR);
        for (const file of files) {
          if (file.startsWith('oauth2-token-')) {
            fs.unlinkSync(path.join(CACHE_DIR, file));
          }
        }
      }
    } catch {}
  }
}
