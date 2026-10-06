import React, { useCallback, useEffect, useState } from 'react';
import { CalendarPlus, Edit3 } from 'lucide-react';
import { createEvent, getAllEvents, getEventCategories, updateEvent } from '../services/eventService';
import { buildEventPayload } from '../utils/studentWorkflow';

const emptyForm = { name: '', categoryId: '', organizer: '', startDate: '', endDate: '', mode: 'OFFLINE', venue: '', eventUrl: '' };

const AdminEvents = () => {
  const [events, setEvents] = useState([]); const [categories, setCategories] = useState([]);
  const [form, setForm] = useState(emptyForm); const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState(''); const [busy, setBusy] = useState(false);
  const reload = useCallback(async () => { const [eventList, categoryList] = await Promise.all([getAllEvents(), getEventCategories()]); setEvents(eventList || []); setCategories(categoryList || []); }, []);
  useEffect(() => { reload().catch((requestError) => setError(requestError.message)); }, [reload]);
  const edit = (event) => { setEditingId(event.eventId); setForm({ name: event.eventName, categoryId: String(event.categoryId), organizer: event.organizer, startDate: event.startDate, endDate: event.endDate, mode: event.mode, venue: event.venue || '', eventUrl: event.eventUrl || '' }); };
  const submit = async (submitEvent) => { submitEvent.preventDefault(); setBusy(true); setError(''); try { const payload = buildEventPayload(form); if (editingId) await updateEvent(editingId, payload); else await createEvent(payload); setForm(emptyForm); setEditingId(null); await reload(); } catch (requestError) { setError(requestError.message); } finally { setBusy(false); } };
  return (
    <section className="admin-events glass-panel">
      <h2 className="section-title"><CalendarPlus size={19} /> Event management</h2>
      {error && <div className="error-alert" role="alert">{error}</div>}
      <form className="admin-event-form" onSubmit={submit}>
        <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="Event name" maxLength="200" required />
        <select value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })} required><option value="">Category</option>{categories.map((category) => <option key={category.categoryId} value={category.categoryId}>{category.categoryName}</option>)}</select>
        <input value={form.organizer} onChange={(e) => setForm({ ...form, organizer: e.target.value })} placeholder="Organizer" maxLength="200" required />
        <input type="date" value={form.startDate} onChange={(e) => setForm({ ...form, startDate: e.target.value })} required />
        <input type="date" value={form.endDate} min={form.startDate} onChange={(e) => setForm({ ...form, endDate: e.target.value })} required />
        <select value={form.mode} onChange={(e) => setForm({ ...form, mode: e.target.value })}><option>OFFLINE</option><option>ONLINE</option><option>HYBRID</option></select>
        <input value={form.venue} onChange={(e) => setForm({ ...form, venue: e.target.value })} placeholder="Venue (optional)" maxLength="255" />
        <input type="url" value={form.eventUrl} onChange={(e) => setForm({ ...form, eventUrl: e.target.value })} placeholder="Reference URL (optional)" maxLength="500" />
        <div className="form-actions">{editingId && <button type="button" className="btn btn-secondary" onClick={() => { setEditingId(null); setForm(emptyForm); }}>Cancel edit</button>}<button className="btn btn-primary" disabled={busy}>{busy ? 'Saving...' : editingId ? 'Save event' : 'Create event'}</button></div>
      </form>
      <div className="admin-event-list">{events.map((event) => <article key={event.eventId}><div><strong>{event.eventName}</strong><span>{event.categoryName} · {event.startDate} – {event.endDate}</span></div><button className="btn btn-secondary btn-sm" onClick={() => edit(event)}><Edit3 size={15} /> Edit</button></article>)}</div>
    </section>
  );
};

export default AdminEvents;
