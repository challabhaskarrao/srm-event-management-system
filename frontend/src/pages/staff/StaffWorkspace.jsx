import React, { useCallback, useEffect, useState } from 'react';
import { CheckCircle2, ClipboardList, CornerDownLeft, RefreshCw, ShieldCheck, XCircle, BarChart3 } from 'lucide-react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { getStaffApplications, reviewApplication } from '../../services/staffService';
import AdminEvents from '../../components/AdminEvents';
import AdminStaffProvisioning from '../../components/AdminStaffProvisioning';
import ApplicationLifecyclePanel from '../../components/ApplicationLifecyclePanel';
import AchievementReviewQueue from '../../components/AchievementReviewQueue';
import MentorFollowUpCentre from '../../components/MentorFollowUpCentre';

const roleContent = {
  MENTOR: {
    title: 'Mentor Review Queue',
    description: 'Applications assigned through your active department, section, academic year, and student-year mapping.',
    approveLabel: 'Recommend to HOD',
  },
  HOD: {
    title: 'HOD Review Queue',
    description: 'Mentor-recommended applications from students in your department.',
    approveLabel: 'Approve Application',
  },
  ADMIN: {
    title: 'Application Overview',
    description: 'Read-only visibility across all event applications.',
  },
};

const statusClass = (status) => {
  if (status === 'APPROVED') return 'status-approved';
  if (status?.startsWith('REJECTED')) return 'status-rejected';
  if (status === 'PENDING_HOD_APPROVAL') return 'status-waitlisted';
  if (status?.startsWith('RETURNED_BY_')) return 'status-waitlisted';
  if (status === 'SUBMITTED') return 'status-submitted';
  return 'status-default';
};

const formatDate = (date) => date ? new Date(`${date}T00:00:00`).toLocaleDateString() : 'Not provided';

const StaffWorkspace = () => {
  const { role, user } = useAuth();
  const location = useLocation();
  const content = roleContent[role];
  const [applications, setApplications] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeReview, setActiveReview] = useState(null);
  const [remarks, setRemarks] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [expandedApplicationId, setExpandedApplicationId] = useState(null);

  const loadApplications = useCallback(async () => {
    setError('');
    setIsLoading(true);
    try {
      const response = await getStaffApplications(role);
      setApplications(Array.isArray(response) ? response : []);
    } catch (requestError) {
      setError(requestError.message || 'Unable to load applications.');
    } finally {
      setIsLoading(false);
    }
  }, [role]);

  useEffect(() => {
    loadApplications();
  }, [loadApplications]);

  useEffect(() => {
    const applicationId = new URLSearchParams(location.search).get('applicationId');
    if (isLoading || !applicationId || !applications.some((application) => String(application.applicationId) === applicationId)) return undefined;
    const timer = window.setTimeout(() => focusApplication(Number(applicationId)), 0);
    return () => window.clearTimeout(timer);
  }, [applications, isLoading, location.search]);

  const openReview = (applicationId, outcome) => {
    setActiveReview({ applicationId, outcome });
    setRemarks('');
    setError('');
  };

  const submitReview = async (event) => {
    event.preventDefault();
    if (!activeReview) return;
    if (['REJECT', 'RETURN'].includes(activeReview.outcome) && !remarks.trim()) {
      setError('Remarks are required when returning or rejecting an application.');
      return;
    }

    setIsSubmitting(true);
    setError('');
    try {
      await reviewApplication(activeReview.applicationId, role, activeReview.outcome, remarks);
      setActiveReview(null);
      setRemarks('');
      await loadApplications();
    } catch (requestError) {
      setError(requestError.message || 'Unable to submit the review.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const focusApplication = (applicationId) => {
    setExpandedApplicationId(applicationId);
    window.setTimeout(() => document.getElementById(`staff-application-${applicationId}`)?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 0);
  };

  return (
    <div className="page-container staff-workspace">
      <header className="staff-page-header">
        <div>
          <div className="staff-eyebrow"><ShieldCheck size={16} /> {role} workspace</div>
          <h1 className="page-title">{content.title}</h1>
          <p className="page-subtitle">{content.description}</p>
          <p className="staff-identity">Signed in as {user?.email}</p>
        </div>
        <button className="btn btn-secondary" onClick={loadApplications} disabled={isLoading}>
          <RefreshCw size={17} /> Refresh
        </button>
        {role === 'HOD' && <Link className="btn btn-primary" to="/staff/hod/analytics"><BarChart3 size={17} /> Department analytics</Link>}
      </header>

      {role === 'ADMIN' && (
        <div className="staff-notice"><ShieldCheck size={18} /> Application approval is read-only for Admin. Event management and result verification remain separate administrative actions.</div>
      )}
      {role === 'ADMIN' && <AdminStaffProvisioning />}
      {role === 'ADMIN' && <AdminEvents />}
      {role === 'MENTOR' && <MentorFollowUpCentre onReviewApplication={focusApplication} />}
      {error && <div className="error-alert" role="alert">{error}</div>}

      <div className="staff-summary glass-panel">
        <ClipboardList size={22} />
        <div>
          <span className="staff-summary-value">{isLoading ? '—' : applications.length}</span>
          <span className="staff-summary-label">{role === 'ADMIN' ? 'applications visible' : 'applications awaiting your review'}</span>
        </div>
      </div>

      {isLoading ? (
        <div className="loading-spinner">Loading applications...</div>
      ) : applications.length === 0 ? (
        <div className="empty-state">
          <ClipboardList size={44} className="empty-icon" />
          <p>{role === 'ADMIN' ? 'No applications are currently available.' : 'Your review queue is clear.'}</p>
        </div>
      ) : (
        <div className="staff-application-grid">
          {applications.map((application) => (
            <article id={`staff-application-${application.applicationId}`} className="staff-application-card glass-panel" key={application.applicationId}>
              <div className="staff-card-heading">
                <div>
                  <span className="application-code">{application.applicationCode}</span>
                  <h2>{application.eventName}</h2>
                </div>
                <span className={`status-badge ${statusClass(application.status)}`}>{application.status}</span>
              </div>

              <dl className="staff-application-details">
                <div><dt>Student</dt><dd>{application.studentName}</dd></div>
                <div><dt>Register number</dt><dd>{application.registrationNumber}</dd></div>
                <div><dt>Event type</dt><dd>{application.eventType || 'Not provided'}</dd></div>
                <div><dt>Participation</dt><dd>{application.participationType}</dd></div>
                {application.teamName && <div><dt>Team</dt><dd>{application.teamName}</dd></div>}
                <div><dt>Dates</dt><dd>{formatDate(application.startDate)} – {formatDate(application.endDate)}</dd></div>
                <div><dt>Outside state</dt><dd>{application.outsideState ? 'Yes' : 'No'}</dd></div>
                {(application.city || application.state) && <div><dt>Location</dt><dd>{[application.city, application.state, application.country].filter(Boolean).join(', ')}</dd></div>}
                {application.outsideState && <div><dt>Travel</dt><dd>{application.modeOfTravel || 'Details pending'}</dd></div>}
              </dl>
              {application.description && <div className="reviewer-note"><strong>Purpose</strong><p>{application.description}</p></div>}
              {application.outsideState && <div className="reviewer-note"><strong>Safety and destination</strong><p>{[application.destinationCity, application.destinationState, application.destinationCountry].filter(Boolean).join(', ') || 'Destination not provided'}{application.emergencyContactName ? ` · Emergency: ${application.emergencyContactName} ${application.emergencyContactNumber || ''}` : ''}</p></div>}
              {application.teamMembers?.length > 0 && <div className="team-member-list">{application.teamMembers.map((member) => <span key={member.studentId}>{member.name} · {member.registrationNumber}</span>)}</div>}

              {role === 'HOD' && (
                <div className="reviewer-note">
                  <strong>Mentor remarks</strong>
                  <p>{application.mentorRemarks || 'No Mentor remarks were provided.'}</p>
                </div>
              )}

              {role !== 'ADMIN' && (
                <div className="staff-card-actions">
                  {role === 'MENTOR' && <button className="btn btn-primary" onClick={() => openReview(application.applicationId, 'RECOMMEND')}>
                    <CheckCircle2 size={17} /> Recommend to HOD
                  </button>}
                  {role === 'HOD' && <button className="btn btn-primary" onClick={() => openReview(application.applicationId, 'APPROVE')}>
                    <CheckCircle2 size={17} /> Approve Application
                  </button>}
                  <button className="btn btn-secondary" onClick={() => openReview(application.applicationId, 'RETURN')}>
                    <CornerDownLeft size={17} /> Return for Correction
                  </button>
                  <button className="btn btn-danger-outline" onClick={() => openReview(application.applicationId, 'REJECT')}>
                    <XCircle size={17} /> Reject
                  </button>
                </div>
              )}
              <button className="btn btn-secondary lifecycle-toggle" onClick={() => setExpandedApplicationId(expandedApplicationId === application.applicationId ? null : application.applicationId)}>{expandedApplicationId === application.applicationId ? 'Hide documents and result' : 'View documents and result'}</button>
              {expandedApplicationId === application.applicationId && <ApplicationLifecyclePanel application={application} role={role} />}

              {activeReview?.applicationId === application.applicationId && (
                <form className="review-form" onSubmit={submitReview}>
                  <label htmlFor={`remarks-${application.applicationId}`}>
                    Remarks {['REJECT', 'RETURN'].includes(activeReview.outcome) ? '(required)' : '(optional)'}
                  </label>
                  <textarea
                    id={`remarks-${application.applicationId}`}
                    value={remarks}
                    onChange={(event) => setRemarks(event.target.value)}
                    rows="3"
                    required={['REJECT', 'RETURN'].includes(activeReview.outcome)}
                  />
                  <div className="review-form-actions">
                    <button type="button" className="btn btn-secondary" onClick={() => setActiveReview(null)}>Cancel</button>
                    <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
                      {isSubmitting ? 'Submitting...' : 'Submit Review'}
                    </button>
                  </div>
                </form>
              )}
            </article>
          ))}
        </div>
      )}
      {role !== 'ADMIN' && <AchievementReviewQueue role={role} focusAchievementId={new URLSearchParams(location.search).get('achievementId')} />}
    </div>
  );
};

export default StaffWorkspace;
