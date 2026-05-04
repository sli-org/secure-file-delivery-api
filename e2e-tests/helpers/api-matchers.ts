import { expect, APIResponse } from '@playwright/test';

expect.extend({
  async toBeSuccessful(response: APIResponse) {
    const status = response.status();
    const pass = status >= 200 && status < 300;
    return {
      message: () => pass 
        ? `Expected response not to be successful, got ${status}`
        : `Expected response to be successful (2xx), got ${status}`,
      pass,
    };
  },
});

expect.extend({
  async toHaveStatus(response: APIResponse, expected: number) {
    const actual = response.status();
    const pass = actual === expected;
    return {
      message: () => pass
        ? `Expected response not to have status ${expected}`
        : `Expected status ${expected}, got ${actual}`,
      pass,
    };
  },
});

expect.extend({
  async toHaveJsonBody(response: APIResponse) {
    try {
      await response.json();
      return { message: () => `Expected response not to have JSON body`, pass: true };
    } catch (error) {
      return { message: () => `Expected valid JSON body, got error: ${error}`, pass: false };
    }
  },
});

expect.extend({
  toHaveValidationErrors(responseBody: any, expectedCount?: number) {
    if (!responseBody?.errors || !Array.isArray(responseBody.errors)) {
      return { message: () => `Expected validation errors, found none`, pass: false };
    }
    if (expectedCount !== undefined && responseBody.errors.length !== expectedCount) {
      return { 
        message: () => `Expected ${expectedCount} errors, found ${responseBody.errors.length}`, 
        pass: false 
      };
    }
    return { message: () => `Expected no validation errors`, pass: true };
  },
});

expect.extend({
  toHaveStatementId(responseBody: any) {
    const hasId = responseBody?.id && typeof responseBody.id === 'string';
    return {
      message: () => hasId 
        ? `Expected no statement ID, found ${responseBody.id}`
        : `Expected statement ID, found none`,
      pass: hasId,
    };
  },
});

expect.extend({
  toHaveErrorMessage(responseBody: any, expectedMessage?: string) {
    const hasMessage = responseBody?.message && typeof responseBody.message === 'string';
    if (!hasMessage) return { message: () => `Expected error message, found none`, pass: false };
    if (expectedMessage && !responseBody.message.includes(expectedMessage)) {
      return { 
        message: () => `Expected message to contain "${expectedMessage}", got "${responseBody.message}"`, 
        pass: false 
      };
    }
    return { message: () => `Expected no error message`, pass: true };
  },
});

expect.extend({
  toBeWithinTimeRange(actualMs: number, maxMs: number) {
    const pass = actualMs <= maxMs;
    return {
      message: () => pass
        ? `Expected operation to take more than ${maxMs}ms, took ${actualMs}ms`
        : `Expected operation within ${maxMs}ms, took ${actualMs}ms`,
      pass,
    };
  },
});

export {};
