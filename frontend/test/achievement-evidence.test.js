import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';

const createAchievement = readFileSync(new URL('../src/pages/student/CreateAchievement.jsx', import.meta.url), 'utf8');
const achievementService = readFileSync(new URL('../src/services/achievementService.js', import.meta.url), 'utf8');
const details = readFileSync(new URL('../src/pages/student/AchievementDetails.jsx', import.meta.url), 'utf8');
const reviewQueue = readFileSync(new URL('../src/components/AchievementReviewQueue.jsx', import.meta.url), 'utf8');
const evidence = readFileSync(new URL('../src/components/AchievementEvidence.jsx', import.meta.url), 'utf8');

test('achievement evidence picker accepts images and multiple supporting files', () => {
  assert.match(createAchievement, /accept="\.pdf,\.png,\.jpg,\.jpeg,application\/pdf,image\/png,image\/jpeg"/);
  assert.match(createAchievement, /multiple=\{definition\.multiple\}/);
  assert.match(createAchievement, /Array\.from\(event\.target\.files \|\| \[\]\)/);
});

test('achievement evidence uploads complete before navigation', () => {
  assert.ok(createAchievement.indexOf('await createAchievement(payload)') < createAchievement.indexOf('await uploadAchievementDocument'));
  assert.ok(createAchievement.indexOf('await uploadAchievementDocument') < createAchievement.indexOf('await submitAchievementForVerification'));
  assert.ok(createAchievement.indexOf('await submitAchievementForVerification') < createAchievement.indexOf('navigate(`/student/achievements/'));
  assert.match(createAchievement, /if \(!type\) throw new Error/);
  assert.match(achievementService, /\/achievements\/\$\{achievementId\}\/submit/);
});

test('student, Mentor, and HOD surfaces render the authorized evidence component', () => {
  assert.match(details, /<AchievementEvidence achievementId=\{id\} initialDocuments=\{documents\} \/>/);
  assert.match(reviewQueue, /<AchievementEvidence achievementId=\{record\.achievementId\} \/>/);
  assert.ok(evidence.includes('downloadApi(`/documents/${document.documentId}/download`)'));
  assert.match(evidence, /achievement-evidence-thumb/);
});

test('activity documents reuse the same authenticated View and Download component', () => {
  const lifecycle = readFileSync(new URL('../src/components/ApplicationLifecyclePanel.jsx', import.meta.url), 'utf8');
  assert.match(lifecycle, /<AchievementEvidence applicationId=\{application\.applicationId\}/);
  assert.match(evidence, /getDocuments\(applicationId\)/);
  assert.match(evidence, /Unable to open this document/);
});
