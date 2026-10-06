import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMyTickets } from '../../services/ticketService';
import { FileText } from 'lucide-react';
import { postEventActionLabel, postEventStatusLabel, statusLabel } from '../../utils/studentWorkflow';

const MyApplications = () => {
  const navigate = useNavigate();
  const [applications, setApplications] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchApplications = async () => {
      try {
        const data = await getMyTickets();
        const list = Array.isArray(data) ? data : (data?.content || data?.data || []);
        setApplications(Array.isArray(list) ? list : []);
      } catch (err) {
        setError(err.message || 'Failed to load your applications.');
      } finally {
        setIsLoading(false);
      }
    };
    fetchApplications();
  }, []);

  const getStatusColor = (status) => {
    switch (status) {
      case 'SUBMITTED': return 'status-submitted';
      case 'PENDING_MENTOR_APPROVAL': return 'status-submitted';
      case 'PENDING_HOD_APPROVAL': return 'status-waitlisted';
      case 'RETURNED_BY_MENTOR': return 'status-waitlisted';
      case 'RETURNED_BY_HOD': return 'status-waitlisted';
      case 'APPROVED': return 'status-approved';
      case 'REJECTED':
      case 'REJECTED_BY_MENTOR':
      case 'REJECTED_BY_HOD': return 'status-rejected';
      default: return 'status-default';
    }
  };

  const getTypeBadge = (type) => {
    if(!type) return 'badge-default';
    switch (type.toUpperCase()) {
      case 'HACKATHON': return 'badge-hackathon';
      case 'WORKSHOP': return 'badge-workshop';
      case 'CONFERENCE': return 'badge-conference';
      default: return 'badge-default';
    }
  };

  const formatDateRange = (app) => {
    if (!app.startDate) return 'Date not provided';
    const start = new Date(`${app.startDate}T00:00:00`).toLocaleDateString();
    if (!app.endDate || app.endDate === app.startDate) return start;
    return `${start} – ${new Date(`${app.endDate}T00:00:00`).toLocaleDateString()}`;
  };

  const participationLabel = (value) => value ? value.replaceAll('_', ' ') : 'Individual';
  const postEventLabel = (app) => {
    const label = postEventStatusLabel(app);
    if (app.postEventStatus === 'NOT_DUE' && label) return `${label} · Available after ${app.endDate || 'event end'}`;
    return label;
  };

  return (
    <div className="page-container">
      <div className="page-header flex-between">
        <div>
          <h1 className="page-title">My Applications</h1>
          <p className="page-subtitle">Track the status of your OD and event requests.</p>
        </div>
        <button className="btn btn-primary" onClick={() => navigate('/student/events')}>
          New Request
        </button>
      </div>

      {error && <div className="error-alert">{error}</div>}

      {isLoading ? (
        <div className="loading-spinner">Loading applications...</div>
      ) : applications.length === 0 ? (
        <div className="empty-state">
          <FileText size={48} className="empty-icon" />
          <p>You haven't submitted any event applications yet.</p>
          <button className="btn btn-primary mt-4" onClick={() => navigate('/student/events')}>
            Apply for an Event
          </button>
        </div>
      ) : (
        <div className="applications-table-container glass-panel">
          <table className="modern-table">
            <thead>
              <tr>
                <th>App Code</th>
                <th>Event Details</th>
                <th>Category / Type</th>
                <th>Dates</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {applications.map(app => (
                <tr key={app.applicationId || app.applicationCode}>
                  <td data-label="App Code" className="font-mono font-bold text-navy">{app.applicationCode}</td>
                  <td data-label="Event">
                    <div className="app-event-name">{app.eventName}</div>
                    <div className="app-institution">{app.organizer || app.venue || 'Not provided'}</div>
                  </td>
                  <td data-label="Category / Type">
                    <span className={`type-badge ${getTypeBadge(app.eventType)}`}>
                      {app.eventType?.replace('_', ' ') || 'GENERAL'}
                    </span>
                    <span className="application-participation">{participationLabel(app.participationType)}</span>
                  </td>
                  <td data-label="Dates" className="text-muted text-sm application-date">
                    {formatDateRange(app)}
                  </td>
                  <td data-label="Status">
                    <span className={`status-badge ${getStatusColor(app.status)}`}>
                      {statusLabel(app.status)}
                    </span>
                    {postEventLabel(app) && <span className="post-event-status">{postEventLabel(app)}</span>}
                  </td>
                  <td data-label="Action" className="application-action-cell">
                    <button 
                      className="btn btn-secondary btn-sm"
                      onClick={() => navigate(`/student/applications/${app.applicationCode}`)}
                    >
                      {postEventActionLabel(app)}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default MyApplications;
