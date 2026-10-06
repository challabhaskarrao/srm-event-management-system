import test from 'node:test';
import assert from 'node:assert/strict';
import { achievementDocuments, achievementFields, isAchievementType } from '../src/utils/achievementFormConfig.js';

const type = (code, kind, team = false, mode) => ({ code, workflowCategory: 'ACHIEVEMENT', config: { kind, team, ...(mode ? { mode } : {}) } });

test('achievement navigation separates achievement types from activities', () => {
  assert.equal(isAchievementType(type('PATENT', 'PATENT')), true);
  assert.equal(isAchievementType({ code: 'HACKATHON', workflowCategory: 'ACTIVITY' }), false);
});

test('achievement fields are type-specific and do not add travel fields', () => {
  const nptel = achievementFields(type('NPTEL_COURSE', 'COURSE'));
  const offlineInternship = achievementFields(type('INTERNSHIP_STIPEND_OFFLINE', 'INTERNSHIP', false, 'OFFLINE'));
  assert.ok(nptel.some(([name]) => name === 'durationHours'));
  assert.ok(offlineInternship.some(([name]) => name === 'organizationName'));
  assert.equal(nptel.some(([name]) => name === 'departureAt'), false);
});

test('team applicability stays driven by event type configuration', () => {
  assert.equal(type('PATENT', 'PATENT', true).config.team, true);
  assert.equal(type('NPTEL_COURSE', 'COURSE', false).config.team, false);
});

test('achievement evidence always has a secure supporting-document fallback and supports multiple files', () => {
  const documents = achievementDocuments(type('PATENT', 'PATENT', true));
  assert.ok(documents.some((document) => document.code === 'ADDITIONAL_SUPPORTING_DOCUMENT' && document.multiple));
  assert.equal(achievementDocuments(type('CUSTOM', 'CUSTOM'))[0].code, 'ADDITIONAL_SUPPORTING_DOCUMENT');
});
