import React from 'react';
import { Link } from 'react-router-dom';
import { MapPin, Calendar, Users } from 'lucide-react';

const EventCard = ({ event }) => {
  const formatDate = (dateString) => {
    const options = { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' };
    return new Date(dateString).toLocaleDateString(undefined, options);
  };

  return (
    <div className="event-card">
      <div className="event-card-header">
        <span className={`mode-pill mode-${event.mode.toLowerCase()}`}>{event.mode}</span>
        <span className="category-text">{event.category}</span>
      </div>
      
      <div className="event-card-body">
        <h3 className="event-title">{event.title}</h3>
        
        <div className="event-details">
          <div className="detail-row">
            <Calendar size={16} />
            <span>{formatDate(event.startTime)}</span>
          </div>
          <div className="detail-row">
            <MapPin size={16} />
            <span>{event.venue || 'TBA'}</span>
          </div>
          <div className="detail-row">
            <Users size={16} />
            <span>Registration Type: {event.registrationType}</span>
          </div>
        </div>
      </div>
      
      <div className="event-card-footer">
        <div className="deadline">
          <span className="deadline-label">Closes:</span>
          <span className="deadline-value">{new Date(event.registrationDeadline).toLocaleDateString()}</span>
        </div>
        <Link to={`/events/${event.id}`} className="btn btn-primary">
          View Details
        </Link>
      </div>
    </div>
  );
};

export default EventCard;
