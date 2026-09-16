import { fetchApi } from './api';

export const getEvents = async () => {
  return await fetchApi('/events');
};

export const getEventById = async (id) => {
  return await fetchApi(`/events/${id}`);
};

export const createEvent = async (payload) => {
  return await fetchApi('/events', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
};
