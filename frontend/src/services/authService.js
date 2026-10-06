import { fetchApi } from './api';
import { buildChangePasswordPayload, buildResetPasswordPayload } from '../utils/passwordSecurity';

export const login = async (email, password) => {
  return fetchApi('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  });
};

export const getCurrentUser = async () => fetchApi('/auth/me');

export const changePassword = async (currentPassword, newPassword) => fetchApi('/auth/change-password', {
  method: 'POST',
  body: JSON.stringify(buildChangePasswordPayload(currentPassword, newPassword)),
});

export const requestPasswordReset = async (email) => fetchApi('/auth/forgot-password', {
  method: 'POST',
  body: JSON.stringify({ email: email.trim().toLowerCase() }),
});

export const resetPassword = async (token, newPassword) => fetchApi('/auth/reset-password', {
  method: 'POST',
  body: JSON.stringify(buildResetPasswordPayload(token, newPassword)),
});

export const signupStudent = async (payload) => fetchApi('/provisioning/student/signup', { method: 'POST', body: JSON.stringify(payload) });
export const verifyAccountOtp = async (payload) => fetchApi('/provisioning/verify-otp', { method: 'POST', body: JSON.stringify(payload) });
