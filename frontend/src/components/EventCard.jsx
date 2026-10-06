import React from 'react';
import { Calendar, ExternalLink, MapPin } from 'lucide-react';
import { Link } from 'react-router-dom';

const formatDate = (value) => value
  ? new Date(`${value}T00:00:00`).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })
  : 'Not provided';

const EventCard = ({ event }) => (
  <article className="event-card glass-panel">
    <div className="event-card-header">
      <span className={`mode-pill mode-${event.mode?.toLowerCase() || 'unknown'}`}>{event.mode || 'Mode not provided'}</span>
      <span className="category-text">{event.categoryName || 'Uncategorized'}</span>
    </div>

    <div className="event-card-body">
      <h2 className="event-title">{event.eventName}</h2>
      <p className="event-organizer">{event.organizer || 'Organizer not provided'}</p>
      <div className="event-details">
        <div className="detail-row"><Calendar size={16} /><span>{formatDate(event.startDate)} – {formatDate(event.endDate)}</span></div>
        <div className="detail-row"><MapPin size={16} /><span>{event.venue || 'Venue not provided'}</span></div>
        {event.eventUrl && (
          <a className="detail-row event-reference-link" href={event.eventUrl} target="_blank" rel="noreferrer">
            <ExternalLink size={16} /><span>Event reference</span>
          </a>
        )}
      </div>
    </div>

    <div className="event-card-footer">
      <Link to={`/student/events/${event.eventId}/apply`} className="btn btn-primary">Apply for this event</Link>
    </div>
  </article>
);

export default EventCard;
