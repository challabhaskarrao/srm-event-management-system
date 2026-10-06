import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { CheckCircle2, FileText, ShieldCheck, Users } from 'lucide-react';
import { createAchievement, getAchievementTypes, submitAchievementForVerification, uploadAchievementDocument } from '../../services/achievementService';
import { getDocumentTypes } from '../../services/lifecycleService';
import { getMyProfile } from '../../services/studentService';
import { achievementDocuments, achievementFields } from '../../utils/achievementFormConfig';
import { TEAM_LIMITS } from '../../utils/participationFormConfig';
import { normalizeStudentProfile } from '../../utils/studentWorkflow';

const initial = { eventTypeCode: '', title: '', organization: '', description: '', startDate: '', endDate: '', officialUrl: '', participationType: 'INDIVIDUAL', teamName: '', declarationAccepted: false };

const CreateAchievement = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState(initial);
  const [types, setTypes] = useState([]);
  const [profile, setProfile] = useState(null);
  const [documentTypes, setDocumentTypes] = useState([]);
  const [details, setDetails] = useState({});
  const [teamMembers, setTeamMembers] = useState([]);
  const [files, setFiles] = useState({});
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    Promise.all([getAchievementTypes(), getMyProfile(), getDocumentTypes()])
      .then(([loadedTypes, loadedProfile, loadedDocuments]) => { setTypes(loadedTypes || []); setProfile(normalizeStudentProfile(loadedProfile)); setDocumentTypes(loadedDocuments || []); })
      .catch((requestError) => setError(requestError.message || 'Unable to load achievement options.'))
      .finally(() => setLoading(false));
  }, []);

  const selectedType = useMemo(() => types.find((type) => type.code === form.eventTypeCode), [types, form.eventTypeCode]);
  const typeFields = achievementFields(selectedType);
  const docs = achievementDocuments(selectedType);
  const teamAllowed = Boolean(selectedType?.config?.team);

  const change = (event) => setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  const selectedDocumentType = (code) => documentTypes.find((type) => type.documentName === code || type.documentName?.replaceAll(' ', '_').toUpperCase() === code);
  const setTeamCount = (event) => setTeamMembers((current) => Array.from({ length: Number(event.target.value) }, (_, index) => current[index] || { registrationNumber: '', name: index === 0 ? profile?.name || '' : '' }));
  const selectType = (event) => { const type = types.find((item) => item.code === event.target.value); setForm((current) => ({ ...current, eventTypeCode: event.target.value, participationType: type?.config?.team ? current.participationType : 'INDIVIDUAL', teamName: '' })); setDetails({}); setFiles({}); setTeamMembers(type?.config?.team ? [{ registrationNumber: profile?.registrationNumber || '', name: profile?.name || '' }, { registrationNumber: '', name: '' }] : []); };
  const updateMember = (index, key, value) => setTeamMembers((current) => current.map((member, memberIndex) => memberIndex === index ? { ...member, [key]: value } : member));

  const validate = () => {
    if (!selectedType) return 'Select a valid achievement type.';
    if (!form.declarationAccepted) return 'Accept the declaration before submitting.';
    if (form.endDate && form.startDate && form.endDate < form.startDate) return 'End date cannot be before start date.';
    if (form.participationType === 'TEAM') {
      if (!form.teamName.trim()) return 'Team name is required.';
      if (teamMembers.length < TEAM_LIMITS.min || teamMembers.length > TEAM_LIMITS.max) return `Team size must be between ${TEAM_LIMITS.min} and ${TEAM_LIMITS.max} students.`;
      const registrations = teamMembers.map((member) => member.registrationNumber.trim().toUpperCase());
      if (registrations.some((value) => !value) || new Set(registrations).size !== registrations.length) return 'Enter unique registration numbers for every team member.';
    }
    return null;
  };

  const submit = async (event) => {
    event.preventDefault(); setError(''); const validationError = validate(); if (validationError) { setError(validationError); return; }
    setBusy(true);
    try {
      const payload = { ...form, teamName: form.participationType === 'TEAM' ? form.teamName.trim() : null, teamMemberRegistrationNumbers: form.participationType === 'TEAM' ? teamMembers.slice(1).map((member) => member.registrationNumber.trim()) : [], typeSpecificDetails: details };
      delete payload.declarationAccepted;
      const achievement = await createAchievement(payload);
      for (const definition of docs) {
        const selectedFiles = Array.isArray(files[definition.code])
          ? files[definition.code]
          : files[definition.code] ? [files[definition.code]] : [];
        if (selectedFiles.length === 0) continue;
        const type = selectedDocumentType(definition.code);
        if (!type) throw new Error(`${definition.title} is unavailable. Refresh the form and try again.`);
        for (const file of selectedFiles) await uploadAchievementDocument(achievement.achievementId, type.documentTypeId, file);
      }
      await submitAchievementForVerification(achievement.achievementId);
      navigate(`/student/achievements/${achievement.achievementId}`);
    } catch (requestError) { setError(requestError.message || 'Unable to submit achievement.'); } finally { setBusy(false); }
  };

  if (loading) return <div className="page-container"><div className="loading-spinner">Loading achievement form…</div></div>;
  return <div className="page-container participation-page">
    <header className="page-header"><div className="staff-eyebrow"><ShieldCheck size={16} /> Student achievement record</div><h1 className="page-title">Record Achievement</h1><p className="page-subtitle">Record an achievement; Mentor and HOD review the submitted record when required.</p></header>
    {error && <div className="error-alert" role="alert">{error}</div>}
    <form className="form-card glass-panel achievement-record-form" onSubmit={submit}>
      <section className="participation-section"><h2 className="section-title"><Users size={18} /> Student information</h2><div className="student-info-grid">{[['Student name', profile?.name], ['Registration number', profile?.registrationNumber], ['Department', profile?.departmentName || profile?.department], ['Section', profile?.sectionName || profile?.section], ['Student year', profile?.studentYear]].map(([label, value]) => <div key={label}><span>{label}</span><strong>{value || 'Not available'}</strong></div>)}</div><p className="participation-help">Institutional identity and routing are read-only and derived from your authenticated profile.</p></section>
      <section className="participation-section"><h2 className="section-title"><FileText size={18} /> Achievement details</h2><div className="participation-form-grid"><div className="participation-field"><label htmlFor="achievement-type">Type of Achievement *</label><select id="achievement-type" name="eventTypeCode" value={form.eventTypeCode} onChange={selectType} required><option value="">Select an achievement type</option>{types.map((type) => <option key={type.code} value={type.code}>{type.name}</option>)}</select></div><div className="participation-field"><label htmlFor="achievement-title">Title *</label><input id="achievement-title" name="title" value={form.title} onChange={change} required maxLength="220" /></div><div className="participation-field"><label htmlFor="achievement-organization">Organization / institution *</label><input id="achievement-organization" name="organization" value={form.organization} onChange={change} required maxLength="220" /></div><div className="participation-field"><label htmlFor="achievement-start">Start date</label><input id="achievement-start" type="date" name="startDate" value={form.startDate} onChange={change} /></div><div className="participation-field"><label htmlFor="achievement-end">End date / achievement date</label><input id="achievement-end" type="date" name="endDate" value={form.endDate} onChange={change} min={form.startDate} /></div><div className="participation-field"><label htmlFor="achievement-url">Official URL</label><input id="achievement-url" type="url" name="officialUrl" value={form.officialUrl} onChange={change} placeholder="https://" maxLength="500" /></div></div><div className="participation-field participation-field-wide"><label htmlFor="achievement-description">Description</label><textarea id="achievement-description" name="description" value={form.description} onChange={change} rows="4" maxLength="4000" /></div></section>
      {selectedType && typeFields.length > 0 && <section className="participation-section"><h2 className="section-title"><FileText size={18} /> Type-specific details</h2><div className="participation-form-grid">{typeFields.map(([name, label, required]) => <div className="participation-field" key={name}><label htmlFor={`achievement-${name}`}>{label}{required && ' *'}</label><input id={`achievement-${name}`} name={name} value={details[name] || ''} onChange={(event) => setDetails((current) => ({ ...current, [name]: event.target.value }))} required={required} type={name.toLowerCase().includes('date') ? 'date' : name === 'durationHours' || name === 'score' || name === 'stipendAmount' ? 'number' : 'text'} /></div>)}</div></section>}
      {teamAllowed && <section className="participation-section"><h2 className="section-title"><Users size={18} /> Participation and team</h2><div className="participation-form-grid"><div className="participation-field"><label htmlFor="achievement-participation">Participation *</label><select id="achievement-participation" name="participationType" value={form.participationType} onChange={change}><option>INDIVIDUAL</option><option>TEAM</option></select></div>{form.participationType === 'TEAM' && <><div className="participation-field"><label htmlFor="achievement-team-name">Team name *</label><input id="achievement-team-name" name="teamName" value={form.teamName} onChange={change} required /></div><div className="participation-field"><label htmlFor="achievement-team-count">Total team members *</label><select id="achievement-team-count" value={teamMembers.length || TEAM_LIMITS.min} onChange={setTeamCount}>{Array.from({ length: TEAM_LIMITS.max - TEAM_LIMITS.min + 1 }, (_, index) => <option key={index} value={index + TEAM_LIMITS.min}>{index + TEAM_LIMITS.min} members</option>)}</select></div></>}</div>{form.participationType === 'TEAM' && <div className="team-builder">{teamMembers.map((member, index) => <article className="team-member-row team-member-card" key={index}><strong>{index === 0 ? 'Member 1 — Team Leader' : `Member ${index + 1}`}</strong><label htmlFor={`achievement-member-${index}`}>Registration number *</label><input id={`achievement-member-${index}`} value={member.registrationNumber} readOnly={index === 0} onChange={(event) => updateMember(index, 'registrationNumber', event.target.value)} required /><small>{index === 0 ? `${member.name || 'Authenticated Student'} · verified profile` : 'Must match an existing Student record'}</small></article>)}</div>}</section>}
      <section className="participation-section"><h2 className="section-title"><FileText size={18} /> Supporting evidence</h2><p className="participation-help">PDF, PNG, or JPG/JPEG only · maximum 5 MB per file. Evidence uploads after the record is saved and before this page opens.</p><div className="document-upload-grid">{docs.map((definition) => { const selectedFiles = Array.isArray(files[definition.code]) ? files[definition.code] : files[definition.code] ? [files[definition.code]] : []; return <div className="document-upload-card" key={definition.code}><label htmlFor={`achievement-document-${definition.code}`}>{definition.title}</label><input id={`achievement-document-${definition.code}`} type="file" multiple={definition.multiple} accept=".pdf,.png,.jpg,.jpeg,application/pdf,image/png,image/jpeg" onChange={(event) => setFiles((current) => ({ ...current, [definition.code]: definition.multiple ? Array.from(event.target.files || []) : event.target.files?.[0] || null }))} /><small>{selectedFiles.length > 0 ? selectedFiles.map((file) => file.name).join(', ') : 'No file selected'}</small></div>; })}</div></section>
      <section className="participation-section"><h2 className="section-title"><CheckCircle2 size={18} /> Declaration</h2><label className="checkbox-label declaration-check" htmlFor="achievement-declaration"><input id="achievement-declaration" type="checkbox" checked={form.declarationAccepted} onChange={(event) => setForm((current) => ({ ...current, declarationAccepted: event.target.checked }))} /><span>I confirm that the information and supporting evidence provided is accurate.</span></label></section>
      <div className="form-actions participation-form-actions"><button type="button" className="btn btn-secondary" onClick={() => navigate('/student/dashboard')} disabled={busy}>Cancel</button><button type="submit" className="btn btn-primary" disabled={busy || !selectedType}>{busy ? 'Saving & submitting…' : 'Save & Submit for Verification'}</button></div>
    </form>
  </div>;
};

export default CreateAchievement;
