import React, { useState } from 'react';
import { KeyRound } from 'lucide-react';
import { Link } from 'react-router-dom';
import { requestPasswordReset } from '../../services/authService';
import { recoveryDeliveryNotice } from '../../utils/passwordSecurity';

const ForgotPassword = () => {
  const [email, setEmail] = useState('');
  const [response, setResponse] = useState(null);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const submit = async (event) => {
    event.preventDefault(); setBusy(true); setError('');
    try { setResponse(await requestPasswordReset(email)); }
    catch (requestError) { setError(requestError.message || 'Unable to process the recovery request.'); }
    finally { setBusy(false); }
  };

  return (
    <div className="auth-container">
      <div className="auth-card glass-panel">
        <div className="text-center"><KeyRound size={44} className="account-info-icon" /><h1 className="auth-title">Reset your password</h1><p className="auth-subtitle">Enter the email associated with your provisioned account.</p></div>
        {error && <div className="error-alert" role="alert">{error}</div>}
        {response ? <div className="recovery-message" role="status"><p>{response.message}</p>{recoveryDeliveryNotice(response.deliveryConfigured) && <p><strong>Development limitation:</strong> {recoveryDeliveryNotice(response.deliveryConfigured)}</p>}</div> : (
          <form className="auth-form" onSubmit={submit}><div className="form-group"><label htmlFor="recovery-email">SRM Email ID</label><input id="recovery-email" type="email" autoComplete="email" value={email} onChange={(event) => setEmail(event.target.value)} required /></div><button className="btn btn-primary btn-block" disabled={busy}>{busy ? 'Submitting...' : 'Request password reset'}</button></form>
        )}
        <div className="auth-footer text-center"><Link to="/login" className="auth-link">Student login</Link><span> · </span><Link to="/staff/login" className="auth-link">Staff login</Link></div>
      </div>
    </div>
  );
};

export default ForgotPassword;
