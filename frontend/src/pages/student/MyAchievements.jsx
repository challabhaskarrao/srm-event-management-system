import React, { useEffect, useState } from 'react';
import { Award, RefreshCw } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { getMyAchievements } from '../../services/achievementService';

const statusClass = (status) => status === 'VERIFIED' ? 'status-approved' : status === 'REJECTED' ? 'status-rejected' : 'status-waitlisted';

const MyAchievements = () => {
  const navigate = useNavigate();
  const [achievements, setAchievements] = useState([]); const [loading, setLoading] = useState(true); const [error, setError] = useState('');
  const load = async () => { setLoading(true); setError(''); try { setAchievements(await getMyAchievements() || []); } catch (requestError) { setError(requestError.message || 'Unable to load achievements.'); } finally { setLoading(false); } };
  useEffect(() => { load(); }, []);
  return <div className="page-container"><header className="page-header flex-between flex-wrap gap-4"><div><h1 className="page-title">My Achievements</h1><p className="page-subtitle">Academic and professional records you have already earned; review status is shown where applicable.</p></div><button className="btn btn-secondary" onClick={load} disabled={loading}><RefreshCw size={17} /> Refresh</button></header>{error && <div className="error-alert" role="alert">{error}</div>}{loading ? <div className="loading-spinner">Loading achievements…</div> : achievements.length === 0 ? <div className="empty-state"><Award size={44} className="empty-icon" /><p>No achievement records have been submitted yet.</p><button className="btn btn-primary" onClick={() => navigate('/student/achievements/new')}>Record Achievement</button></div> : <div className="achievement-list">{achievements.map((achievement) => <button className="achievement-list-card glass-panel" key={achievement.achievementId} onClick={() => navigate(`/student/achievements/${achievement.achievementId}`)}><div><span className="category-text">{achievement.eventTypeName}</span><h2>{achievement.title}</h2><p>{achievement.organization}</p></div><div className="achievement-list-meta"><span className={`status-badge ${statusClass(achievement.verificationStatus)}`}>{achievement.verificationStatus.replaceAll('_', ' ')}</span><small>{achievement.endDate || achievement.startDate || 'Date not provided'}</small></div></button>)}</div>}</div>;
};

export default MyAchievements;
