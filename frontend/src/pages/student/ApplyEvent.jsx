import React, { useEffect, useState } from 'react';
import { ArrowLeft, Calendar, CheckCircle2, MapPin, Users } from 'lucide-react';
import { Link, useParams } from 'react-router-dom';
import { getEventById } from '../../services/eventService';
import { registerApplication } from '../../services/ticketService';
import { applicationDetailPath, buildApplicationPayload } from '../../utils/studentWorkflow';

const formatDate = (value) => value ? new Date(`${value}T00:00:00`).toLocaleDateString() : 'Not provided';

const ApplyEvent = () => {
  const { eventId } = useParams();
  const [event, setEvent] = useState(null);
  const [form, setForm] = useState({ participationType: 'INDIVIDUAL', teamName: '', teamMemberRegistrationNumbers: '', travelRequired: false, outsideState: false });
  const [createdApplication, setCreatedApplication] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadEvent = async () => {
      setIsLoading(true);
      setError('');
      try {
        const loadedEvent = await getEventById(eventId);
        if (new Date(`${loadedEvent.startDate}T00:00:00`) < new Date(new Date().toDateString())) {
          throw new Error('This event is no longer available for application.');
        }
        setEvent(loadedEvent);
      } catch (requestError) {
        setError(requestError.message || 'Unable to load the selected event.');
      } finally {
        setIsLoading(false);
      }
    };
    loadEvent();
  }, [eventId]);

  const handleChange = (changeEvent) => {
    const { name, value, type, checked } = changeEvent.target;
    setForm((current) => ({ ...current, [name]: type === 'checkbox' ? checked : value }));
  };

  const handleSubmit = async (submitEvent) => {
    submitEvent.preventDefault();
    setError('');
    if (form.participationType === 'TEAM' && !form.teamName.trim()) {
      setError('Team name is required for team participation.');
      return;
    }
    setIsSubmitting(true);
    try {
      setCreatedApplication(await registerApplication(buildApplicationPayload(eventId, form)));
    } catch (requestError) {
      setError(requestError.message || 'Unable to submit the application.');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) return <div className="loading-spinner">Loading selected event...</div>;

  if (!event) {
    return <div className="page-container"><div className="error-alert" role="alert">{error || 'Event not found.'}</div><Link to="/student/events" className="btn btn-secondary">Back to events</Link></div>;
  }

  if (createdApplication) {
    return (
      <div className="page-container submission-confirmation">
        <div className="glass-panel confirmation-card">
          <CheckCircle2 size={54} className="confirmation-icon" />
          <h1>Application submitted</h1>
          <p>Your request is now <strong>{createdApplication.status}</strong> and has been routed to your assigned Mentor.</p>
          <div className="application-code">{createdApplication.applicationCode}</div>
          <Link className="btn btn-primary" to={applicationDetailPath(createdApplication)}>View application status</Link>
        </div>
      </div>
    );
  }

  return (
    <div className="page-container application-form-page">
      <Link to="/student/events" className="back-btn"><ArrowLeft size={16} /> Back to events</Link>
      <div className="selected-event glass-panel">
        <div>
          <span className="category-text">{event.categoryName}</span>
          <h1 className="page-title">{event.eventName}</h1>
          <p className="page-subtitle">{event.organizer}</p>
        </div>
        <div className="selected-event-meta">
          <span><Calendar size={16} /> {formatDate(event.startDate)} – {formatDate(event.endDate)}</span>
          <span><MapPin size={16} /> {event.venue || 'Venue not provided'}</span>
        </div>
      </div>

      {error && <div className="error-alert" role="alert">{error}</div>}
      <form className="form-card glass-panel" onSubmit={handleSubmit}>
        <h2 className="section-title"><Users size={18} /> Participation details</h2>
        <div className="form-group">
          <label htmlFor="participationType">Participation type</label>
          <select id="participationType" name="participationType" value={form.participationType} onChange={handleChange}>
            <option value="INDIVIDUAL">Individual</option>
            <option value="TEAM">Team</option>
          </select>
        </div>
        {form.participationType === 'TEAM' && (
          <>
            <div className="form-group">
              <label htmlFor="teamName">Team name</label>
              <input id="teamName" name="teamName" value={form.teamName} onChange={handleChange} maxLength="150" required />
            </div>
            <div className="form-group">
              <label htmlFor="teamMemberRegistrationNumbers">Additional member registration numbers</label>
              <textarea id="teamMemberRegistrationNumbers" name="teamMemberRegistrationNumbers" value={form.teamMemberRegistrationNumbers} onChange={handleChange} rows="3" placeholder="Comma-separated existing Student registration numbers" />
              <small>Your own Student record is added automatically as team leader. Every additional member must already exist in institutional Student data.</small>
            </div>
          </>
        )}
        <label className="checkbox-label request-option">
          <input type="checkbox" name="travelRequired" checked={form.travelRequired} onChange={handleChange} />
          <span>Travel is required for this event</span>
        </label>
        <label className="checkbox-label request-option">
          <input type="checkbox" name="outsideState" checked={form.outsideState} onChange={handleChange} />
          <span>The event is outside Tamil Nadu</span>
        </label>
        <div className="form-actions">
          <Link className="btn btn-secondary" to="/student/events">Cancel</Link>
          <button className="btn btn-primary" type="submit" disabled={isSubmitting}>{isSubmitting ? 'Submitting...' : 'Submit request'}</button>
        </div>
      </form>
    </div>
  );
};

export default ApplyEvent;
