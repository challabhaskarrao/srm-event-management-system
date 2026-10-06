import { fetchApi } from './api.js';

export const getEvents = async () => {
  return fetchApi('/events/available');
};

export const getEventById = async (id) => {
  return fetchApi(`/events/${id}`);
};

export const createEvent = async (payload) => {
  return fetchApi('/events', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
};

export const updateEvent = async (eventId, payload) => fetchApi(`/events/${eventId}`, {
  method: 'PUT',
  body: JSON.stringify(payload),
});

export const getAllEvents = async () => fetchApi('/events');
export const getEventCategories = async () => fetchApi('/events/categories');
