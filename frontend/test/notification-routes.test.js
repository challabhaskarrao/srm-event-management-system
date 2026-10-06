import test from 'node:test';
import assert from 'node:assert/strict';
import { isSafeNotificationPath, resolveNotificationPath } from '../src/utils/notificationRoutes.js';

const notification = (actionPath, recordType, recordId) => ({ actionPath, recordType, recordId });

test('mentor application notifications open the actual Mentor workspace at the application', () => {
  assert.equal(resolveNotificationPath(notification('/staff/mentor', 'APPLICATION', 123)), '/staff/mentor?applicationId=123');
  assert.equal(resolveNotificationPath(notification('/staff/mentor', 'APPLICATION', 456)), '/staff/mentor?applicationId=456');
});

test('HOD application notifications open the actual HOD workspace at the application', () => {
  assert.equal(resolveNotificationPath(notification('/staff/hod', 'APPLICATION', 123)), '/staff/hod?applicationId=123');
});

test('student application and achievement notifications preserve their actual detail routes', () => {
  assert.equal(resolveNotificationPath(notification('/student/applications/PAR-123', 'APPLICATION', 123)), '/student/applications/PAR-123');
  assert.equal(resolveNotificationPath(notification('/student/achievements/77', 'ACHIEVEMENT', 77)), '/student/achievements/77');
});

test('mentor and HOD achievement notifications focus the appropriate workspace record', () => {
  assert.equal(resolveNotificationPath(notification('/staff/mentor', 'ACHIEVEMENT', 77)), '/staff/mentor?achievementId=77');
  assert.equal(resolveNotificationPath(notification('/staff/hod', 'ACHIEVEMENT', 77)), '/staff/hod?achievementId=77');
});

test('weekly Mentor follow-up opens the existing Follow-Up Centre workspace', () => {
  assert.equal(resolveNotificationPath(notification('/staff/mentor', 'MENTOR_FOLLOW_UP', 41)), '/staff/mentor');
});

test('safe notification paths accept internal routes and reject external or traversal paths', () => {
  assert.equal(isSafeNotificationPath('/staff/mentor?applicationId=123'), true);
  assert.equal(isSafeNotificationPath('/student/applications/PAR-123'), true);
  assert.equal(isSafeNotificationPath('http://evil.example'), false);
  assert.equal(isSafeNotificationPath('https://evil.example'), false);
  assert.equal(isSafeNotificationPath('//evil.example'), false);
  assert.equal(isSafeNotificationPath('javascript:alert(1)'), false);
  assert.equal(isSafeNotificationPath('/staff/../admin'), false);
});
