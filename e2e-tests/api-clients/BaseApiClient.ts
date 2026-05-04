import { APIRequestContext, APIResponse } from '@playwright/test';
import { ApiDebugHelper } from '../helpers/api-debug-helper';

export class BaseApiClient {
  constructor(
    protected request: APIRequestContext,
    protected baseUrl: string,
    protected authToken: string,
    protected environmentName?: string
  ) {}

  protected getAuthHeaders(): Record<string, string> {
    return {
      'Authorization': `Bearer ${this.authToken}`,
      'Content-Type': 'application/json'
    };
  }

  protected getHeaders(): Record<string, string> {
    return { 'Content-Type': 'application/json' };
  }

  protected async post(url: string, options: any = {}): Promise<APIResponse> {
    return await ApiDebugHelper.post(this.request, url, {
      headers: options.headers || {},
      data: options.data
    });
  }

  protected async get(url: string, options: any = {}): Promise<APIResponse> {
    return await ApiDebugHelper.get(this.request, url, {
      headers: options.headers || {}
    });
  }

  protected async put(url: string, options: any = {}): Promise<APIResponse> {
    return await ApiDebugHelper.put(this.request, url, {
      headers: options.headers || {},
      data: options.data
    });
  }

  protected async delete(url: string, options: any = {}): Promise<APIResponse> {
    return await ApiDebugHelper.delete(this.request, url, {
      headers: options.headers || {}
    });
  }
}
