export const PASSWORD_REQUIREMENTS = 'Use at least 12 characters with uppercase, lowercase, and a number.';

export const validateNewPassword = (password, confirmation) => {
  if (password.length < 12 || !/[A-Z]/.test(password) || !/[a-z]/.test(password) || !/\d/.test(password)) {
    return PASSWORD_REQUIREMENTS;
  }
  if (new TextEncoder().encode(password).length > 72) return 'Password must not exceed 72 UTF-8 bytes.';
  if (password !== confirmation) return 'Password confirmation does not match.';
  return '';
};

export const buildChangePasswordPayload = (currentPassword, newPassword) => ({ currentPassword, newPassword });
export const buildResetPasswordPayload = (token, newPassword) => ({ token, newPassword });
export const recoveryDeliveryNotice = (deliveryConfigured) => deliveryConfigured
  ? ''
  : 'Institutional email delivery is not configured, so no email can currently be delivered.';
