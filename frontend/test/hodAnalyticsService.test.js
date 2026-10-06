import test from 'node:test';
import assert from 'node:assert/strict';
import {
  buildHodAnalyticsParams,
  buildHodAnalyticsQuery,
  getHodAnalyticsFilterError,
} from '../src/services/hodAnalyticsService.js';

const monthly = { period: 'MONTHLY', month: 10, year: 2026 };

test('HOD analytics query preserves period and omits empty optional filters', () => {
  const query = buildHodAnalyticsQuery({ period: 'SEMESTER', academicYearId: 12, semester: 3, sectionId: '', status: null });
  assert.equal(query, 'period=SEMESTER&academicYearId=12&semester=3');
});

test('default monthly filter is valid and produces a request', () => {
  assert.equal(getHodAnalyticsFilterError(monthly), '');
  assert.deepEqual(buildHodAnalyticsParams(monthly), monthly);
});

test('semester waits for both academic year and semester', () => {
  assert.match(getHodAnalyticsFilterError({ period: 'SEMESTER' }), /academic year and semester/);
  assert.match(getHodAnalyticsFilterError({ period: 'SEMESTER', academicYearId: 12 }), /semester/);
  assert.equal(getHodAnalyticsFilterError({ period: 'SEMESTER', academicYearId: 12, semester: 3 }), '');
  assert.deepEqual(buildHodAnalyticsParams({ period: 'SEMESTER', academicYearId: 12, semester: 3 }), {
    period: 'SEMESTER', academicYearId: 12, semester: 3,
  });
});

test('academic year waits for the academic year and then succeeds', () => {
  assert.match(getHodAnalyticsFilterError({ period: 'ACADEMIC_YEAR' }), /academic year/);
  assert.equal(getHodAnalyticsFilterError({ period: 'ACADEMIC_YEAR', academicYearId: 12 }), '');
  assert.deepEqual(buildHodAnalyticsParams({ period: 'ACADEMIC_YEAR', academicYearId: 12 }), {
    period: 'ACADEMIC_YEAR', academicYearId: 12,
  });
});

test('custom period requires valid ordered dates', () => {
  assert.match(getHodAnalyticsFilterError({ period: 'CUSTOM' }), /both custom dates/);
  assert.match(getHodAnalyticsFilterError({ period: 'CUSTOM', fromDate: '2026-10-20', toDate: '2026-10-19' }), /on or before/);
  assert.equal(getHodAnalyticsFilterError({ period: 'CUSTOM', fromDate: '2026-10-19', toDate: '2026-10-20' }), '');
  assert.deepEqual(buildHodAnalyticsParams({ period: 'CUSTOM', fromDate: '2026-10-19', toDate: '2026-10-20' }), {
    period: 'CUSTOM', fromDate: '2026-10-19', toDate: '2026-10-20',
  });
});

test('bounded semester and numeric optional filters reject malformed values', () => {
  assert.match(getHodAnalyticsFilterError({ period: 'SEMESTER', academicYearId: 12, semester: 13 }), /1 to 12/);
  assert.match(getHodAnalyticsFilterError({ ...monthly, sectionId: 'bad' }), /section/);
  assert.match(getHodAnalyticsFilterError({ ...monthly, studentYear: 0 }), /student year/);
  assert.match(getHodAnalyticsFilterError({ ...monthly, mentorId: 'bad' }), /mentor/);
});

test('optional section, student year, event type, status, and mentor filters are preserved', () => {
  const params = buildHodAnalyticsParams({
    ...monthly, sectionId: 4, studentYear: 3, eventTypeCode: 'HACKATHON', status: 'APPROVED', mentorId: 9,
  });
  assert.deepEqual(params, {
    period: 'MONTHLY', month: 10, year: 2026, sectionId: 4, studentYear: 3,
    eventTypeCode: 'HACKATHON', status: 'APPROVED', mentorId: 9,
  });
});

test('period-specific fields do not leak into another period request', () => {
  assert.deepEqual(buildHodAnalyticsParams({
    period: 'SEMESTER', month: 10, year: 2026, academicYearId: 12, semester: 3, fromDate: '2026-10-01', toDate: '2026-10-31',
  }), { period: 'SEMESTER', academicYearId: 12, semester: 3 });
});
