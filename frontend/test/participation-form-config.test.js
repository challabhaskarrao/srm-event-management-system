import test from 'node:test';
import assert from 'node:assert/strict';
import { documentDefinitions, supportsTeam, supportsTravel, TEAM_LIMITS, typeSpecificFields } from '../src/utils/participationFormConfig.js';

const type = (kind, config = {}) => ({ code: kind, name: kind, config: { kind, ...config } });

test('event type configuration controls team and travel sections', () => {
  assert.equal(supportsTeam(type('EVENT', { team: true })), true);
  assert.equal(supportsTeam(type('COURSE', { team: false })), false);
  assert.equal(supportsTravel(type('EVENT', { travel: true }), 'OFFLINE'), true);
  assert.equal(supportsTravel(type('EVENT', { travel: true }), 'ONLINE'), false);
});

test('team range is shared and document requirements are type-aware', () => {
  assert.deepEqual(TEAM_LIMITS, { min: 2, max: 10 });
  const docs = documentDefinitions(type('EVENT', { documents: ['EVENT_POSTER', 'REGISTRATION_PROOF'] }), true);
  assert.equal(docs.find((doc) => doc.code === 'EVENT_POSTER').required, true);
  assert.ok(docs.some((doc) => doc.code === 'TRAVEL_TICKET'));
});

test('type-specific fields adapt without duplicating whole forms', () => {
  assert.ok(typeSpecificFields(type('COURSE')).some(([name]) => name === 'durationHours'));
  assert.ok(typeSpecificFields(type('INTERNSHIP', { mode: 'OFFLINE' })).some(([name, , required]) => name === 'location' && required));
  assert.equal(typeSpecificFields(type('EVENT')).length, 0);
});
