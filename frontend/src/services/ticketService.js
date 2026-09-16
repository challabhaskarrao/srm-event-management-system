import { fetchApi } from './api';

export const registerApplication = async (payload) => {
  return await fetchApi('/applications/register', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
};

export const getMyTickets = async () => {
  return await fetchApi('/applications/my');
};

export const getTicketByCode = async (code) => {
  return await fetchApi(`/applications/${code}`);
};
