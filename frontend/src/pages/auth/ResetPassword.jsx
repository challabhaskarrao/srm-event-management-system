import React, { useEffect, useState } from 'react';
import { KeyRound } from 'lucide-react';
import { Link, useSearchParams } from 'react-router-dom';
import { resetPassword } from '../../services/authService';
import { PASSWORD_REQUIREMENTS, validateNewPassword } from '../../utils/passwordSecurity';

const ResetPassword = () => {
  const [searchParams] = useSearchParams();
  const [token] = useState(() => searchParams.get('token') || '');
  const [password, setPassword] = useState('');
  const [confirmation, setConfirmation] = useState('');
  const [error, setError] = useState('');
  const [complete, setComplete] = useState(false);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (searchParams.has('token')) window.history.replaceState({}, '', '/reset-password');
  }, [searchParams]);

  const submit = async (event) => {
    event.preventDefault();
    if (!token) { setError('The reset link is missing its token.'); return; }
    const validation = validateNewPassword(password, confirmation);
    if (validation) { setError(validation); return; }
    setBusy(true); setError('');
    try { await resetPassword(token, password); setComplete(true); }
    catch (requestError) { setError(requestError.message || 'The reset link is invalid or expired.'); }
    finally { setBusy(false); }
  };

  return (
    <div className="auth-container"><div className="auth-card glass-panel"><div className="text-center"><KeyRound size={44} className="account-info-icon" /><h1 className="auth-title">Choose a new password</h1></div>
      {error && <div className="error-alert" role="alert">{error}</div>}
      {complete ? <div className="recovery-message" role="status"><p>Password reset completed. You can now sign in.</p><Link to="/login" className="btn btn-primary btn-block">Continue to login</Link></div> : <form className="auth-form" onSubmit={submit}><div className="form-group"><label htmlFor="reset-password">New password</label><input id="reset-password" type="password" autoComplete="new-password" value={password} onChange={(event) => setPassword(event.target.value)} required /><small>{PASSWORD_REQUIREMENTS}</small></div><div className="form-group"><label htmlFor="reset-confirmation">Confirm new password</label><input id="reset-confirmation" type="password" autoComplete="new-password" value={confirmation} onChange={(event) => setConfirmation(event.target.value)} required /></div><button className="btn btn-primary btn-block" disabled={busy}>{busy ? 'Resetting...' : 'Reset password'}</button></form>}
    </div></div>
  );
};

export default ResetPassword;
