import React, { useCallback, useEffect, useState } from 'react';
import { FileText, Trophy, Upload } from 'lucide-react';
import {
  getAchievement, getDocuments, getDocumentTypes,
  reviewAchievement, uploadDocument,
} from '../services/lifecycleService';
import AchievementEvidence from './AchievementEvidence';

const ApplicationLifecyclePanel = ({ application, role }) => {
  const [documents, setDocuments] = useState([]);
  const [types, setTypes] = useState([]);
  const [achievement, setAchievement] = useState(null);
  const [selectedType, setSelectedType] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const reload = useCallback(async () => {
    const [documentList, typeList, resultRecord] = await Promise.all([
      getDocuments(application.applicationId), getDocumentTypes(), getAchievement(application.applicationId),
    ]);
    setDocuments(Array.isArray(documentList) ? documentList : []);
    setTypes(Array.isArray(typeList) ? typeList : []);
    setAchievement(resultRecord || null);
  }, [application.applicationId]);

  useEffect(() => { reload().catch((requestError) => setError(requestError.message)); }, [reload]);

  const submitFile = async (event) => {
    event.preventDefault(); setError(''); setBusy(true);
    try { await uploadDocument(application.applicationId, selectedType, selectedFile); setSelectedFile(null); await reload(); }
    catch (requestError) { setError(requestError.message); }
    finally { setBusy(false); }
  };

  const verifyResult = async (status) => {
    const remarks = status === 'VERIFIED' ? '' : window.prompt('Verification remarks are required:');
    if (remarks === null || (status !== 'VERIFIED' && !remarks.trim())) return;
    setBusy(true); setError('');
    try { await reviewAchievement(application.applicationId, { status, remarks }); await reload(); }
    catch (requestError) { setError(requestError.message); }
    finally { setBusy(false); }
  };

  const eligibleTypes = types.filter((type) => (
    type.documentPhase === 'PRE_EVENT'
      ? ['SUBMITTED', 'RETURNED_BY_MENTOR', 'RETURNED_BY_HOD'].includes(application.status)
      : application.status === 'APPROVED' && ['PENDING', 'SUBMITTED', 'COMPLETE'].includes(application.postEventStatus)
  ));
  const postEventTypes = eligibleTypes.filter((type) => type.documentPhase === 'POST_EVENT');

  return (
    <div className="lifecycle-panels">
      {error && <div className="error-alert" role="alert">{error}</div>}
      <section className="glass-panel lifecycle-panel">
        <h2 className="section-title"><FileText size={18} /> Documents</h2>
        <AchievementEvidence applicationId={application.applicationId} initialDocuments={documents} emptyLabel="No documents uploaded." />
        {role === 'STUDENT' && postEventTypes.length > 0 && (
          <p className="participation-help post-event-evidence-help">Post-activity evidence: upload your participation certificate/proof, result proof, event photos, or another configured supporting document. Save the participation report first when required.</p>
        )}
        {role === 'STUDENT' && eligibleTypes.length > 0 && (
          <form className="document-upload-form" onSubmit={submitFile}>
            <select value={selectedType} onChange={(event) => setSelectedType(event.target.value)} required>
              <option value="">Select document type</option>
              {eligibleTypes.map((type) => <option key={type.documentTypeId} value={type.documentTypeId}>{type.documentName}</option>)}
            </select>
            <input type="file" accept=".pdf,.png,.jpg,.jpeg" onChange={(event) => setSelectedFile(event.target.files?.[0] || null)} required />
            <button className="btn btn-primary" disabled={busy}><Upload size={16} /> Upload</button>
          </form>
        )}
      </section>

      <section className="glass-panel lifecycle-panel">
        <h2 className="section-title"><Trophy size={18} /> Participation result</h2>
        {achievement ? (
          <div className="achievement-summary">
            <strong>{achievement.resultType.replaceAll('_', ' ')}</strong>
            {achievement.position && <span>Position: {achievement.position}</span>}
            {achievement.prizeDetails && <span>{achievement.prizeDetails}</span>}
            <span className="status-badge status-default">{achievement.verificationStatus}</span>
            {achievement.remarks && <p>{achievement.remarks}</p>}
            {role === 'ADMIN' && achievement.verificationStatus === 'PENDING' && (
              <div className="form-actions"><button className="btn btn-secondary" onClick={() => verifyResult('REJECTED')} disabled={busy}>Reject</button><button className="btn btn-primary" onClick={() => verifyResult('VERIFIED')} disabled={busy}>Verify</button></div>
            )}
          </div>
        ) : <p className="muted-text">Use the Post-activity report above to record participation and the result.</p>}
      </section>
    </div>
  );
};

export default ApplicationLifecyclePanel;
