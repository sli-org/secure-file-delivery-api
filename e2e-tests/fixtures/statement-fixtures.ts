/**
 * Test data fixtures for Statement API E2E tests
 * 
 * Provides pre-configured test data aligned with StatementDTO validation rules.
 */

export const StatementFixtures = {
  validPdfFile: (): Buffer => {
    return Buffer.from('%PDF-1.4 test content');
  },

  validUploadParams: () => ({
    fileName: `test-statement-${Date.now()}.pdf`,
    customerId: 'CUST-E2E-001',
    statementDate: '2024-01-31T00:00:00',
    statementType: 'MONTHLY',
    accountNumber: '1234567890',
  }),

  minimalUploadParams: () => ({
    fileName: 'minimal.pdf',
    customerId: 'CUST-E2E-002',
    statementDate: '2024-06-15T00:00:00',
    statementType: 'MONTHLY',
    accountNumber: '0987654321',
  }),

  missingCustomerId: () => ({
    fileName: 'test.pdf',
    customerId: '',
    statementDate: '2024-01-31T00:00:00',
    statementType: 'MONTHLY',
    accountNumber: '1234567890',
  }),

  invalidStatementType: () => ({
    fileName: 'test.pdf',
    customerId: 'CUST-E2E-001',
    statementDate: '2024-01-31T00:00:00',
    statementType: 'INVALID_TYPE',
    accountNumber: '1234567890',
  }),

  downloadLinkRequest: (expiryMinutes: number = 60) => ({
    expiryMinutes,
  }),

  xssAttempt: () => ({
    fileName: '<script>alert("xss")</script>.pdf',
    customerId: '"><script>alert(document.cookie)</script>',
    statementDate: '2024-01-31T00:00:00',
    statementType: 'MONTHLY',
    accountNumber: '1234567890',
  }),

  sqlInjectionAttempt: () => ({
    fileName: "'; DROP TABLE statements; --.pdf",
    customerId: "1' OR '1'='1",
    statementDate: '2024-01-31T00:00:00',
    statementType: 'MONTHLY',
    accountNumber: '1234567890',
  }),
};
