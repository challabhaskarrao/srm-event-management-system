import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getTicketByCode } from '../../services/ticketService';
import { ArrowLeft, Building, MapPin, Calendar, Users, Globe, Plane, CheckCircle2, MessageSquare } from 'lucide-react';

const ApplicationDetails = () => {
  const { code } = useParams();
  const navigate = useNavigate();
  const [app, setApp] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchDetails = async () => {
      try {
        const data = await getTicketByCode(code);
        setApp(data);
      } catch (err) {
        setError('Failed to load application details.');
      } finally {
        setIsLoading(false);
      }
    };
    fetchDetails();
  }, [code]);

  if (isLoading) return <div className="loading-spinner">Loading application...</div>;
  if (error) return <div className="error-alert">{error}</div>;
  if (!app) return <div className="empty-state">Application not found</div>;

  // Determine current step index based on status
  // 1: Submitted, 2: Mentor Review, 3: HOD Review, 4: Approved/Final
  let currentStep = 1;
  const status = app.status || 'SUBMITTED';
  if (status === 'SUBMITTED') currentStep = 1;
  if (status === 'UNDER_REVIEW') currentStep = 2; // Assuming Mentor Review
  if (status === 'HOD_REVIEW') currentStep = 3;
  if (status === 'APPROVED' || status === 'REJECTED') currentStep = 4;

  const StepIndicator = ({ stepNum, label, isCompleted, isCurrent, isRejected }) => {
    let circleClass = "step-circle text-muted border-slate-300";
    if (isCompleted) circleClass = "step-circle bg-green-500 text-white border-green-500";
    else if (isCurrent) circleClass = "step-circle bg-primary text-white border-primary";
    
    if (isRejected && (isCurrent || isCompleted)) circleClass = "step-circle bg-red-500 text-white border-red-500";

    return (
      <div className="step-indicator">
        <div className={circleClass}>
          {isCompleted && !isRejected ? <CheckCircle2 size={16} /> : stepNum}
        </div>
        <span className={`step-label ${isCurrent ? 'font-bold text-navy' : 'text-muted'}`}>{label}</span>
      </div>
    );
  };

  return (
    <div className="page-container">
      <button className="back-btn" onClick={() => navigate('/my-applications')}>
        <ArrowLeft size={16} /> Back to Applications
      </button>

      <div className="app-detail-header glass-panel">
        <div className="flex-between flex-wrap gap-4">
          <div>
            <h1 className="text-2xl font-bold text-navy">{app.eventName || app.event?.title || 'Unknown Event'}</h1>
            <p className="text-muted flex items-center gap-2 mt-2">
              <Building size={16} /> {app.organizingInstitution || app.event?.venue || 'Institution TBA'}
            </p>
          </div>
          <div className="text-right">
            <div className="font-mono text-lg font-bold text-navy mb-2">{app.applicationCode || code}</div>
            <span className={`status-badge ${status === 'APPROVED' ? 'status-approved' : status === 'REJECTED' ? 'status-rejected' : 'status-submitted'}`}>
              {status}
            </span>
          </div>
        </div>

        {/* Multi-Stage Approval Tracker */}
        <div className="progress-tracker mt-8 pt-6 border-t border-slate-200">
          <h4 className="font-bold text-navy mb-4">Approval Status</h4>
          <div className="stepper-container">
            <StepIndicator stepNum={1} label="Submitted" isCompleted={currentStep > 1 || status === 'APPROVED'} isCurrent={currentStep === 1} isRejected={false} />
            <div className={`step-line ${currentStep > 1 ? 'line-active' : ''}`}></div>
            <StepIndicator stepNum={2} label="Mentor Review" isCompleted={currentStep > 2 || status === 'APPROVED'} isCurrent={currentStep === 2} isRejected={status === 'REJECTED' && currentStep === 2} />
            <div className={`step-line ${currentStep > 2 ? 'line-active' : ''}`}></div>
            <StepIndicator stepNum={3} label="HOD Review" isCompleted={currentStep > 3 || status === 'APPROVED'} isCurrent={currentStep === 3} isRejected={status === 'REJECTED' && currentStep === 3} />
            <div className={`step-line ${currentStep > 3 || status === 'APPROVED' ? 'line-active' : ''}`}></div>
            <StepIndicator stepNum={4} label="Final Decision" isCompleted={status === 'APPROVED'} isCurrent={currentStep === 4} isRejected={status === 'REJECTED'} />
          </div>
        </div>
      </div>

      <div className="app-detail-grid">
        <div className="app-main-info flex flex-col gap-6">
          <div className="glass-panel">
            <h3 className="section-title">Event Information</h3>
            <div className="info-grid">
              <div className="info-item">
                <span className="info-label">Type</span>
                <span className="info-value">{app.eventType || 'N/A'}</span>
              </div>
              <div className="info-item">
                <span className="info-label">Mode</span>
                <span className="info-value">{app.eventMode || 'N/A'}</span>
              </div>
              <div className="info-item">
                <span className="info-label">Dates</span>
                <span className="info-value flex items-center gap-2">
                  <Calendar size={14}/> {app.startDate ? new Date(app.startDate).toLocaleDateString() : 'N/A'} - {app.endDate ? new Date(app.endDate).toLocaleDateString() : 'N/A'}
                </span>
              </div>
              <div className="info-item">
                <span className="info-label">Location</span>
                <span className="info-value flex items-center gap-2">
                  <MapPin size={14}/> {app.eventCity || 'N/A'}
                </span>
              </div>
              <div className="info-item col-span-2">
                <span className="info-label">Website</span>
                <a href={app.eventUrl || '#'} target="_blank" rel="noopener noreferrer" className="info-value flex items-center gap-2 text-primary hover-underline">
                  <Globe size={14}/> {app.eventUrl || 'No Link Provided'}
                </a>
              </div>
              <div className="info-item col-span-2 mt-4 border-t pt-4">
                <span className="info-label">Justification</span>
                <p className="info-value mt-2">{app.justification || 'No justification provided.'}</p>
              </div>
            </div>
          </div>

          {/* Remarks Section */}
          <div className="glass-panel">
            <h3 className="section-title flex items-center gap-2"><MessageSquare size={18}/> Reviewer Remarks</h3>
            <div className="remarks-container mt-4">
              {app.mentorRemarks ? (
                <div className="remark-box bg-slate-50 p-4 rounded-lg mb-3 border border-slate-200">
                  <p className="text-xs font-bold text-muted uppercase mb-1">Mentor Remark</p>
                  <p className="text-navy">{app.mentorRemarks}</p>
                </div>
              ) : (
                <p className="text-muted text-sm italic mb-2">No remarks from Mentor yet.</p>
              )}
              
              {app.hodRemarks ? (
                <div className="remark-box bg-slate-50 p-4 rounded-lg border border-slate-200">
                  <p className="text-xs font-bold text-muted uppercase mb-1">HOD Remark</p>
                  <p className="text-navy">{app.hodRemarks}</p>
                </div>
              ) : (
                <p className="text-muted text-sm italic">No remarks from HOD yet.</p>
              )}
            </div>
          </div>
        </div>

        <div className="app-side-info flex flex-col gap-6">
          <div className="glass-panel">
            <h3 className="section-title flex items-center gap-2"><Users size={18}/> Participation</h3>
            <div className="info-item mt-4">
              <span className="info-label">Type</span>
              <span className="info-value">{app.participationType || 'INDIVIDUAL'}</span>
            </div>
            {app.participationType === 'TEAM' && (
              <>
                <div className="info-item mt-3">
                  <span className="info-label">Team Members</span>
                  <span className="info-value">{app.numMembers || 'N/A'}</span>
                </div>
                {/* Could map through app.teamMembers array here if backend stores it */}
              </>
            )}
          </div>

          {app.isOutstation && (
            <div className="glass-panel border-l-4 border-amber-500">
              <h3 className="section-title flex items-center gap-2"><Plane size={18}/> Travel Details</h3>
              <div className="info-item mt-4">
                <span className="info-label">Mode of Travel</span>
                <span className="info-value">{app.modeOfTravel || 'N/A'}</span>
              </div>
              <div className="info-item mt-3">
                <span className="info-label">Travel Dates</span>
                <span className="info-value text-sm">
                  {app.departureDate ? new Date(app.departureDate).toLocaleDateString() : 'N/A'} to {app.returnDate ? new Date(app.returnDate).toLocaleDateString() : 'N/A'}
                </span>
              </div>
              <div className="info-item mt-3">
                <span className="info-label">Accommodation / Venue</span>
                <span className="info-value text-sm block mt-1">{app.accommodationDetails || 'N/A'}</span>
              </div>
              <div className="info-item mt-3 border-t border-slate-200 pt-3">
                <span className="info-label text-red-500 font-bold">Emergency Contact</span>
                <span className="info-value text-sm block mt-1">{app.emergencyContactName} ({app.emergencyContactPhone})</span>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ApplicationDetails;
