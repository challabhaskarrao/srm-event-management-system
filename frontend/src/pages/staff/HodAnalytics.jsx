import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { BarChart3, Filter, RefreshCw, Search, ShieldCheck } from 'lucide-react';
import {
  buildHodAnalyticsParams,
  downloadHodAnalyticsExport,
  getHodAnalytics,
  getHodAnalyticsDrilldown,
  getHodAnalyticsFilterError,
} from '../../services/hodAnalyticsService';

const current = new Date();
const initialFilters = {
  period: 'MONTHLY',
  month: current.getMonth() + 1,
  year: current.getFullYear(),
  academicYearId: '',
  semester: '',
  fromDate: '',
  toDate: '',
  sectionId: '',
  studentYear: '',
  eventTypeCode: '',
  status: '',
  mentorId: '',
};

const number = (value) => Number(value || 0);
const label = (value) => value || '—';
const metricCards = [
  ['total_applications', 'Applications', 'APPLICATIONS'],
  ['approved_applications', 'Approved', 'APPLICATIONS'],
  ['pending_mentor', 'Pending Mentor', 'PENDING_MENTOR'],
  ['pending_hod', 'Pending HOD', 'PENDING_HOD'],
  ['returned_applications', 'Returned', 'RETURNED'],
  ['rejected_applications', 'Rejected', 'REJECTED'],
  ['post_event_pending', 'Post-event pending', 'POST_EVENT_PENDING'],
  ['evidence_incomplete', 'Evidence incomplete', 'EVIDENCE_INCOMPLETE'],
  ['complete', 'Complete', 'COMPLETE'],
  ['achievements_recorded', 'Achievements', 'ACHIEVEMENTS'],
  ['achievements_verification_pending', 'Verification pending', 'ACHIEVEMENT_VERIFICATION_PENDING'],
  ['achievements_verified', 'Verified achievements', 'VERIFIED_ACHIEVEMENTS'],
  ['unique_activity_students', 'Unique activity students', 'APPLICATIONS'],
  ['unique_achievement_students', 'Unique achievement students', 'ACHIEVEMENTS'],
  ['unique_any_engagement', 'Unique engaged students', 'APPLICATIONS'],
];

const HodAnalytics = () => {
  const [filters, setFilters] = useState(initialFilters);
  const [appliedFilters, setAppliedFilters] = useState(initialFilters);
  const [data, setData] = useState(null);
  const [drilldown, setDrilldown] = useState({ metric: '', rows: [] });
  const [loading, setLoading] = useState(true);
  const [drilldownLoading, setDrilldownLoading] = useState(false);
  const [error, setError] = useState('');
  const [sectionOptions, setSectionOptions] = useState([]);
  const [exporting, setExporting] = useState('');

  const load = useCallback(async (nextFilters) => {
    const validationError = getHodAnalyticsFilterError(nextFilters);
    if (validationError) return false;
    setLoading(true); setError('');
    try {
      setData(await getHodAnalytics(buildHodAnalyticsParams(nextFilters)));
      setAppliedFilters({ ...nextFilters });
      setDrilldown({ metric: '', rows: [] });
      return true;
    } catch (requestError) { setError(requestError.message || 'Unable to load HOD analytics.'); return false; }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { load(initialFilters); }, [load]);
  useEffect(() => {
    if (!data?.sections) return;
    setSectionOptions((currentOptions) => {
      const merged = new Map(currentOptions.map((section) => [String(section.section_id), section]));
      data.sections.forEach((section) => merged.set(String(section.section_id), section));
      return [...merged.values()].sort((left, right) => String(left.section_name).localeCompare(String(right.section_name)));
    });
  }, [data]);

  const update = (name, value) => setFilters((currentFilters) => {
    if (name !== 'period') return { ...currentFilters, [name]: value };
    return {
      ...currentFilters,
      period: value,
      academicYearId: '',
      semester: '',
      fromDate: '',
      toDate: '',
    };
  });
  const filterError = getHodAnalyticsFilterError(filters);
  const hasUnappliedChanges = JSON.stringify(filters) !== JSON.stringify(appliedFilters);
  const applyFilters = () => { if (!filterError) load(filters); };
  const openDrilldown = async (metric) => {
    setDrilldownLoading(true); setError('');
    try { setDrilldown({ metric, rows: await getHodAnalyticsDrilldown(metric, buildHodAnalyticsParams(appliedFilters)) }); }
    catch (requestError) { setError(requestError.message || 'Unable to load the selected drilldown.'); }
    finally { setDrilldownLoading(false); }
  };
  const exportReport = async (format) => { setExporting(format); setError(''); try { const blob = await downloadHodAnalyticsExport(format, buildHodAnalyticsParams(appliedFilters)); const url = URL.createObjectURL(blob); const link = document.createElement('a'); link.href = url; link.download = `SRM_Activity_Report.${format === 'excel' ? 'xlsx' : 'pdf'}`; link.click(); URL.revokeObjectURL(url); } catch (requestError) { setError(requestError.message || `Unable to export ${format} report.`); } finally { setExporting(''); } };

  const mentorOptions = useMemo(() => (data?.mentors || []).slice().sort((left, right) => String(left.mentor_name).localeCompare(String(right.mentor_name))), [data]);

  const summary = data?.summary || {};
  const maxTrend = useMemo(() => Math.max(1, ...(data?.monthlyTrend || []).map((row) => number(row.activity_applications) + number(row.achievements_recorded))), [data]);
  const maxType = useMemo(() => Math.max(1, ...(data?.activityTypes || []).map((row) => number(row.applications))), [data]);

  return (
    <div className="page-container hod-analytics-page">
      <header className="staff-page-header">
        <div>
          <div className="staff-eyebrow"><ShieldCheck size={16} /> HOD workspace</div>
          <h1 className="page-title">Department analytics</h1>
          <p className="page-subtitle">Operational visibility for your authenticated department. The department is never taken from the browser filters.</p>
        </div>
        <div className="flex gap-2 flex-wrap"><button className="btn btn-secondary" onClick={() => exportReport('excel')} disabled={Boolean(exporting) || !data || hasUnappliedChanges || Boolean(filterError)}>Export Excel</button><button className="btn btn-secondary" onClick={() => exportReport('pdf')} disabled={Boolean(exporting) || !data || hasUnappliedChanges || Boolean(filterError)}>Export PDF</button><button className="btn btn-secondary" onClick={() => load(appliedFilters)} disabled={loading}><RefreshCw size={17} /> Refresh</button></div>
      </header>

      <section className="glass-panel hod-analytics-filters">
        <div className="hod-analytics-filter-heading"><Filter size={18} /><strong>Analytics scope</strong><span>Semester and student-year charts include snapshot data only.</span></div>
        <div className="hod-filter-grid">
          <label>Period<select value={filters.period} onChange={(event) => update('period', event.target.value)}><option value="MONTHLY">Monthly</option><option value="SEMESTER">Semester</option><option value="ACADEMIC_YEAR">Academic year</option><option value="CUSTOM">Custom date range</option></select></label>
          {filters.period === 'MONTHLY' && <><label>Month<input type="number" min="1" max="12" value={filters.month} onChange={(event) => update('month', event.target.value)} /></label><label>Year<input type="number" value={filters.year} onChange={(event) => update('year', event.target.value)} /></label></>}
          {(filters.period === 'SEMESTER' || filters.period === 'ACADEMIC_YEAR') && <label>Academic year ID<input type="number" min="1" value={filters.academicYearId} onChange={(event) => update('academicYearId', event.target.value)} placeholder="Required" /></label>}
          {filters.period === 'SEMESTER' && <label>Institutional semester<select value={filters.semester} onChange={(event) => update('semester', event.target.value)}><option value="">Required</option>{Array.from({ length: 12 }, (_, index) => index + 1).map((semester) => <option key={semester} value={semester}>{semester}</option>)}</select></label>}
          {filters.period === 'CUSTOM' && <><label>Start date<input type="date" value={filters.fromDate} onChange={(event) => update('fromDate', event.target.value)} /></label><label>End date<input type="date" value={filters.toDate} min={filters.fromDate || undefined} onChange={(event) => update('toDate', event.target.value)} /></label></>}
          <label>Section<select value={filters.sectionId} onChange={(event) => update('sectionId', event.target.value)}><option value="">All sections</option>{sectionOptions.map((section) => <option key={section.section_id} value={section.section_id}>{section.section_name}</option>)}</select></label>
          <label>Student year<input type="number" min="1" value={filters.studentYear} onChange={(event) => update('studentYear', event.target.value)} placeholder="All" /></label>
          <label>Event type code<input value={filters.eventTypeCode} onChange={(event) => update('eventTypeCode', event.target.value)} placeholder="All" /></label>
          <label>Status<input value={filters.status} onChange={(event) => update('status', event.target.value)} placeholder="All" /></label>
          <label>Mentor<select value={filters.mentorId} onChange={(event) => update('mentorId', event.target.value)}><option value="">All mentors</option>{mentorOptions.map((mentor) => <option key={mentor.mentor_id} value={mentor.mentor_id}>{mentor.mentor_name}</option>)}</select></label>
        </div>
        {filterError && <p className="muted-text" role="status">{filterError}</p>}
        {hasUnappliedChanges && data && <p className="muted-text" role="status">Showing previous results. Apply the completed filters to update analytics.</p>}
        <button className="btn btn-primary" type="button" onClick={applyFilters} disabled={loading || Boolean(filterError)}>Apply Filters</button>
      </section>

      {error && <div className="error-alert" role="alert">{error}</div>}
      {loading && <div className="loading-spinner">Loading department analytics...</div>}
      {!loading && data && <>
        <div className="hod-analytics-note">Historical context: {data.historicalContext?.note}</div>
        <section className="hod-analytics-card-grid">
          {metricCards.map(([key, title, metric]) => <button className="glass-panel hod-metric-card" key={key} onClick={() => openDrilldown(metric)}><span>{title}</span><strong>{number(summary[key])}</strong><small>View drilldown</small></button>)}
        </section>

        <div className="hod-analytics-two-column">
          <section className="glass-panel hod-analytics-panel"><h2>Activity types</h2><div className="hod-bars">{(data.activityTypes || []).map((row) => <div className="hod-bar-row" key={row.type_code}><span>{label(row.type_name)}</span><div><i style={{ width: `${Math.round((number(row.applications) / maxType) * 100)}%` }} /></div><strong>{number(row.applications)}</strong></div>)}</div>{!data.activityTypes?.length && <p className="muted-text">No activity records in this scope.</p>}</section>
          <section className="glass-panel hod-analytics-panel"><h2>Achievement types</h2><div className="analytics-table-wrap"><table className="modern-table"><thead><tr><th>Type</th><th>Recorded</th><th>Pending</th><th>Verified</th></tr></thead><tbody>{(data.achievementTypes || []).map((row) => <tr key={row.type_code}><td data-label="Type">{label(row.type_name)}</td><td data-label="Recorded">{number(row.recorded)}</td><td data-label="Pending">{number(row.pending)}</td><td data-label="Verified">{number(row.verified)}</td></tr>)}</tbody></table></div>{!data.achievementTypes?.length && <p className="muted-text">No achievement records in this scope.</p>}</section>
        </div>

        <section className="glass-panel hod-analytics-panel"><h2>Monthly trend</h2><div className="hod-trend-list">{(data.monthlyTrend || []).map((row) => <div className="hod-trend-row" key={row.label}><span>{row.label}</span><div className="hod-trend-track"><i style={{ width: `${Math.round(((number(row.activity_applications) + number(row.achievements_recorded)) / maxTrend) * 100)}%` }} /></div><small>{number(row.activity_applications)} activities · {number(row.achievements_recorded)} achievements</small></div>)}</div>{!data.monthlyTrend?.length && <p className="muted-text">No monthly records in this scope.</p>}</section>

        <div className="hod-analytics-two-column">
          <section className="glass-panel hod-analytics-panel"><h2>Sections</h2><div className="analytics-table-wrap"><table className="modern-table"><thead><tr><th>Section</th><th>Students</th><th>Applications</th><th>Approved</th><th>Complete</th><th>Post-event pending</th></tr></thead><tbody>{(data.sections || []).map((row) => <tr key={row.section_id}><td data-label="Section">{label(row.section_name)}</td><td data-label="Students">{number(row.students_with_records)}</td><td data-label="Applications">{number(row.applications)}</td><td data-label="Approved">{number(row.approved_activities)}</td><td data-label="Complete">{number(row.completed_activities)}</td><td data-label="Post-event pending">{number(row.post_event_pending)}</td></tr>)}</tbody></table></div></section>
          <section className="glass-panel hod-analytics-panel"><h2>Student years</h2><div className="analytics-table-wrap"><table className="modern-table"><thead><tr><th>Captured year</th><th>Records</th><th>Students</th></tr></thead><tbody>{(data.studentYears || []).map((row) => <tr key={row.student_year_label}><td data-label="Captured year">{row.student_year_label}</td><td data-label="Records">{number(row.records)}</td><td data-label="Students">{number(row.unique_students)}</td></tr>)}</tbody></table></div></section>
        </div>

        <section className="glass-panel hod-analytics-panel"><h2>Mentor operational view</h2><p className="muted-text">Counts describe workload and follow-up, not mentor performance scoring.</p><div className="analytics-table-wrap"><table className="modern-table"><thead><tr><th>Mentor</th><th>Assigned students</th><th>Applications</th><th>Needs review</th><th>Correction pending</th><th>Post-event pending</th><th>Achievement verification</th></tr></thead><tbody>{(data.mentors || []).map((row) => <tr key={row.mentor_id}><td data-label="Mentor">{row.mentor_name}</td><td data-label="Assigned students">{number(row.assigned_student_count)}</td><td data-label="Applications">{number(row.applications)}</td><td data-label="Needs review">{number(row.needs_review)}</td><td data-label="Correction pending">{number(row.correction_pending)}</td><td data-label="Post-event pending">{number(row.post_event_pending)}</td><td data-label="Achievement verification">{number(row.achievement_verification_pending)}</td></tr>)}</tbody></table></div></section>

        <section className="glass-panel hod-analytics-panel"><div className="hod-drilldown-heading"><div><h2>Drilldown {drilldown.metric && `· ${drilldown.metric}`}</h2><p className="muted-text">Select a metric card above to inspect the department-scoped records.</p></div>{drilldownLoading && <Search className="spin" size={18} />}</div>{drilldown.rows.length > 0 && <div className="analytics-table-wrap"><table className="modern-table"><thead><tr><th>Record</th><th>Student</th><th>Section</th><th>Mentor</th><th>Title</th><th>Type</th><th>Status/result</th><th>Date</th></tr></thead><tbody>{drilldown.rows.map((row) => <tr key={`${row.record_type}-${row.record_id}`}><td data-label="Record">{row.record_type} #{row.record_id}</td><td data-label="Student">{row.student_name}<small>{row.register_number}</small></td><td data-label="Section">{label(row.section_name)}</td><td data-label="Mentor">{label(row.mentor_name)}</td><td data-label="Title">{label(row.title)}</td><td data-label="Type">{label(row.type_name)}</td><td data-label="Status/result">{row.status}{row.result ? ` · ${row.result}` : ''}</td><td data-label="Date">{row.relevant_date ? new Date(row.relevant_date).toLocaleDateString() : '—'}</td></tr>)}</tbody></table></div>}{drilldown.metric && !drilldown.rows.length && !drilldownLoading && <p className="muted-text">No records match this metric and scope.</p>}</section>
      </>}
    </div>
  );
};

export default HodAnalytics;
