import { StatementApiClient } from '../api-clients/StatementApiClient';

export class TestCleanupHelper {
  private static createdStatementIds: Set<string> = new Set();
  private static isCleanupEnabled: boolean = process.env.TEST_CLEANUP !== 'false';

  static registerStatementForCleanup(statementId: string): void {
    if (this.isCleanupEnabled && statementId) {
      this.createdStatementIds.add(statementId);
      console.log(`Registered statement ${statementId} for cleanup`);
    }
  }

  static async cleanupStatement(statementApi: StatementApiClient, statementId: string): Promise<void> {
    try {
      if (this.createdStatementIds.has(statementId)) {
        const response = await statementApi.deleteStatement(statementId);
        if ([200, 204, 404].includes(response.status())) {
          this.createdStatementIds.delete(statementId);
          console.log(`Cleaned up statement ${statementId}`);
        }
      }
    } catch (error) {
      console.warn(`Failed to cleanup statement ${statementId}:`, error);
    }
  }

  static async cleanupAllStatements(statementApi: StatementApiClient): Promise<void> {
    if (!this.isCleanupEnabled || this.createdStatementIds.size === 0) return;
    
    console.log(`Cleaning up ${this.createdStatementIds.size} statements...`);
    const ids = Array.from(this.createdStatementIds);
    await Promise.allSettled(ids.map(id => this.cleanupStatement(statementApi, id)));
  }

  static async createStatementWithCleanup(
    statementApi: StatementApiClient, 
    file: Buffer,
    fileName: string,
    customerId: string,
    statementDate: string,
    statementType: string,
    accountNumber: string
  ): Promise<{ response: any; statementId: string | null }> {
    const response = await statementApi.uploadStatement(file, fileName, customerId, statementDate, statementType, accountNumber);
    let statementId: string | null = null;
    
    if (response.status() === 201) {
      const body = await response.json();
      statementId = body.id;
      if (statementId) this.registerStatementForCleanup(statementId);
    }
    
    return { response, statementId };
  }
}
