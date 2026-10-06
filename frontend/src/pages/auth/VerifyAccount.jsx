import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { verifyAccountOtp } from '../../services/authService';

export default function VerifyAccount() {
  const location = useLocation(); const context = location.state || {};
  const [otp, setOtp] = useState(''); const [error, setError] = useState(''); const [done, setDone] = useState(false); const [busy, setBusy] = useState(false);
  const submit = async (event) => { event.preventDefault(); setError(''); setBusy(true); try { await verifyAccountOtp({ registryType: context.registryType, identifier: context.identifier, otp }); setDone(true); } catch (e) { setError(e.message || 'The verification code is invalid or expired.'); } finally { setBusy(false); } };
  if (context.registryType !== 'STUDENT' || !context.identifier) return <div className="auth-container"><div className="auth-card glass-panel"><h1 className="auth-title">Verification session unavailable</h1><p className="auth-subtitle">Staff accounts are provisioned by the institution.</p><Link className="btn btn-primary btn-block" to="/staff/login">Return to staff login</Link></div></div>;
  return <div className="auth-container"><div className="auth-card glass-panel"><h1 className="auth-title">Verify your email</h1><p className="auth-subtitle">Enter the six-digit code sent to the email you provided. Codes expire after 10 minutes and can only be used once.</p>{error && <div className="error-alert" role="alert">{error}</div>}{done ? <div className="recovery-message" role="status"><p>Account verified and activated. You can now sign in.</p><Link className="btn btn-primary btn-block" to="/login">Continue to login</Link></div> : <form className="auth-form" onSubmit={submit}><div className="form-group"><label htmlFor="account-otp">Verification code</label><input id="account-otp" inputMode="numeric" pattern="[0-9]{6}" maxLength="6" value={otp} onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))} required /></div><button className="btn btn-primary btn-block" disabled={busy || otp.length !== 6}>{busy ? 'Verifying…' : 'Verify email'}</button></form>}</div></div>;
}
