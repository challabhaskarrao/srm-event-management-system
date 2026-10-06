import { downloadApi, fetchApi } from './api.js';

const buildQuery = (params = {}) => new URLSearchParams(
  Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== '')
).toString();

const isPositiveInteger = (value) => value !== undefined && value !== null && value !== ''
  && Number.isInteger(Number(value)) && Number(value) > 0;

const isValidDate = (value) => {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(String(value || ''))) return false;
  const date = new Date(`${value}T00:00:00Z`);
  return !Number.isNaN(date.getTime()) && date.toISOString().slice(0, 10) === value;
};

export const getHodAnalyticsFilterError = (filters = {}) => {
  const period = filters.period || 'MONTHLY';

  if (!['MONTHLY', 'SEMESTER', 'ACADEMIC_YEAR', 'CUSTOM'].includes(period)) {
    return 'Select a supported analytics period.';
  }
  if (period === 'MONTHLY' && (!isPositiveInteger(filters.month) || Number(filters.month) > 12
    || !isPositiveInteger(filters.year) || Number(filters.year) < 2000 || Number(filters.year) > 2200)) {
    return 'Select a valid month and year to view analytics.';
  }
  if (period === 'SEMESTER' && !isPositiveInteger(filters.academicYearId)) {
    return 'Select an academic year and semester to view analytics.';
  }
  if (period === 'SEMESTER' && (!isPositiveInteger(filters.semester) || Number(filters.semester) > 12)) {
    return 'Select an institutional semester from 1 to 12 to view analytics.';
  }
  if (period === 'ACADEMIC_YEAR' && !isPositiveInteger(filters.academicYearId)) {
    return 'Select an academic year to view analytics.';
  }
  if (period === 'CUSTOM' && (!isValidDate(filters.fromDate) || !isValidDate(filters.toDate))) {
    return 'Select both custom dates to view analytics.';
  }
  if (period === 'CUSTOM' && filters.fromDate > filters.toDate) {
    return 'Custom start date must be on or before the end date.';
  }

  for (const [name, label] of [['sectionId', 'section'], ['studentYear', 'student year'], ['mentorId', 'mentor']]) {
    if (filters[name] !== undefined && filters[name] !== null && filters[name] !== '' && !isPositiveInteger(filters[name])) {
      return `Select a valid ${label}.`;
    }
  }
  return '';
};

export const buildHodAnalyticsParams = (filters = {}) => {
  const period = filters.period || 'MONTHLY';
  const params = { period };
  if (period === 'MONTHLY') Object.assign(params, { month: filters.month, year: filters.year });
  if (period === 'SEMESTER') Object.assign(params, { academicYearId: filters.academicYearId, semester: filters.semester });
  if (period === 'ACADEMIC_YEAR') Object.assign(params, { academicYearId: filters.academicYearId });
  if (period === 'CUSTOM') Object.assign(params, { fromDate: filters.fromDate, toDate: filters.toDate });
  for (const name of ['sectionId', 'studentYear', 'eventTypeCode', 'status', 'mentorId']) {
    if (filters[name] !== undefined && filters[name] !== null && filters[name] !== '') params[name] = filters[name];
  }
  return params;
};

export const getHodAnalytics = (params) => {
  const query = buildQuery(params);
  return fetchApi(`/hod/analytics${query ? `?${query}` : ''}`);
};

export const getHodAnalyticsDrilldown = (metric, params) => {
  const query = buildQuery({ ...params, metric });
  return fetchApi(`/hod/analytics/drilldown?${query}`);
};

export const downloadHodAnalyticsExport = (format, params) => {
  const query = buildQuery(params);
  return downloadApi(`/hod/analytics/export/${format}${query ? `?${query}` : ''}`);
};

export { buildQuery as buildHodAnalyticsQuery };
