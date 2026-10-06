import test from 'node:test';
import assert from 'node:assert/strict';
import { buildHodAnalyticsQuery } from '../src/services/hodAnalyticsService.js';

test('portfolio and report filters omit empty values while preserving selected scope', () => {
  const query = buildHodAnalyticsQuery({ period: 'SEMESTER', academicYearId: 4, semester: 1, sectionId: '', mentorId: 9 });
  assert.equal(query, 'period=SEMESTER&academicYearId=4&semester=1&mentorId=9');
});
