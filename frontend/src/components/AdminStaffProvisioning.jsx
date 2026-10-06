import React, { useCallback, useEffect, useState } from 'react';
import { ShieldCheck, UserPlus } from 'lucide-react';
import { getProvisionableFaculty, provisionFaculty } from '../services/adminProvisioningService';
import { PASSWORD_REQUIREMENTS, validateNewPassword } from '../utils/passwordSecurity';

const AdminStaffProvisioning = () => {
  const [options, setOptions] = useState([]);
  const [registryId, setRegistryId] = useState('');
  const [email, setEmail] = useState('');
  const [temporaryPassword, setTemporaryPassword] = useState('');
  const [confirmTemporaryPassword, setConfirmTemporaryPassword] = useState('');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [busy, setBusy] = useState(false);

  const load = useCallback(async () => {
    try { setOptions(await getProvisionableFaculty()); } catch (requestError) { setError(requestError.message || 'Unable to load faculty registry options.'); }
  }, []);
  useEffect(() => { load(); }, [load]);

  const submit = async (event) => {
    event.preventDefault();
    const passwordError = validateNewPassword(temporaryPassword, confirmTemporaryPassword);
    if (!registryId || !email.trim() || passwordError) { setError(passwordError || 'Select a faculty registry entry and provide an email.'); return; }
    setBusy(true); setError(''); setMessage('');
    try {
      const result = await provisionFaculty({ registryId: Number(registryId), email: email.trim(), temporaryPassword, confirmTemporaryPassword });
      setMessage(`${result.message} ${result.facultyCode} (${result.role}) can now sign in through the Staff Portal.`);
      setRegistryId(''); setEmail(''); setTemporaryPassword(''); setConfirmTemporaryPassword('');
      await load();
    } catch (requestError) { setError(requestError.message || 'Unable to provision the staff account.'); }
    finally { setBusy(false); }
  };

  const selected = options.find((option) => String(option.registryId) === registryId);
  return <section className="admin-events glass-panel"><h2 className="section-title"><UserPlus size={19} /> Provision staff account</h2><p className="muted-text"><ShieldCheck size={15} /> Role, faculty code, name, and department come from the selected eligible faculty registry entry.</p>{error && <div className="error-alert" role="alert">{error}</div>}{message && <div className="recovery-message" role="status">{message}</div>}<form className="admin-event-form" onSubmit={submit}><select value={registryId} onChange={(event) => setRegistryId(event.target.value)} required><option value="">Select eligible faculty</option>{options.map((option) => <option key={option.registryId} value={option.registryId}>{option.facultyCode} · {option.facultyName} · {option.role}</option>)}</select><input type="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="Staff email" autoComplete="off" required /><input type="password" value={temporaryPassword} onChange={(event) => setTemporaryPassword(event.target.value)} placeholder="Temporary password" autoComplete="new-password" required /><input type="password" value={confirmTemporaryPassword} onChange={(event) => setConfirmTemporaryPassword(event.target.value)} placeholder="Confirm temporary password" autoComplete="new-password" required /><small>{PASSWORD_REQUIREMENTS} The temporary credential is not stored in plaintext; the staff member should change it after first sign-in.</small><div className="form-actions"><button className="btn btn-primary" disabled={busy || !selected}>{busy ? 'Provisioning…' : 'Provision staff account'}</button></div></form></section>;
};

export default AdminStaffProvisioning;
