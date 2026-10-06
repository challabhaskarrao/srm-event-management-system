import test from 'node:test';
import assert from 'node:assert/strict';
import { ApiError, fetchApi } from '../src/services/api.js';

const removedKeys = [];
globalThis.localStorage = {
  getItem: () => 'test-token',
  removeItem: (key) => removedKeys.push(key),
};

test('API client includes the bearer token and exposes 403 status', async () => {
  let request;
  globalThis.fetch = async (url, options) => {
    request = { url, options };
    return {
      ok: false,
      status: 403,
      statusText: 'Forbidden',
      headers: { get: () => 'application/json' },
      json: async () => ({ message: 'Role mismatch' }),
    };
  };

  await assert.rejects(fetchApi('/applications/all'), (error) => {
    assert.ok(error instanceof ApiError);
    assert.equal(error.status, 403);
    assert.equal(error.message, 'Role mismatch');
    return true;
  });
  assert.equal(request.options.headers.Authorization, 'Bearer test-token');
});

test('API client clears stored session after a 401', async () => {
  removedKeys.length = 0;
  globalThis.fetch = async () => ({
    ok: false,
    status: 401,
    statusText: 'Unauthorized',
    headers: { get: () => 'application/json' },
    json: async () => ({ message: 'Invalid token' }),
  });

  await assert.rejects(fetchApi('/auth/me'), (error) => error.status === 401);
  assert.deepEqual(removedKeys, ['token', 'user']);
});
