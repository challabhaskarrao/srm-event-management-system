import React, { useState } from 'react';
import { KeyRound, ShieldCheck } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { changePassword } from '../../services/authService';
import { PASSWORD_REQUIREMENTS, validateNewPassword } from '../../utils/passwordSecurity';
import { isStaffRole } from '../../utils/authRouting';

const AccountSecurity = () => {
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmation, setConfirmation] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const { role, logout } = useAuth();
  const navigate = useNavigate();

  const submit = async (event) => {
    event.preventDefault();
    const validation = validateNewPassword(newPassword, confirmation);
    if (validation) { setError(validation); return; }
    setBusy(true); setError('');
    try {
      await changePassword(currentPassword, newPassword);
      const loginPath = isStaffRole(role) ? '/staff/login' : '/login';
      logout();
      navigate(loginPath, { replace: true, state: { message: 'Password changed. Sign in again with your new password.' } });
    } catch (requestError) {
      setError(requestError.message || 'Unable to change password.');
    } finally { setBusy(false); }
  };

  return (
    <div className="page-container account-security-page">
      <section className="glass-panel account-security-card">
        <div className="account-security-heading"><ShieldCheck size={28} /><div><h1>Account security</h1><p>Change the password for your authenticated account.</p></div></div>
        {error && <div className="error-alert" role="alert">{error}</div>}
        <form className="auth-form" onSubmit={submit}>
          <div className="form-group"><label htmlFor="current-password">Current password</label><input id="current-password" type="password" autoComplete="current-password" value={currentPassword} onChange={(event) => setCurrentPassword(event.target.value)} required /></div>
          <div className="form-group"><label htmlFor="new-password">New password</label><input id="new-password" type="password" autoComplete="new-password" value={newPassword} onChange={(event) => setNewPassword(event.target.value)} required /><small>{PASSWORD_REQUIREMENTS}</small></div>
          <div className="form-group"><label htmlFor="confirm-password">Confirm new password</label><input id="confirm-password" type="password" autoComplete="new-password" value={confirmation} onChange={(event) => setConfirmation(event.target.value)} required /></div>
          <button className="btn btn-primary btn-block" disabled={busy}><KeyRound size={17} /> {busy ? 'Changing password...' : 'Change password'}</button>
        </form>
      </section>
    </div>
  );
};

export default AccountSecurity;
