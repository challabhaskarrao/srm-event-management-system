import test from 'node:test';
import assert from 'node:assert/strict';
import {
  buildChangePasswordPayload, buildResetPasswordPayload, recoveryDeliveryNotice, validateNewPassword,
} from '../src/utils/passwordSecurity.js';

test('password change and reset payloads contain only required security fields', () => {
  assert.deepEqual(buildChangePasswordPayload('CurrentPassword1', 'NewPassword123'), {
    currentPassword: 'CurrentPassword1', newPassword: 'NewPassword123',
  });
  assert.deepEqual(buildResetPasswordPayload('opaque-token', 'NewPassword123'), {
    token: 'opaque-token', newPassword: 'NewPassword123',
  });
});

test('frontend mirrors backend password policy and confirmation checks', () => {
  assert.equal(validateNewPassword('NewPassword123', 'NewPassword123'), '');
  assert.match(validateNewPassword('weak', 'weak'), /at least 12/i);
  assert.match(validateNewPassword('NewPassword123', 'DifferentPassword1'), /confirmation/i);
});

test('recovery UI accurately reports unavailable delivery without claiming email was sent', () => {
  assert.match(recoveryDeliveryNotice(false), /not configured/i);
  assert.equal(recoveryDeliveryNotice(true), '');
});
