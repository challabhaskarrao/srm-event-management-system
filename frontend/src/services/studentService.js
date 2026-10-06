import { fetchApi } from './api.js';

export const getMyProfile = () => fetchApi('/students/me');
