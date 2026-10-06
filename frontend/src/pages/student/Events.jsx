import React, { useEffect, useState } from 'react';
import { CalendarSearch, RefreshCw } from 'lucide-react';
import EventCard from '../../components/EventCard';
import { getEvents } from '../../services/eventService';
import { normalizeEventList } from '../../utils/studentWorkflow';

const Events = () => {
  const [events, setEvents] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  const loadEvents = async () => {
    setIsLoading(true);
    setError('');
    try {
      setEvents(normalizeEventList(await getEvents()));
    } catch (requestError) {
      setError(requestError.message || 'Unable to load available events.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => { loadEvents(); }, []);

  return (
    <div className="page-container events-page">
      <header className="page-header flex-between flex-wrap gap-4">
        <div>
          <h1 className="page-title">Available Events</h1>
          <p className="page-subtitle">Select an event to create an institutional participation request.</p>
        </div>
        <button className="btn btn-secondary" onClick={loadEvents} disabled={isLoading}><RefreshCw size={17} /> Refresh</button>
      </header>

      {error && <div className="error-alert" role="alert">{error}</div>}
      {isLoading ? (
        <div className="loading-spinner">Loading available events...</div>
      ) : events.length === 0 ? (
        <div className="empty-state"><CalendarSearch size={44} className="empty-icon" /><p>No upcoming events are currently available.</p></div>
      ) : (
        <div className="event-grid">{events.map((event) => <EventCard key={event.eventId} event={event} />)}</div>
      )}
    </div>
  );
};

export default Events;
