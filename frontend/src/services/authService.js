import { fetchApi } from './api';

export const login = async (email, password) => {
  return await fetchApi('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  });
};

export const register = async (userData) => {
  return await fetchApi('/auth/register', {
    method: 'POST',
    body: JSON.stringify(userData),
  });
};
