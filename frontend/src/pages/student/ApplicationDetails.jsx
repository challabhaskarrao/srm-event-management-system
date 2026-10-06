import React, { useEffect, useState } from 'react';
import { ArrowLeft, Calendar, CheckCircle2, Clock3, ExternalLink, MapPin, MessageSquare, Pencil, Users } from 'lucide-react';
import { Link, useParams } from 'react-router-dom';
import { getTicketByCode, resubmitApplication } from '../../services/ticketService';
import { submitPostEventReport } from '../../services/lifecycleService';
import { statusLabel } from '../../utils/studentWorkflow';
import ApplicationLifecyclePanel from '../../components/ApplicationLifecyclePanel';

const dateOnly = (value) => value ? new Date(`${value}T00:00:00`).toLocaleDateString() : 'Not provided';
const dateTime = (value) => value ? new Date(value).toLocaleString() : 'Not yet';

const statusClass = (status) => {
  if (status === 'APPROVED') return 'status-approved';
  if (status?.startsWith('REJECTED')) return 'status-rejected';
  if (status === 'PENDING_HOD_APPROVAL') return 'status-waitlisted';
  if (status === 'RETURNED_BY_MENTOR' || status === 'RETURNED_BY_HOD') return 'status-waitlisted';
  return 'status-submitted';
};

const ApplicationDetails = () => {
  const { code } = useParams();
  const [application, setApplication] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [editOpen, setEditOpen] = useState(false);
  const [isResubmitting, setIsResubmitting] = useState(false);
  const [editForm, setEditForm] = useState(null);
  const [isReporting, setIsReporting] = useState(false);
  const [postEventError, setPostEventError] = useState('');
  const [postEventForm, setPostEventForm] = useState({
    participationStatus: '', noParticipationReason: '', resultType: 'PARTICIPANT',
    position: '', resultTitle: '', remarks: '', reflection: '', resultUrl: '',
  });

  useEffect(() => {
    const loadApplication = async () => {
      try {
        const loaded = await getTicketByCode(code);
        setApplication(loaded);
        setEditForm({
          eventName: loaded.eventName || '', organizer: loaded.organizer || '', startDate: loaded.startDate || '',
          endDate: loaded.endDate || '', mode: loaded.eventMode || 'ONLINE', venue: loaded.venue || '',
          eventUrl: loaded.eventUrl || '', description: loaded.description || '', city: loaded.city || '',
          state: loaded.state || '', country: loaded.country || 'India', declarationAccepted: false,
        });
      } catch (requestError) {
        setError(requestError.message || 'Unable to load this application.');
      } finally {
        setIsLoading(false);
      }
    };
    loadApplication();
  }, [code]);

  if (isLoading) return <div className="loading-spinner">Loading application...</div>;
  if (!application) return <div className="page-container"><div className="error-alert" role="alert">{error || 'Application not found.'}</div><Link className="btn btn-secondary" to="/student/applications">Back to applications</Link></div>;

  const returned = ['RETURNED_BY_MENTOR', 'RETURNED_BY_HOD'].includes(application.status);
  const returnedBy = application.status === 'RETURNED_BY_MENTOR' ? 'Mentor' : 'HOD';
  const correctionRemarks = application.status === 'RETURNED_BY_MENTOR' ? application.mentorRemarks : application.hodRemarks;
  const changeEdit = (event) => setEditForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  const submitCorrection = async (event) => {
    event.preventDefault();
    setError('');
    setIsResubmitting(true);
    try {
      await resubmitApplication(application.applicationId, editForm);
      const refreshed = await getTicketByCode(code);
      setApplication(refreshed);
      setEditOpen(false);
      setEditForm((current) => ({ ...current, declarationAccepted: false }));
    } catch (requestError) {
      setError(requestError.message || 'Unable to resubmit this application.');
    } finally {
      setIsResubmitting(false);
    }
  };

  const submitPostEvent = async (event) => {
    event.preventDefault();
    setPostEventError('');
    setIsReporting(true);
    try {
      await submitPostEventReport(application.applicationId, postEventForm);
      setApplication(await getTicketByCode(code));
    } catch (requestError) {
      setPostEventError(requestError.message || 'Unable to submit the post-activity report.');
    } finally {
      setIsReporting(false);
    }
  };

  const isApprovedActivity = application.status === 'APPROVED';
  const postEventStatus = application.postEventStatus || 'NOT_DUE';
  const postEventStatusLabel = {
    NOT_DUE: 'Not due yet',
    PENDING: 'Pending',
    SUBMITTED: 'Evidence incomplete / submitted',
    COMPLETE: 'Complete',
  }[postEventStatus] || postEventStatus.replaceAll('_', ' ');

  return (
    <div className="page-container">
      <Link className="back-btn" to="/student/applications"><ArrowLeft size={16} /> Back to applications</Link>
      <header className="app-detail-header glass-panel">
        <div className="flex-between flex-wrap gap-4">
          <div><h1 className="page-title">{application.eventName}</h1><p className="page-subtitle">{application.organizer || 'Organizer not provided'}</p></div>
          <div className="text-right"><div className="application-code">{application.applicationCode}</div><span className={`status-badge ${statusClass(application.status)}`}>{statusLabel(application.status)}</span></div>
        </div>
      </header>

      {returned && <section className="glass-panel correction-notice">
        <h2 className="section-title"><MessageSquare size={18} /> Returned for Correction</h2>
        <p><strong>Returned by:</strong> {returnedBy}</p>
        <p><strong>Remarks:</strong> {correctionRemarks || 'Please review the application details and resubmit.'}</p>
        {!editOpen && <button className="btn btn-primary" type="button" onClick={() => setEditOpen(true)}><Pencil size={16} /> Edit &amp; Resubmit</button>}
      </section>}

      {returned && editOpen && editForm && <form className="glass-panel correction-form" onSubmit={submitCorrection}>
        <h2 className="section-title">Edit application details</h2>
        <p className="participation-help">Student identity, registration number, routing, and assigned Mentor remain unchanged. Existing documents remain attached.</p>
        <div className="participation-form-grid">
          <label className="participation-field">Activity / event name<input name="eventName" value={editForm.eventName} onChange={changeEdit} required /></label>
          <label className="participation-field">Organizer / institution<input name="organizer" value={editForm.organizer} onChange={changeEdit} required /></label>
          <label className="participation-field">Start date<input name="startDate" type="date" value={editForm.startDate} onChange={changeEdit} required /></label>
          <label className="participation-field">End date<input name="endDate" type="date" value={editForm.endDate} onChange={changeEdit} min={editForm.startDate} required /></label>
          <label className="participation-field">Mode<select name="mode" value={editForm.mode} onChange={changeEdit}><option>ONLINE</option><option>OFFLINE</option><option>HYBRID</option></select></label>
          <label className="participation-field">Venue<input name="venue" value={editForm.venue} onChange={changeEdit} /></label>
          <label className="participation-field">Official link<input name="eventUrl" type="url" value={editForm.eventUrl} onChange={changeEdit} placeholder="https://" /></label>
          <label className="participation-field">City<input name="city" value={editForm.city} onChange={changeEdit} /></label>
          <label className="participation-field">State<input name="state" value={editForm.state} onChange={changeEdit} /></label>
          <label className="participation-field">Country<input name="country" value={editForm.country} onChange={changeEdit} /></label>
          <label className="participation-field participation-field-wide">Description / purpose<textarea name="description" value={editForm.description} onChange={changeEdit} rows="4" required /></label>
        </div>
        <label className="checkbox-label declaration-check"><input type="checkbox" checked={editForm.declarationAccepted} onChange={(event) => setEditForm((current) => ({ ...current, declarationAccepted: event.target.checked }))} required /><span>I confirm that the corrected information is accurate.</span></label>
        <div className="form-actions"><button className="btn btn-secondary" type="button" onClick={() => setEditOpen(false)} disabled={isResubmitting}>Cancel</button><button className="btn btn-primary" disabled={isResubmitting}>{isResubmitting ? 'Resubmitting…' : 'Resubmit to Mentor'}</button></div>
      </form>}

      {isApprovedActivity && <section className="glass-panel application-outcome-panel">
        <h2 className="section-title">Post-activity report</h2>
        <p className="participation-help">
          Status: <strong>{postEventStatusLabel}</strong>.
          {postEventStatus === 'NOT_DUE' && <> Available after: <strong>{dateOnly(application.endDate)}</strong>. Submission opens after the activity ends.</>}
          {postEventStatus === 'PENDING' && ' Record whether you participated and submit the result details.'}
          {postEventStatus === 'SUBMITTED' && ' Your participation report is recorded; upload any remaining permitted evidence below.'}
        </p>
        {postEventError && <div className="error-alert" role="alert">{postEventError}</div>}
        {postEventStatus === 'PENDING' && <form className="participation-form" onSubmit={submitPostEvent}>
          <div className="participation-form-grid">
            <label className="participation-field">Did you participate?
              <select value={postEventForm.participationStatus} onChange={(event) => setPostEventForm((current) => ({ ...current, participationStatus: event.target.value }))} required>
                <option value="">Select one</option><option value="YES">Yes</option><option value="NO">No</option>
              </select>
            </label>
            {postEventForm.participationStatus === 'NO' && <label className="participation-field participation-field-wide">Reason for not participating
              <textarea value={postEventForm.noParticipationReason} onChange={(event) => setPostEventForm((current) => ({ ...current, noParticipationReason: event.target.value }))} rows="3" required />
            </label>}
            {postEventForm.participationStatus === 'YES' && <>
              <label className="participation-field">Outcome
                <select value={postEventForm.resultType} onChange={(event) => setPostEventForm((current) => ({ ...current, resultType: event.target.value }))} required>
                  {['PARTICIPANT', 'FINALIST', 'WINNER', 'RUNNER_UP', 'POSITION', 'OTHER'].map((value) => <option key={value}>{value}</option>)}
                </select>
              </label>
              {postEventForm.resultType === 'POSITION' && <label className="participation-field">Position / rank
                <input value={postEventForm.position} onChange={(event) => setPostEventForm((current) => ({ ...current, position: event.target.value }))} required />
              </label>}
              <label className="participation-field">Result title / prize details
                <input value={postEventForm.resultTitle} onChange={(event) => setPostEventForm((current) => ({ ...current, resultTitle: event.target.value }))} />
              </label>
              <label className="participation-field participation-field-wide">Remarks / reflection
                <textarea value={postEventForm.reflection} onChange={(event) => setPostEventForm((current) => ({ ...current, reflection: event.target.value }))} rows="3" />
              </label>
              <label className="participation-field participation-field-wide">Result link (optional)
                <input type="url" value={postEventForm.resultUrl} onChange={(event) => setPostEventForm((current) => ({ ...current, resultUrl: event.target.value }))} placeholder="https://" />
              </label>
            </>}
          </div>
          <div className="form-actions"><button className="btn btn-primary" disabled={isReporting}>{isReporting ? 'Submitting…' : 'Submit post-activity report'}</button></div>
        </form>}
        {postEventStatus === 'SUBMITTED' && <div className="info-grid">
          <div className="info-item"><dt className="info-label">Participation</dt><dd className="info-value">{application.participationStatus === 'NO' ? 'No' : 'Yes'}</dd></div>
          {application.outcomeNotes && <div className="info-item col-span-2"><dt className="info-label">Submitted details</dt><dd className="info-value">{application.outcomeNotes}</dd></div>}
          {application.resultUrl && <div className="info-item col-span-2"><dt className="info-label">Result link</dt><dd className="info-value"><a className="auth-link" href={application.resultUrl} target="_blank" rel="noreferrer"><ExternalLink size={14} /> Open result link</a></dd></div>}
          {application.postEventReportedAt && <div className="info-item"><dt className="info-label">Submitted</dt><dd className="info-value">{dateTime(application.postEventReportedAt)}</dd></div>}
        </div>}
        {postEventStatus === 'COMPLETE' && <div className="info-grid">
          <div className="info-item"><dt className="info-label">Participation</dt><dd className="info-value">{application.participationStatus === 'NO' ? 'No' : 'Yes'}</dd></div>
          {application.achievement?.resultType && <div className="info-item"><dt className="info-label">Outcome</dt><dd className="info-value">{application.achievement.resultType.replaceAll('_', ' ')}</dd></div>}
          {application.achievement?.position && <div className="info-item"><dt className="info-label">Position</dt><dd className="info-value">{application.achievement.position}</dd></div>}
          {application.outcomeNotes && <div className="info-item col-span-2"><dt className="info-label">Notes</dt><dd className="info-value">{application.outcomeNotes}</dd></div>}
          {application.resultUrl && <div className="info-item col-span-2"><dt className="info-label">Result link</dt><dd className="info-value"><a className="auth-link" href={application.resultUrl} target="_blank" rel="noreferrer"><ExternalLink size={14} /> Open result link</a></dd></div>}
          {application.postEventReportedAt && <div className="info-item"><dt className="info-label">Submitted</dt><dd className="info-value">{dateTime(application.postEventReportedAt)}</dd></div>}
        </div>}
      </section>}

      <div className="app-detail-grid">
        <div className="flex flex-col gap-6">
          <section className="glass-panel">
            <h2 className="section-title">Event information</h2>
            <dl className="info-grid">
              <div className="info-item"><dt className="info-label">Category</dt><dd className="info-value">{application.eventType || 'Not provided'}</dd></div>
              <div className="info-item"><dt className="info-label">Mode</dt><dd className="info-value">{application.eventMode || 'Not provided'}</dd></div>
              <div className="info-item"><dt className="info-label">Dates</dt><dd className="info-value"><Calendar size={14} /> {dateOnly(application.startDate)} – {dateOnly(application.endDate)}</dd></div>
              <div className="info-item"><dt className="info-label">Venue</dt><dd className="info-value"><MapPin size={14} /> {application.venue || 'Not provided'}</dd></div>
              {(application.city || application.state || application.country) && <div className="info-item"><dt className="info-label">Location</dt><dd className="info-value">{[application.city, application.state, application.country].filter(Boolean).join(', ')}</dd></div>}
              {application.description && <div className="info-item col-span-2"><dt className="info-label">Purpose</dt><dd className="info-value">{application.description}</dd></div>}
              {application.eventUrl && <div className="info-item col-span-2"><dt className="info-label">Reference</dt><dd><a className="auth-link" href={application.eventUrl} target="_blank" rel="noreferrer"><ExternalLink size={14} /> Open event website</a></dd></div>}
            </dl>
          </section>

          <section className="glass-panel">
            <h2 className="section-title"><MessageSquare size={18} /> Reviewer remarks</h2>
            <div className="remarks-container">
              <div className="reviewer-note"><strong>Mentor</strong><p>{application.mentorRemarks || 'No Mentor remarks have been recorded.'}</p></div>
              <div className="reviewer-note"><strong>HOD</strong><p>{application.hodRemarks || 'No HOD remarks have been recorded.'}</p></div>
            </div>
          </section>
        </div>

        <aside className="flex flex-col gap-6">
          <section className="glass-panel">
            <h2 className="section-title"><Users size={18} /> Participation</h2>
            <div className="info-item"><span className="info-label">Type</span><span className="info-value">{application.participationType}</span></div>
            {application.teamName && <div className="info-item mt-3"><span className="info-label">Team</span><span className="info-value">{application.teamName}</span></div>}
            {application.teamMembers?.length > 0 && <div className="team-member-list">{application.teamMembers.map((member) => <span key={member.studentId}>{member.name} · {member.registrationNumber}</span>)}</div>}
            <div className="info-item mt-3"><span className="info-label">Travel required</span><span className="info-value">{application.travelRequired ? 'Yes' : 'No'}</span></div>
            <div className="info-item mt-3"><span className="info-label">Outside Tamil Nadu</span><span className="info-value">{application.outsideState ? 'Yes' : 'No'}</span></div>
            {application.outsideState && <div className="travel-detail-list"><div><span>Travel</span><strong>{application.modeOfTravel || 'Not provided'}</strong></div><div><span>Destination</span><strong>{[application.destinationCity, application.destinationState, application.destinationCountry].filter(Boolean).join(', ') || 'Not provided'}</strong></div><div><span>Emergency contact</span><strong>{application.emergencyContactName ? `${application.emergencyContactName} · ${application.emergencyContactNumber || ''}` : 'Not provided'}</strong></div></div>}
          </section>

          <section className="glass-panel">
            <h2 className="section-title"><Clock3 size={18} /> Workflow history</h2>
            <div className="workflow-times">
              <p><CheckCircle2 size={15} /> Submitted: {dateTime(application.submittedAt)}</p>
              <p>Mentor reviewed: {dateTime(application.mentorReviewedAt)}</p>
              <p>HOD reviewed: {dateTime(application.hodReviewedAt)}</p>
            </div>
            {application.approvalHistory?.length > 0 && <div className="approval-history">
              {application.approvalHistory.map((entry) => <div className="approval-history-entry" key={entry.approvalActionId}>
                <strong>{entry.action.replaceAll('_', ' ')}</strong>
                <span>{entry.previousStatus} → {entry.newStatus}</span>
                <small>{dateTime(entry.actionAt)}</small>
                {entry.remarks && <p>{entry.remarks}</p>}
              </div>)}
            </div>}
          </section>
        </aside>
      </div>
      <ApplicationLifecyclePanel application={application} role="STUDENT" />
    </div>
  );
};

export default ApplicationDetails;
