import { APIRequestContext, APIResponse } from '@playwright/test';
import { BaseApiClient } from './BaseApiClient';

export class StatementApiClient extends BaseApiClient {
  constructor(
    request: APIRequestContext,
    baseUrl: string,
    authToken: string,
    environmentName?: string
  ) {
    super(request, baseUrl, authToken, environmentName);
  }

  async uploadStatement(
    file: Buffer,
    fileName: string,
    customerId: string,
    statementDate: string,
    statementType: string,
    accountNumber: string
  ): Promise<APIResponse> {
    return await this.request.post(`${this.baseUrl}/statements`, {
      headers: {
        'Authorization': `Bearer ${this.authToken}`
      },
      multipart: {
        file: { name: fileName, mimeType: 'application/pdf', buffer: file },
        customerId: customerId,
        statementDate: statementDate,
        statementType: statementType,
        accountNumber: accountNumber
      }
    });
  }

  async getStatement(statementId: string): Promise<APIResponse> {
    return await this.get(`${this.baseUrl}/statements/${statementId}`, {
      headers: this.getAuthHeaders()
    });
  }

  async getStatements(params: { customerId?: string; offset?: number; limit?: number } = {}): Promise<APIResponse> {
    const queryParts: string[] = [];
    if (params.customerId) queryParts.push(`customerId=${params.customerId}`);
    queryParts.push(`offset=${params.offset ?? 0}`);
    queryParts.push(`limit=${params.limit ?? 10}`);
    const query = queryParts.join('&');

    return await this.get(`${this.baseUrl}/statements?${query}`, {
      headers: this.getAuthHeaders()
    });
  }

  async generateDownloadLink(statementId: string, expiryMinutes?: number): Promise<APIResponse> {
    return await this.post(`${this.baseUrl}/statements/${statementId}/download-links`, {
      headers: this.getAuthHeaders(),
      data: { expiryMinutes: expiryMinutes ?? 60 }
    });
  }

  async downloadByToken(token: string): Promise<APIResponse> {
    return await this.get(`${this.baseUrl}/statements/download/${token}`, {
      headers: this.getHeaders()
    });
  }

  async deleteStatement(statementId: string): Promise<APIResponse> {
    return await this.delete(`${this.baseUrl}/statements/${statementId}`, {
      headers: this.getAuthHeaders()
    });
  }

  async getStatementsUnauthenticated(): Promise<APIResponse> {
    return await this.get(`${this.baseUrl}/statements?offset=0&limit=10`, {
      headers: this.getHeaders()
    });
  }
}
