import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMyTickets } from '../../services/ticketService';
import { FileText, Calendar, MapPin, ExternalLink, Activity } from 'lucide-react';

const MyApplications = () => {
  const navigate = useNavigate();
  const [applications, setApplications] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchApplications = async () => {
      try {
        // Assume backend ticket API maps to our application models
        const data = await getMyTickets();
        const appsList = Array.isArray(data) ? data : (data.content || data.data || []);
        
        // Let's mock a bit if the backend doesn't return the exact schema we just designed
        // In a real scenario, the backend would align with the new schema.
        setApplications(appsList);
      } catch (err) {
        setError('Failed to load your applications.');
      } finally {
        setIsLoading(false);
      }
    };
    fetchApplications();
  }, []);

  const getStatusColor = (status) => {
    switch (status) {
      case 'SUBMITTED': return 'status-submitted';
      case 'UNDER_REVIEW': return 'status-waitlisted';
      case 'APPROVED': return 'status-approved';
      case 'REJECTED': return 'status-rejected';
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

  return (
    <div className="page-container">
      <div className="page-header flex-between">
        <div>
          <h1 className="page-title">My Applications</h1>
          <p className="page-subtitle">Track the status of your OD and event requests.</p>
        </div>
        <button className="btn btn-primary" onClick={() => navigate('/apply')}>
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
          <button className="btn btn-primary mt-4" onClick={() => navigate('/apply')}>
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
                <th>Type</th>
                <th>Dates</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {applications.map(app => (
                <tr key={app.id || app.applicationCode}>
                  <td className="font-mono font-bold text-navy">{app.applicationCode || 'APP-MOCK'}</td>
                  <td>
                    <div className="app-event-name">{app.eventName || app.event?.title || 'Unknown Event'}</div>
                    <div className="app-institution">{app.organizingInstitution || app.event?.venue || 'Institution TBA'}</div>
                  </td>
                  <td>
                    <span className={`type-badge ${getTypeBadge(app.eventType)}`}>
                      {app.eventType?.replace('_', ' ') || 'GENERAL'}
                    </span>
                  </td>
                  <td className="text-muted text-sm">
                    {app.startDate ? new Date(app.startDate).toLocaleDateString() : 'N/A'}
                  </td>
                  <td>
                    <span className={`status-badge ${getStatusColor(app.status)}`}>
                      {app.status || 'SUBMITTED'}
                    </span>
                  </td>
                  <td>
                    <button 
                      className="btn btn-secondary btn-sm"
                      onClick={() => navigate(`/applications/${app.applicationCode || app.id}`)}
                    >
                      View Details
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
