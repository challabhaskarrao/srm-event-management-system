import { fetchApi } from './api.js';

export const getNotifications = (limit = 30) => fetchApi(`/notifications?limit=${encodeURIComponent(limit)}`);
export const getUnreadNotificationCount = () => fetchApi('/notifications/unread-count');
export const markNotificationRead = (id) => fetchApi(`/notifications/${encodeURIComponent(id)}/read`, { method: 'PATCH' });
export const markAllNotificationsRead = () => fetchApi('/notifications/read-all', { method: 'POST' });
