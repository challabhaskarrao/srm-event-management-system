import { fetchApi } from './api.js';

export const getMyPortfolio = (params = {}) => {
  const query = new URLSearchParams(Object.entries(params).filter(([, value]) => value !== '' && value !== null && value !== undefined)).toString();
  return fetchApi(`/students/me/portfolio${query ? `?${query}` : ''}`);
};
