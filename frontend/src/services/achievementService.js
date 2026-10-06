import { fetchApi } from './api.js';

export const getAchievementTypes = async () => {
  const types = await fetchApi('/event-types');
  return (types || []).filter((type) => type.workflowCategory === 'ACHIEVEMENT');
};

export const getMyAchievements = () => fetchApi('/achievements/my');
export const getAchievement = (achievementId) => fetchApi(`/achievements/${achievementId}`);
export const createAchievement = (payload) => fetchApi('/achievements', { method: 'POST', body: JSON.stringify(payload) });
export const submitAchievementForVerification = (achievementId) => fetchApi(`/achievements/${achievementId}/submit`, { method: 'POST' });
export const getAchievementDocuments = (achievementId) => fetchApi(`/achievements/${achievementId}/documents`);
export const uploadAchievementDocument = (achievementId, documentTypeId, file) => {
  const data = new FormData();
  data.append('file', file);
  return fetchApi(`/achievements/${achievementId}/documents?documentTypeId=${encodeURIComponent(documentTypeId)}`, {
    method: 'POST', body: data,
  });
};
