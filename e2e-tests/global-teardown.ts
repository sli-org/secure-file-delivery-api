import { clearTokenCache } from './helpers/auth-helper';

async function globalTeardown() {
  console.log('🧹 Running global teardown...');
  clearTokenCache();
  console.log('✅ Global teardown completed');
}

export default globalTeardown;
