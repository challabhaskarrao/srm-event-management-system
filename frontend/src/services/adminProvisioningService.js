import { fetchApi } from './api.js';

export const getProvisionableFaculty = () => fetchApi('/admin/provisioning/faculty');

export const provisionFaculty = (payload) => fetchApi('/admin/provisioning/faculty', {
  method: 'POST',
  body: JSON.stringify(payload),
});
