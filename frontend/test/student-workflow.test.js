import test from 'node:test';
import assert from 'node:assert/strict';
import {
  applicationDetailPath,
  buildApplicationPayload,
  buildEventPayload,
  normalizeEventList,
  normalizeStudentProfile,
  postEventActionLabel,
  postEventStatusLabel,
  statusLabel,
} from '../src/utils/studentWorkflow.js';

test('event list keeps only real backend event records', () => {
  assert.deepEqual(normalizeEventList([
    { eventId: 5, eventName: 'Hackathon' },
    null,
    { eventName: 'Missing ID' },
  ]), [{ eventId: 5, eventName: 'Hackathon' }]);
  assert.deepEqual(normalizeEventList({ content: [] }), []);
});

test('application payload matches RegisterApplicationRequestDTO exactly', () => {
  assert.deepEqual(buildApplicationPayload('7', {
    participationType: 'TEAM',
    teamName: '  Innovators  ',
    teamMemberRegistrationNumbers: ' RA1, RA2 ',
    travelRequired: true,
    outsideState: false,
  }), {
    eventId: 7,
    participationType: 'TEAM',
    travelRequired: true,
    outsideState: false,
    teamName: 'Innovators',
    teamMemberRegistrationNumbers: ['RA1', 'RA2'],
  });
});

test('admin event payload contains only supported event fields', () => {
  assert.deepEqual(buildEventPayload({
    name: ' Event ', categoryId: '3', organizer: ' SRM ', startDate: '2030-01-01',
    endDate: '2030-01-02', mode: 'HYBRID', venue: ' Hall ', eventUrl: '',
  }), {
    name: 'Event', categoryId: 3, organizer: 'SRM', startDate: '2030-01-01',
    endDate: '2030-01-02', mode: 'HYBRID', venue: 'Hall', eventUrl: null,
  });
});

test('individual applications do not send a team name', () => {
  assert.equal(buildApplicationPayload(9, {
    participationType: 'INDIVIDUAL',
    teamName: 'ignored',
    travelRequired: false,
    outsideState: false,
  }).teamName, null);
});

test('profile response is normalized without fabricated fallback data', () => {
  assert.deepEqual(normalizeStudentProfile({
    name: 'Student One', departmentCode: 'CSE', sectionName: 'A', semester: 5,
  }), {
    name: 'Student One', registrationNumber: null, email: null,
    department: 'CSE', departmentCode: 'CSE', section: 'A',
    academicYear: null, studentYear: null, semester: 5, batch: null,
  });
});

test('submission result maps to the owned application detail route', () => {
  assert.equal(applicationDetailPath({ applicationCode: 'APP-2026-TEST' }), '/student/applications/APP-2026-TEST');
  assert.throws(() => applicationDetailPath({}), /application code/);
});

test('workflow statuses have accurate student-facing labels', () => {
  assert.equal(statusLabel('SUBMITTED'), 'Submitted — awaiting Mentor review');
  assert.equal(statusLabel('PENDING_HOD_APPROVAL'), 'Mentor recommended — awaiting HOD review');
  assert.equal(statusLabel('RETURNED_BY_MENTOR'), 'Returned by Mentor for correction');
  assert.equal(statusLabel('RETURNED_BY_HOD'), 'Returned by HOD for correction');
  assert.equal(statusLabel('REJECTED_BY_HOD'), 'Rejected by HOD');
});

test('post-activity statuses remain visible and actionable', () => {
  assert.equal(postEventStatusLabel({ status: 'APPROVED', postEventStatus: 'NOT_DUE' }), 'Approved — Event Upcoming');
  assert.equal(postEventStatusLabel({ status: 'APPROVED', postEventStatus: 'PENDING' }), 'Post-Activity Report Pending');
  assert.equal(postEventStatusLabel({ status: 'APPROVED', postEventStatus: 'SUBMITTED' }), 'Evidence Incomplete');
  assert.equal(postEventStatusLabel({ status: 'APPROVED', postEventStatus: 'COMPLETE' }), 'Completed');
  assert.equal(postEventActionLabel({ postEventStatus: 'PENDING' }), 'Submit Participation Details');
  assert.equal(postEventActionLabel({ postEventStatus: 'SUBMITTED' }), 'Complete Evidence');
});
