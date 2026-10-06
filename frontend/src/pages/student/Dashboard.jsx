import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMyTickets } from '../../services/ticketService';
import { getMyProfile } from '../../services/studentService';
import { getMyAchievements } from '../../services/achievementService';
import { normalizeStudentProfile } from '../../utils/studentWorkflow';
import { Award, FileText, Clock, CheckCircle, Plus, BookOpen, MapPin, Mail, Hash } from 'lucide-react';

const Dashboard = () => {
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [loadError, setLoadError] = useState('');
  const [stats, setStats] = useState({ total: 0, pendingMentor: 0, pendingHod: 0, approved: 0, postEventPending: 0, correctionPending: 0, achievementPending: 0 });
  const [recentApps, setRecentApps] = useState([]);
  const [pendingActions, setPendingActions] = useState({ returned: [], postEvent: [], achievements: [] });
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchDashboardData = async () => {
      const [profileResult, applicationsResult, achievementsResult] = await Promise.allSettled([getMyProfile(), getMyTickets(), getMyAchievements()]);
      if (profileResult.status === 'fulfilled') {
        setProfile(normalizeStudentProfile(profileResult.value));
      } else {
        setLoadError(profileResult.reason?.message || 'Unable to load your Student profile.');
      }

      if (applicationsResult.status === 'fulfilled') {
        const data = applicationsResult.value;
        const appsList = Array.isArray(data) ? data : [];
        
        const total = appsList.length;
        const approved = appsList.filter(a => a.status === 'APPROVED').length;
        const pendingMentor = appsList.filter(a => a.status === 'SUBMITTED').length; 
        const pendingHod = appsList.filter(a => a.status === 'PENDING_HOD_APPROVAL').length;
        const postEventPending = appsList.filter(a => ['PENDING', 'SUBMITTED'].includes(a.postEventStatus)).length;
        const returned = appsList.filter(a => ['RETURNED_BY_MENTOR', 'RETURNED_BY_HOD'].includes(a.status));
        const postEvent = appsList.filter(a => ['PENDING', 'SUBMITTED'].includes(a.postEventStatus));
        const correctionPending = returned.length;
        const achievements = achievementsResult.status === 'fulfilled' && Array.isArray(achievementsResult.value)
          ? achievementsResult.value.filter(a => ['PENDING_MENTOR_VERIFICATION', 'RESUBMISSION_REQUIRED'].includes(a.verificationStatus)) : [];
        
        setStats({ total, pendingMentor, pendingHod, approved, postEventPending, correctionPending, achievementPending: achievements.length });
        setPendingActions({ returned, postEvent, achievements });
        setRecentApps(appsList.slice(0, 3));
      } else {
        setLoadError((current) => current || applicationsResult.reason?.message || 'Unable to load applications.');
      }
      setIsLoading(false);
    };
    
    fetchDashboardData();
  }, []);

  const getStatusColor = (status) => {
    switch (status) {
      case 'SUBMITTED': return 'status-submitted';
      case 'PENDING_HOD_APPROVAL': return 'status-waitlisted';
      case 'RETURNED_BY_MENTOR': return 'status-waitlisted';
      case 'RETURNED_BY_HOD': return 'status-waitlisted';
      case 'APPROVED': return 'status-approved';
      case 'REJECTED_BY_MENTOR':
      case 'REJECTED_BY_HOD': return 'status-rejected';
      default: return 'status-default';
    }
  };

  return (
    <div className="page-container">
      {loadError && <div className="error-alert" role="alert">{loadError}</div>}
      
      {/* Student Profile Card */}
      <div className="profile-card glass-panel mb-8">
        <div className="profile-hero">
          <h1 className="text-3xl font-bold text-white mb-2">Welcome back, {profile?.name || 'Student'}!</h1>
          <p className="text-blue-100 opacity-90">Student Dashboard</p>
          <div className="profile-action">
            <button className="btn profile-cta shadow-lg" onClick={() => navigate('/student/participations/new')}>
              <Plus size={18} className="mr-2" />
              Apply for Activity / Event
            </button>
          </div>
        </div>
        
        <div className="profile-details-panel">
          <div className="profile-details-grid">
            
            <div className="flex items-center gap-3">
              <div className="bg-slate-100 p-2 rounded-full text-primary"><Hash size={20} /></div>
              <div>
                <p className="text-xs font-bold text-muted uppercase tracking-wider">Register No.</p>
                <p className="font-semibold text-navy">{profile?.registrationNumber || 'Not available'}</p>
              </div>
            </div>

            <div className="flex items-center gap-3">
              <div className="bg-slate-100 p-2 rounded-full text-primary"><Mail size={20} /></div>
              <div>
                <p className="text-xs font-bold text-muted uppercase tracking-wider">SRM Email</p>
                <p className="font-semibold text-navy">{profile?.email || 'Not available'}</p>
              </div>
            </div>

            <div className="flex items-center gap-3">
              <div className="bg-slate-100 p-2 rounded-full text-primary"><BookOpen size={20} /></div>
              <div>
                <p className="text-xs font-bold text-muted uppercase tracking-wider">Department</p>
                <p className="font-semibold text-navy">{profile?.department || 'Not available'}</p>
              </div>
            </div>

            <div className="flex items-center gap-3">
              <div className="bg-slate-100 p-2 rounded-full text-primary"><MapPin size={20} /></div>
              <div>
                <p className="text-xs font-bold text-muted uppercase tracking-wider">Year & Section</p>
                <p className="font-semibold text-navy">
                  {profile?.studentYear && profile?.section ? `${profile.studentYear} Year · Sec ${profile.section}` : 'Not available'}
                </p>
              </div>
            </div>

            <div className="flex items-center gap-3">
              <div className="bg-slate-100 p-2 rounded-full text-primary"><BookOpen size={20} /></div>
              <div><p className="text-xs font-bold text-muted uppercase tracking-wider">Academic Year</p><p className="font-semibold text-navy">{profile?.academicYear || 'Not available'}</p></div>
            </div>

            <div className="flex items-center gap-3">
              <div className="bg-slate-100 p-2 rounded-full text-primary"><Hash size={20} /></div>
              <div><p className="text-xs font-bold text-muted uppercase tracking-wider">Semester</p><p className="font-semibold text-navy">{profile?.semester || 'Not available'}</p></div>
            </div>

            <div className="flex items-center gap-3">
              <div className="bg-slate-100 p-2 rounded-full text-primary"><MapPin size={20} /></div>
              <div><p className="text-xs font-bold text-muted uppercase tracking-wider">Batch</p><p className="font-semibold text-navy">{profile?.batch || 'Not available'}</p></div>
            </div>

          </div>
        </div>
      </div>

      <section className="student-workflow-actions">
        <button className="workflow-action-card glass-panel" onClick={() => navigate('/student/participations/new')}>
          <span className="workflow-action-icon activity-action-icon"><Plus size={22} /></span>
          <span><strong>Apply for Activity / Event</strong><small>Request permission before participating in an activity.</small></span>
        </button>
        <button className="workflow-action-card glass-panel" onClick={() => navigate('/student/achievements/new')}>
          <span className="workflow-action-icon achievement-action-icon"><Award size={22} /></span>
          <span><strong>Add Achievement</strong><small>Submit a course, patent, publication, internship, or other record.</small></span>
        </button>
      </section>

      <div className="student-record-links">
        <button className="btn btn-secondary" onClick={() => navigate('/student/applications')}>My Applications</button>
        <button className="btn btn-secondary" onClick={() => navigate('/student/achievements')}>My Achievements</button>
        <button className="btn btn-primary" onClick={() => navigate('/student/portfolio')}>My Portfolio</button>
      </div>

      {/* KPI Cards */}
      <h2 className="text-xl font-bold text-navy mb-4">Application Statistics</h2>
      <div className="kpi-grid mb-8">
        <div className="kpi-card glass-panel flex items-center p-5">
          <div className="kpi-icon-bg bg-blue-100 p-3 rounded-lg mr-4">
            <FileText size={24} className="text-blue-600" />
          </div>
          <div>
            <p className="text-muted text-sm font-semibold uppercase tracking-wider">Total Requests</p>
            <h3 className="text-2xl font-bold text-navy">{isLoading ? '-' : stats.total}</h3>
          </div>
        </div>

        <div className="kpi-card glass-panel flex items-center p-5">
          <div className="kpi-icon-bg bg-orange-100 p-3 rounded-lg mr-4">
            <Clock size={24} className="text-orange-600" />
          </div>
          <div>
            <p className="text-muted text-sm font-semibold uppercase tracking-wider">Post-activity pending</p>
            <h3 className="text-2xl font-bold text-navy">{isLoading ? '-' : stats.postEventPending}</h3>
          </div>
        </div>
        
        <div className="kpi-card glass-panel flex items-center p-5">
          <div className="kpi-icon-bg bg-amber-100 p-3 rounded-lg mr-4">
            <Clock size={24} className="text-amber-600" />
          </div>
          <div>
            <p className="text-muted text-sm font-semibold uppercase tracking-wider">Pending Mentor</p>
            <h3 className="text-2xl font-bold text-navy">{isLoading ? '-' : stats.pendingMentor}</h3>
          </div>
        </div>

        <div className="kpi-card glass-panel flex items-center p-5">
          <div className="kpi-icon-bg bg-purple-100 p-3 rounded-lg mr-4">
            <Clock size={24} className="text-purple-600" />
          </div>
          <div>
            <p className="text-muted text-sm font-semibold uppercase tracking-wider">Pending HOD</p>
            <h3 className="text-2xl font-bold text-navy">{isLoading ? '-' : stats.pendingHod}</h3>
          </div>
        </div>

        <div className="kpi-card glass-panel flex items-center p-5">
          <div className="kpi-icon-bg bg-green-100 p-3 rounded-lg mr-4">
            <CheckCircle size={24} className="text-green-600" />
          </div>
          <div>
            <p className="text-muted text-sm font-semibold uppercase tracking-wider">Approved Events</p>
            <h3 className="text-2xl font-bold text-navy">{isLoading ? '-' : stats.approved}</h3>
          </div>
        </div>
      </div>

      <section className="student-follow-up-summary glass-panel">
        <div className="flex-between flex-wrap gap-3"><div><h2 className="section-title">Your Pending Actions</h2><p className="page-subtitle">Only actions on your own records are shown.</p></div></div>
        <div className="student-follow-up-actions">
          <button type="button" className="workflow-action-card" onClick={() => navigate(pendingActions.returned[0] ? `/student/applications/${pendingActions.returned[0].applicationCode}` : '/student/applications')}><strong>{stats.correctionPending} Application{stats.correctionPending === 1 ? '' : 's'} Returned for Correction</strong><small>Fix and resubmit the same application.</small></button>
          <button type="button" className="workflow-action-card" onClick={() => navigate(pendingActions.postEvent[0] ? `/student/applications/${pendingActions.postEvent[0].applicationCode}` : '/student/applications')}><strong>{stats.postEventPending} Post-Activity Report{stats.postEventPending === 1 ? '' : 's'} Pending</strong><small>Submit participation details and result information.</small></button>
          <button type="button" className="workflow-action-card" onClick={() => navigate(pendingActions.achievements[0] ? `/student/achievements/${pendingActions.achievements[0].achievementId}` : '/student/achievements')}><strong>{stats.achievementPending} Achievement{stats.achievementPending === 1 ? '' : 's'} Verification Pending</strong><small>View the record and its review status.</small></button>
        </div>
      </section>

      {/* Recent Submissions */}
      <div className="recent-section">
        <div className="flex-between mb-4">
          <h2 className="text-xl font-bold text-navy">Recent Submissions</h2>
          <button className="btn btn-secondary btn-sm" onClick={() => navigate('/student/applications')}>View All</button>
        </div>
        
        {isLoading ? (
          <div className="glass-panel p-8 text-center text-muted">Loading recent activity...</div>
        ) : recentApps.length === 0 ? (
          <div className="glass-panel p-8 text-center text-muted">No recent applications found.</div>
        ) : (
          <div className="recent-grid">
            {recentApps.map(app => (
              <div key={app.applicationId || app.applicationCode} className="glass-panel p-5 recent-card hover:-translate-y-1 transition-transform cursor-pointer" onClick={() => navigate(`/student/applications/${app.applicationCode}`)}>
                <div className="flex-between mb-3">
                  <span className="font-mono text-xs font-bold bg-slate-100 px-2 py-1 rounded">{app.applicationCode || 'Not assigned'}</span>
                  <span className={`status-badge ${getStatusColor(app.status)}`}>{app.status || 'SUBMITTED'}</span>
                </div>
                <h4 className="font-bold text-navy mb-1 truncate" title={app.eventName}>{app.eventName || app.event?.title || 'Unknown Event'}</h4>
                <p className="text-sm text-muted mb-3 truncate">{app.organizingInstitution || app.event?.venue || 'Institution TBA'}</p>
                <div className="text-xs text-muted flex items-center">
                  <Clock size={12} className="mr-1" />
                  Applied: {app.submissionDate ? new Date(app.submissionDate).toLocaleDateString() : 'Recently'}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default Dashboard;
