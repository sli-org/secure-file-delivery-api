import { config } from 'dotenv';

async function globalSetup() {
  console.log('🚀 Running global setup...');
  config({ path: './.env' });
  console.log('📁 Environment variables loaded');
  console.log('✅ Global setup completed');
}

export default globalSetup;
