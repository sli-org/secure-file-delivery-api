import { APIRequestContext, APIResponse, test } from '@playwright/test';

interface ApiCallRecord {
  method: string;
  url: string;
  status: number;
  statusText: string;
  requestData?: any;
  responseBody?: any;
  timestamp: number;
  duration: number;
}

export class ApiDebugHelper {
  private static apiCalls: ApiCallRecord[] = [];

  private static async captureApiCall(
    method: string, 
    url: string, 
    response: APIResponse, 
    requestData?: any, 
    duration?: number
  ): Promise<void> {
    try {
      const responseBody = await response.text();
      let parsedBody;
      try { parsedBody = JSON.parse(responseBody); } catch { parsedBody = responseBody; }

      this.apiCalls.push({
        method,
        url,
        status: response.status(),
        statusText: response.statusText(),
        requestData,
        responseBody: parsedBody,
        timestamp: Date.now(),
        duration: duration || 0
      });
    } catch {}
  }

  static attachDebugData(): void {
    const testInfo = test.info();
    if (this.apiCalls.length > 0) {
      this.apiCalls.forEach((call, index) => {
        const callNumber = (index + 1).toString().padStart(2, '0');
        const statusIcon = call.status >= 400 ? '!' : '+';
        const attachmentName = `${callNumber}-${statusIcon}-${call.method}-${call.url.split('?')[0]} (${call.duration}ms)`;
        
        testInfo.attach(attachmentName, {
          body: JSON.stringify({
            request: { method: call.method, url: call.url, data: call.requestData },
            response: { status: call.status, body: call.responseBody },
            duration: `${call.duration}ms`
          }, null, 2),
          contentType: 'application/json'
        });
      });
    }
    this.apiCalls = [];
  }

  static reset(): void { this.apiCalls = []; }

  static async post(request: APIRequestContext, url: string, options: any = {}): Promise<APIResponse> {
    const startTime = Date.now();
    const response = await request.post(url, options);
    await this.captureApiCall('POST', url, response, options.data, Date.now() - startTime);
    return response;
  }

  static async get(request: APIRequestContext, url: string, options: any = {}): Promise<APIResponse> {
    const startTime = Date.now();
    const response = await request.get(url, options);
    await this.captureApiCall('GET', url, response, undefined, Date.now() - startTime);
    return response;
  }

  static async put(request: APIRequestContext, url: string, options: any = {}): Promise<APIResponse> {
    const startTime = Date.now();
    const response = await request.put(url, options);
    await this.captureApiCall('PUT', url, response, options.data, Date.now() - startTime);
    return response;
  }

  static async delete(request: APIRequestContext, url: string, options: any = {}): Promise<APIResponse> {
    const startTime = Date.now();
    const response = await request.delete(url, options);
    await this.captureApiCall('DELETE', url, response, undefined, Date.now() - startTime);
    return response;
  }
}
