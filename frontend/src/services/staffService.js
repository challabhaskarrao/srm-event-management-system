import { fetchApi } from './api.js';

export const buildReviewPayload = (role, outcome, remarks = '') => {
  const normalizedRole = role?.toUpperCase();
  const normalizedOutcome = outcome?.toUpperCase();

  if (!['MENTOR', 'HOD'].includes(normalizedRole)) {
    throw new Error('Only Mentor and HOD users can submit reviews.');
  }
  if (!['APPROVE', 'RECOMMEND', 'RETURN', 'REJECT'].includes(normalizedOutcome)) {
    throw new Error('Review outcome must be APPROVE, RETURN, or REJECT.');
  }

  const status = normalizedOutcome === 'REJECT'
    ? 'REJECTED'
    : normalizedOutcome === 'RETURN'
      ? normalizedRole === 'MENTOR' ? 'RETURNED_BY_MENTOR' : 'RETURNED_BY_HOD'
      : normalizedRole === 'MENTOR' ? 'RECOMMENDED' : 'APPROVED';

  return { status, remarks: remarks.trim() };
};

export const getStaffApplications = (role) => {
  if (role === 'ADMIN') {
    return fetchApi('/applications/all');
  }
  return fetchApi(`/applications/review-queue?role=${encodeURIComponent(role)}`);
};

export const reviewApplication = (applicationId, role, outcome, remarks) => (
  fetchApi(`/applications/${applicationId}/review`, {
    method: 'PUT',
    body: JSON.stringify(buildReviewPayload(role, outcome, remarks)),
  })
);

export const getStaffAchievements = (role) => fetchApi(`/achievements/review-queue?role=${encodeURIComponent(role)}`);
export const reviewAchievement = (achievementId, outcome, remarks) => fetchApi(`/achievements/${achievementId}/verification`, {
  method: 'PUT', body: JSON.stringify({ status: outcome, remarks: remarks.trim() }),
});
export const getMentorFollowUps = (params = {}) => {
  const query = new URLSearchParams(Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== '')).toString();
  return fetchApi(`/mentor/follow-ups${query ? `?${query}` : ''}`);
};
