import { fetchApi } from './api.js';

export const registerApplication = async (payload) => {
  return fetchApi('/applications/register', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
};

export const getMyTickets = async () => {
  return fetchApi('/applications/my');
};

export const getTicketByCode = async (code) => {
  return fetchApi(`/applications/${encodeURIComponent(code)}`);
};

export const resubmitApplication = (applicationId, payload) => fetchApi(`/participations/${applicationId}/resubmit`, {
  method: 'PUT',
  body: JSON.stringify(payload),
});
