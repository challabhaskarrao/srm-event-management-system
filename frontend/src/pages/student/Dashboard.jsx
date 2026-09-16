import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { getMyTickets } from '../../services/ticketService';
import { FileText, Clock, CheckCircle, Plus, BookOpen, MapPin, Mail, Hash } from 'lucide-react';

const Dashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [stats, setStats] = useState({ total: 0, pendingMentor: 0, pendingHod: 0, approved: 0 });
  const [recentApps, setRecentApps] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const data = await getMyTickets();
        const appsList = Array.isArray(data) ? data : (data.content || data.data || []);
        
        const total = appsList.length;
        const approved = appsList.filter(a => a.status === 'APPROVED').length;
        const pendingMentor = appsList.filter(a => a.status === 'SUBMITTED').length; 
        const pendingHod = appsList.filter(a => a.status === 'UNDER_REVIEW').length;
        
        setStats({ total, pendingMentor, pendingHod, approved });
        setRecentApps(appsList.slice(0, 3));
      } catch (error) {
        console.error("Failed to load dashboard data", error);
      } finally {
        setIsLoading(false);
      }
    };
    
    fetchDashboardData();
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

  return (
    <div className="page-container">
      
      {/* Student Profile Card */}
      <div className="profile-card glass-panel mb-8">
        <div className="profile-hero">
          <h1 className="text-3xl font-bold text-white mb-2">Welcome back, {user?.name || user?.fullName || 'Student'}!</h1>
          <p className="text-blue-100 opacity-90">Student Dashboard &middot; Academic Year 2026-2027</p>
          <div className="profile-action">
            <button className="btn profile-cta shadow-lg" onClick={() => navigate('/apply')}>
              <Plus size={18} className="mr-2" />
              New Request
            </button>
          </div>
        </div>
        
        <div className="profile-details-panel">
          <div className="profile-details-grid">
            
            <div className="flex items-center gap-3">
              <div className="bg-slate-100 p-2 rounded-full text-primary"><Hash size={20} /></div>
              <div>
                <p className="text-xs font-bold text-muted uppercase tracking-wider">Register No.</p>
                <p className="font-semibold text-navy">{user?.registrationNumber || 'RA2311003010001'}</p>
              </div>
            </div>

            <div className="flex items-center gap-3">
              <div className="bg-slate-100 p-2 rounded-full text-primary"><Mail size={20} /></div>
              <div>
                <p className="text-xs font-bold text-muted uppercase tracking-wider">SRM Email</p>
                <p className="font-semibold text-navy">{user?.email || 'student@srmist.edu.in'}</p>
              </div>
            </div>

            <div className="flex items-center gap-3">
              <div className="bg-slate-100 p-2 rounded-full text-primary"><BookOpen size={20} /></div>
              <div>
                <p className="text-xs font-bold text-muted uppercase tracking-wider">Department</p>
                <p className="font-semibold text-navy">{user?.department || 'Computer Science (CSE)'}</p>
              </div>
            </div>

            <div className="flex items-center gap-3">
              <div className="bg-slate-100 p-2 rounded-full text-primary"><MapPin size={20} /></div>
              <div>
                <p className="text-xs font-bold text-muted uppercase tracking-wider">Year & Section</p>
                <p className="font-semibold text-navy">{user?.year || '3rd'} Year &middot; Sec {user?.section || 'A'}</p>
              </div>
            </div>

          </div>
        </div>
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

      {/* Recent Submissions */}
      <div className="recent-section">
        <div className="flex-between mb-4">
          <h2 className="text-xl font-bold text-navy">Recent Submissions</h2>
          <button className="btn btn-secondary btn-sm" onClick={() => navigate('/my-applications')}>View All</button>
        </div>
        
        {isLoading ? (
          <div className="glass-panel p-8 text-center text-muted">Loading recent activity...</div>
        ) : recentApps.length === 0 ? (
          <div className="glass-panel p-8 text-center text-muted">No recent applications found.</div>
        ) : (
          <div className="recent-grid">
            {recentApps.map(app => (
              <div key={app.id || app.applicationCode} className="glass-panel p-5 recent-card hover:-translate-y-1 transition-transform cursor-pointer" onClick={() => navigate(`/applications/${app.applicationCode || app.id}`)}>
                <div className="flex-between mb-3">
                  <span className="font-mono text-xs font-bold bg-slate-100 px-2 py-1 rounded">{app.applicationCode || 'APP-MOCK'}</span>
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
