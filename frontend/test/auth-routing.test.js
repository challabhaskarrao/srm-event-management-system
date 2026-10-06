import test from 'node:test';
import assert from 'node:assert/strict';
import { canAccessRole, homeForRole, isStaffRole } from '../src/utils/authRouting.js';
import { buildReviewPayload } from '../src/services/staffService.js';

test('authenticated roles route to their own portal', () => {
  assert.equal(homeForRole('STUDENT'), '/student/dashboard');
  assert.equal(homeForRole('MENTOR'), '/staff/mentor');
  assert.equal(homeForRole('HOD'), '/staff/hod');
  assert.equal(homeForRole('ADMIN'), '/staff/admin');
});

test('student role is rejected from the staff role set', () => {
  assert.equal(isStaffRole('STUDENT'), false);
  assert.equal(isStaffRole('MENTOR'), true);
  assert.equal(isStaffRole('HOD'), true);
  assert.equal(isStaffRole('ADMIN'), true);
});

test('each staff route grants only its exact backend-authenticated role', () => {
  assert.equal(canAccessRole('MENTOR', ['MENTOR']), true);
  assert.equal(canAccessRole('HOD', ['MENTOR']), false);
  assert.equal(canAccessRole('ADMIN', ['MENTOR']), false);
  assert.equal(canAccessRole('STUDENT', ['MENTOR']), false);
  assert.equal(canAccessRole('HOD', ['HOD']), true);
  assert.equal(canAccessRole('ADMIN', ['ADMIN']), true);
});

test('Mentor and HOD positive review payloads use backend contract statuses', () => {
  assert.deepEqual(buildReviewPayload('MENTOR', 'APPROVE', 'Verified'), {
    status: 'RECOMMENDED',
    remarks: 'Verified',
  });
  assert.deepEqual(buildReviewPayload('HOD', 'APPROVE', 'Approved'), {
    status: 'APPROVED',
    remarks: 'Approved',
  });
});

test('rejections use REJECTED and preserve trimmed remarks', () => {
  assert.deepEqual(buildReviewPayload('MENTOR', 'REJECT', '  Missing proof  '), {
    status: 'REJECTED',
    remarks: 'Missing proof',
  });
});

test('correction reviews use distinct backend statuses and require the UI caller to provide remarks', () => {
  assert.deepEqual(buildReviewPayload('MENTOR', 'RETURN', '  Correct the event date  '), {
    status: 'RETURNED_BY_MENTOR',
    remarks: 'Correct the event date',
  });
  assert.deepEqual(buildReviewPayload('HOD', 'RETURN', 'Attach the official confirmation'), {
    status: 'RETURNED_BY_HOD',
    remarks: 'Attach the official confirmation',
  });
});
