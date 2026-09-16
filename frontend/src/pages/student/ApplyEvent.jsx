import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { registerApplication } from '../../services/ticketService';
import { Calendar, MapPin, Briefcase, Users, Link as LinkIcon, FileText, Upload, Plus, Trash2 } from 'lucide-react';

const ApplyEvent = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    eventType: 'HACKATHON',
    eventName: '',
    organizingInstitution: '',
    eventMode: 'OFFLINE',
    startDate: '',
    endDate: '',
    eventUrl: '',
    
    participationType: 'INDIVIDUAL',
    numMembers: 2,
    teamMembers: [],
    
    isOutstation: false,
    modeOfTravel: 'TRAIN',
    departureDate: '',
    returnDate: '',
    accommodationDetails: '',
    emergencyContactName: '',
    emergencyContactPhone: '',
    
    justification: ''
  });

  const [files, setFiles] = useState({
    brochure: null,
    selectionLetter: null,
    poster: null
  });
  
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const eventTypes = [
    'HACKATHON', 'WORKSHOP', 'PAPER_PRESENTATION', 'CONFERENCE', 
    'TECHNICAL_SYMPOSIUM', 'PROJECT_EXPO', 'CULTURAL', 'SPORTS', 'OTHER'
  ];

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleMemberChange = (index, field, value) => {
    const updatedMembers = [...formData.teamMembers];
    if (!updatedMembers[index]) {
      updatedMembers[index] = { fullName: '', registerNumber: '', department: '', section: '' };
    }
    updatedMembers[index][field] = value;
    setFormData(prev => ({ ...prev, teamMembers: updatedMembers }));
  };

  // Adjust team members array size based on numMembers input
  React.useEffect(() => {
    if (formData.participationType === 'TEAM') {
      const count = parseInt(formData.numMembers, 10) || 2;
      const currentMembers = [...formData.teamMembers];
      
      // If we need more fields, add them. We assume the current user is member 1 (implicit or explicit), 
      // but let's just create 'count' number of fields for simplicity.
      while (currentMembers.length < count) {
        currentMembers.push({ fullName: '', registerNumber: '', department: 'CSE', section: '' });
      }
      // If we have too many, truncate
      if (currentMembers.length > count) {
        currentMembers.length = count;
      }
      
      setFormData(prev => ({ ...prev, teamMembers: currentMembers }));
    }
  }, [formData.numMembers, formData.participationType]);

  const handleFileChange = (e, fileType) => {
    if (e.target.files && e.target.files[0]) {
      setFiles(prev => ({ ...prev, [fileType]: e.target.files[0] }));
    }
  };

  const removeFile = (fileType) => {
    setFiles(prev => ({ ...prev, [fileType]: null }));
    // Reset file input value
    const input = document.getElementById(`file-${fileType}`);
    if(input) input.value = '';
  };

  const validateForm = () => {
    if (new Date(formData.startDate) > new Date(formData.endDate)) {
      return 'End Date cannot be before Start Date.';
    }
    
    if (formData.isOutstation) {
      if (!formData.departureDate || !formData.returnDate) return 'Departure and Return dates are required for outstation travel.';
      if (new Date(formData.departureDate) > new Date(formData.returnDate)) return 'Return date cannot be before Departure date.';
      if (!formData.emergencyContactName || !formData.emergencyContactPhone) return 'Emergency contact details are required.';
    }

    if (formData.participationType === 'TEAM') {
      const emptyMember = formData.teamMembers.find(m => !m.fullName || !m.registerNumber || !m.section);
      if (emptyMember) return 'Please fill all details for all team members.';
    }

    return null;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    
    const validationError = validateForm();
    if (validationError) {
      setError(validationError);
      return;
    }

    setIsSubmitting(true);

    try {
      // Create FormData if we actually had a backend supporting multipart/form-data
      // For now, we simulate sending the JSON payload.
      const payload = {
        ...formData
      };
      
      await registerApplication(payload);
      navigate('/my-applications');
    } catch (err) {
      setError(err.message || 'Failed to submit application. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const FileUploader = ({ label, type }) => (
    <div className="file-upload-box">
      <label className="file-upload-label">{label}</label>
      {files[type] ? (
        <div className="file-preview glass-panel">
          <div className="flex items-center gap-2">
            <FileText size={20} className="text-primary" />
            <span className="file-name truncate" title={files[type].name}>{files[type].name}</span>
          </div>
          <button type="button" className="icon-btn text-red-500 hover:text-red-700" onClick={() => removeFile(type)}>
            <Trash2 size={16} />
          </button>
        </div>
      ) : (
        <div className="file-upload-input-wrapper">
          <input 
            type="file" 
            id={`file-${type}`}
            accept=".pdf,.png,.jpg,.jpeg" 
            onChange={(e) => handleFileChange(e, type)} 
            className="hidden-file-input"
          />
          <label htmlFor={`file-${type}`} className="btn btn-secondary btn-sm flex items-center justify-center gap-2 cursor-pointer border-dashed border-2">
            <Upload size={16} /> Choose File
          </label>
        </div>
      )}
    </div>
  );

  return (
    <div className="page-container">
      <div className="page-header">
        <h1 className="page-title">Create Participation Request</h1>
        <p className="page-subtitle">Submit a request to participate in an external event (OD).</p>
      </div>

      <div className="form-card glass-panel">
        {error && <div className="error-alert">{error}</div>}

        <form onSubmit={handleSubmit} className="admin-form">
          {/* Section 1: Basic Info */}
          <div className="form-section">
            <h3 className="section-title"><Briefcase size={18}/> 1. Event Information</h3>
            
            <div className="form-row">
              <div className="form-group">
                <label>Event Name</label>
                <input type="text" name="eventName" value={formData.eventName} onChange={handleChange} required placeholder="e.g. Smart India Hackathon" />
              </div>
              <div className="form-group">
                <label>Organizing Institution / University</label>
                <input type="text" name="organizingInstitution" value={formData.organizingInstitution} onChange={handleChange} required placeholder="e.g. IIT Madras" />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Event Type</label>
                <select name="eventType" value={formData.eventType} onChange={handleChange}>
                  {eventTypes.map(type => (
                    <option key={type} value={type}>{type.replace('_', ' ')}</option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label>Event Mode</label>
                <select name="eventMode" value={formData.eventMode} onChange={handleChange}>
                  <option value="OFFLINE">OFFLINE</option>
                  <option value="ONLINE">ONLINE</option>
                  <option value="HYBRID">HYBRID</option>
                </select>
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Start Date</label>
                <input type="date" name="startDate" value={formData.startDate} onChange={handleChange} required />
              </div>
              <div className="form-group">
                <label>End Date</label>
                <input type="date" name="endDate" value={formData.endDate} onChange={handleChange} required />
              </div>
            </div>

            <div className="form-group">
              <label>Event Website / Reference URL</label>
              <div className="input-with-icon">
                <LinkIcon size={18} className="input-icon" />
                <input type="url" name="eventUrl" value={formData.eventUrl} onChange={handleChange} required placeholder="https://" style={{paddingLeft: '2.5rem'}} />
              </div>
            </div>
          </div>

          {/* Section 2: Media Upload */}
          <div className="form-section">
            <h3 className="section-title"><Upload size={18}/> 2. Supporting Documents</h3>
            <p className="text-sm text-muted mb-4">Upload relevant files. Max size 5MB per file (.pdf, .jpg, .png)</p>
            
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <FileUploader label="Event Brochure" type="brochure" />
              <FileUploader label="Selection Letter (if any)" type="selectionLetter" />
              <FileUploader label="Event Poster" type="poster" />
            </div>
          </div>

          {/* Section 3: Participation */}
          <div className="form-section">
            <h3 className="section-title"><Users size={18}/> 3. Participation Details</h3>
            
            <div className="form-row">
              <div className="form-group">
                <label>Participation Type</label>
                <select name="participationType" value={formData.participationType} onChange={handleChange}>
                  <option value="INDIVIDUAL">Individual</option>
                  <option value="TEAM">Team</option>
                </select>
              </div>
              
              {formData.participationType === 'TEAM' && (
                <div className="form-group">
                  <label>Number of Members</label>
                  <input type="number" name="numMembers" value={formData.numMembers} onChange={handleChange} min="2" max="10" required />
                </div>
              )}
            </div>

            {formData.participationType === 'TEAM' && (
              <div className="team-members-container bg-slate-50 p-4 rounded-lg border border-slate-200 mt-4">
                <h4 className="font-bold text-navy mb-3">Team Member Details</h4>
                {formData.teamMembers.map((member, index) => (
                  <div key={index} className="member-subform mb-4 pb-4 border-b border-slate-200 last:border-0 last:pb-0 last:mb-0">
                    <p className="text-sm font-semibold text-primary mb-2">Member {index + 1}</p>
                    <div className="form-row" style={{marginBottom: '1rem'}}>
                      <input type="text" placeholder="Full Name" required value={member.fullName} onChange={(e) => handleMemberChange(index, 'fullName', e.target.value)} className="form-control" />
                      <input type="text" placeholder="Register Number" required value={member.registerNumber} onChange={(e) => handleMemberChange(index, 'registerNumber', e.target.value)} className="form-control" />
                    </div>
                    <div className="form-row">
                      <select required value={member.department} onChange={(e) => handleMemberChange(index, 'department', e.target.value)} className="form-control">
                        <option value="CSE">CSE</option>
                        <option value="IT">IT</option>
                        <option value="ECE">ECE</option>
                        <option value="MECH">MECH</option>
                        <option value="OTHER">Other</option>
                      </select>
                      <input type="text" placeholder="Section (e.g. A)" required value={member.section} onChange={(e) => handleMemberChange(index, 'section', e.target.value)} className="form-control" maxLength="2" />
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Section 4: Travel / Outstation */}
          {(formData.eventMode === 'OFFLINE' || formData.eventMode === 'HYBRID') && (
            <div className="form-section">
              <h3 className="section-title"><MapPin size={18}/> 4. Travel & Accommodation</h3>
              
              <div className="form-group checkbox-group bg-slate-50 p-3 rounded-lg border border-slate-200">
                <label className="checkbox-label">
                  <input type="checkbox" name="isOutstation" checked={formData.isOutstation} onChange={handleChange} />
                  <span className="font-semibold">Is this event outside Tamil Nadu / Outstation?</span>
                </label>
              </div>
              
              {formData.isOutstation && (
                <div className="outstation-details mt-4 pt-4 border-t border-dashed border-slate-300">
                  <div className="form-row">
                    <div className="form-group">
                      <label>Mode of Travel</label>
                      <select name="modeOfTravel" value={formData.modeOfTravel} onChange={handleChange}>
                        <option value="FLIGHT">Flight</option>
                        <option value="TRAIN">Train</option>
                        <option value="BUS">Bus</option>
                        <option value="SELF">Self-arranged</option>
                      </select>
                    </div>
                  </div>
                  
                  <div className="form-row">
                    <div className="form-group">
                      <label>Departure Date</label>
                      <input type="date" name="departureDate" value={formData.departureDate} onChange={handleChange} required={formData.isOutstation} />
                    </div>
                    <div className="form-group">
                      <label>Return Date</label>
                      <input type="date" name="returnDate" value={formData.returnDate} onChange={handleChange} required={formData.isOutstation} />
                    </div>
                  </div>

                  <div className="form-group">
                    <label>Accommodation Details / Venue Address</label>
                    <textarea name="accommodationDetails" value={formData.accommodationDetails} onChange={handleChange} rows="2" placeholder="Hotel Name, Full Address..." required={formData.isOutstation}></textarea>
                  </div>

                  <div className="form-row">
                    <div className="form-group">
                      <label>Emergency Contact Name</label>
                      <input type="text" name="emergencyContactName" value={formData.emergencyContactName} onChange={handleChange} required={formData.isOutstation} />
                    </div>
                    <div className="form-group">
                      <label>Emergency Contact Phone</label>
                      <input type="tel" name="emergencyContactPhone" value={formData.emergencyContactPhone} onChange={handleChange} required={formData.isOutstation} />
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Section 5: Justification */}
          <div className="form-section">
            <h3 className="section-title"><FileText size={18}/> 5. Remarks / Justification</h3>
            <div className="form-group">
              <textarea name="justification" value={formData.justification} onChange={handleChange} rows="4" required placeholder="Describe how participating in this event benefits your academic or professional growth. This will be reviewed by your Mentor and HOD."></textarea>
            </div>
          </div>

          <div className="form-actions pt-6">
            <button type="button" className="btn btn-secondary" onClick={() => navigate(-1)}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
              {isSubmitting ? 'Submitting...' : 'Submit Request'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ApplyEvent;
