import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { AlertCircle, Award, CheckCircle2, ClipboardList, RefreshCw } from 'lucide-react';
import { getMentorFollowUps } from '../services/staffService';

const labels = {
  NEEDS_MENTOR_REVIEW: 'Needs My Review',
  STUDENT_CORRECTION_PENDING: 'Student Correction Pending',
  POST_EVENT_PENDING: 'Post-Event Proof Pending',
  POST_EVENT_EVIDENCE_INCOMPLETE: 'Evidence Incomplete',
  ACHIEVEMENT_VERIFICATION_PENDING: 'Achievement Verification Pending',
};

const MentorFollowUpCentre = ({ onReviewApplication, onOpenItem }) => {
  const [payload, setPayload] = useState({ summary: {}, items: [] });
  const [filters, setFilters] = useState({ followUpType: '', section: '', studentYear: '', recordCategory: '', minimumDaysPending: '' });
  const [selectedType, setSelectedType] = useState('');
  const [selectedItem, setSelectedItem] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    setLoading(true); setError('');
    try { setPayload(await getMentorFollowUps()); }
    catch (requestError) { setError(requestError.message || 'Unable to load student follow-ups.'); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, [load]);

  const filteredItems = useMemo(() => (payload.items || []).filter((item) => (
    (!filters.followUpType || item.followUpType === filters.followUpType) &&
    (!filters.section || item.section === filters.section) &&
    (!filters.studentYear || String(item.studentYear) === filters.studentYear) &&
    (!filters.recordCategory || item.recordCategory === filters.recordCategory) &&
    (!filters.minimumDaysPending || item.daysPending >= Number(filters.minimumDaysPending)) &&
    (!selectedType || item.followUpType === selectedType)
  )), [filters, payload.items, selectedType]);

  const summary = filteredItems.reduce((accumulator, item) => {
    accumulator[item.followUpType] = (accumulator[item.followUpType] || 0) + 1;
    return accumulator;
  }, {});

  const updateFilter = (name, value) => { setFilters((current) => ({ ...current, [name]: value })); setSelectedType(''); };
  const openItem = (item) => {
    if (item.recordType === 'APPLICATION' && item.followUpType === 'NEEDS_MENTOR_REVIEW') onReviewApplication?.(item.applicationId);
    else { setSelectedItem(item); onOpenItem?.(item); }
  };
  const sections = [...new Set((payload.items || []).map((item) => item.section).filter(Boolean))].sort();
  const categories = [...new Set((payload.items || []).map((item) => item.recordCategory).filter(Boolean))].sort();
  const cards = [
    ['NEEDS_MENTOR_REVIEW', summary.NEEDS_MENTOR_REVIEW || 0, ClipboardList],
    ['STUDENT_CORRECTION_PENDING', summary.STUDENT_CORRECTION_PENDING || 0, AlertCircle],
    ['POST_EVENT_PENDING', summary.POST_EVENT_PENDING || 0, ClipboardList],
    ['POST_EVENT_EVIDENCE_INCOMPLETE', summary.POST_EVENT_EVIDENCE_INCOMPLETE || 0, AlertCircle],
    ['ACHIEVEMENT_VERIFICATION_PENDING', summary.ACHIEVEMENT_VERIFICATION_PENDING || 0, Award],
  ];

  return <section className="mentor-follow-up-centre glass-panel">
    <header className="staff-page-header">
      <div><div className="staff-eyebrow"><AlertCircle size={16} /> Student follow-ups</div><h2 className="section-title">Mentor Follow-Up Centre</h2><p className="page-subtitle">Only students routed to your active Mentor assignment are shown.</p></div>
      <button className="btn btn-secondary" onClick={load} disabled={loading}><RefreshCw size={17} /> Refresh</button>
    </header>
    {error && <div className="error-alert" role="alert">{error}</div>}
    <div className="mentor-follow-up-cards">
      {cards.map(([type, count, Icon]) => <button type="button" className={`mentor-follow-up-card ${selectedType === type ? 'is-active' : ''}`} key={type} onClick={() => setSelectedType(selectedType === type ? '' : type)}><Icon size={20} /><span><strong>{count}</strong><small>{labels[type]}</small></span></button>)}
      <div className="mentor-follow-up-total"><strong>{filteredItems.length}</strong><span>Total Attention Required</span></div>
    </div>
    <div className="mentor-follow-up-filters">
      <select value={filters.followUpType} onChange={(event) => updateFilter('followUpType', event.target.value)}><option value="">All follow-up types</option>{Object.entries(labels).map(([value, label]) => <option value={value} key={value}>{label}</option>)}</select>
      <select value={filters.section} onChange={(event) => updateFilter('section', event.target.value)}><option value="">All sections</option>{sections.map((value) => <option value={value} key={value}>{value}</option>)}</select>
      <select value={filters.studentYear} onChange={(event) => updateFilter('studentYear', event.target.value)}><option value="">All student years</option>{[1, 2, 3, 4, 5, 6].map((value) => <option value={value} key={value}>Year {value}</option>)}</select>
      <select value={filters.recordCategory} onChange={(event) => updateFilter('recordCategory', event.target.value)}><option value="">All event / achievement types</option>{categories.map((value) => <option value={value} key={value}>{value}</option>)}</select>
      <input type="number" min="0" placeholder="Minimum days" value={filters.minimumDaysPending} onChange={(event) => updateFilter('minimumDaysPending', event.target.value)} />
    </div>
    {selectedItem && <div className="reviewer-note mentor-follow-up-selection"><strong>Selected follow-up</strong><p>{selectedItem.studentName} · {selectedItem.title} · {labels[selectedItem.followUpType] || selectedItem.followUpType}</p><button type="button" className="btn btn-secondary btn-sm" onClick={() => setSelectedItem(null)}>Close</button></div>}
    {loading ? <div className="loading-spinner">Loading follow-ups...</div> : filteredItems.length === 0 ? <div className="empty-state"><CheckCircle2 size={40} className="empty-icon" /><p>No follow-ups match the current filters.</p></div> : <div className="mentor-follow-up-table-wrap"><table className="modern-table mentor-follow-up-table"><thead><tr><th>Student</th><th>Section</th><th>Type</th><th>Activity / Achievement</th><th>Status / Reason</th><th>Relevant Date</th><th>Days Pending</th><th>Action</th></tr></thead><tbody>{filteredItems.map((item) => <tr key={`${item.recordType}-${item.applicationId || item.achievementId}`}><td><strong>{item.studentName}</strong><small>{item.registrationNumber}</small></td><td>{item.section} · Year {item.studentYear}</td><td>{labels[item.followUpType] || item.followUpType}</td><td>{item.title}<small>{item.recordCategory}</small></td><td>{item.currentStatus.replaceAll('_', ' ')}{item.remarks && <small>{item.remarks}</small>}</td><td>{item.relevantDate || 'Not provided'}</td><td>{item.daysPending}</td><td><button type="button" className="btn btn-secondary btn-sm" onClick={() => openItem(item)}>{item.followUpType === 'NEEDS_MENTOR_REVIEW' ? 'Review Application' : item.recordType === 'ACHIEVEMENT' ? 'Verify Achievement' : 'View Record'}</button></td></tr>)}</tbody></table></div>}
  </section>;
};

export default MentorFollowUpCentre;
