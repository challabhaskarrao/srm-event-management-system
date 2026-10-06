import React, { useEffect, useState } from 'react';
import { Download, FileText, Image, ExternalLink } from 'lucide-react';
import { downloadApi } from '../services/api';
import { getAchievementDocuments } from '../services/achievementService';
import { downloadDocument, getDocuments, viewDocument } from '../services/lifecycleService';

const AchievementEvidence = ({ achievementId, applicationId, initialDocuments, emptyLabel = 'No evidence uploaded.' }) => {
  const [documents, setDocuments] = useState(initialDocuments || null);
  const [previews, setPreviews] = useState({});
  const [error, setError] = useState('');
  const [actionError, setActionError] = useState('');

  useEffect(() => {
    if (initialDocuments) {
      setDocuments(initialDocuments);
      return undefined;
    }
    let active = true;
    const load = achievementId ? getAchievementDocuments(achievementId) : getDocuments(applicationId);
    load
      .then((items) => { if (active) setDocuments(items || []); })
      .catch(() => { if (active) { setError(`Unable to load ${achievementId ? 'achievement evidence' : 'activity documents'}.`); setDocuments([]); } });
    return () => { active = false; };
  }, [achievementId, applicationId, initialDocuments]);

  useEffect(() => {
    const imageDocuments = (documents || []).filter((document) => document.mimeType?.startsWith('image/'));
    let active = true;
    const urls = [];
    Promise.all(imageDocuments.map(async (document) => {
      try {
        const url = URL.createObjectURL(await downloadApi(`/documents/${document.documentId}/download`));
        if (!active) {
          URL.revokeObjectURL(url);
          return;
        }
        urls.push(url);
        setPreviews((current) => ({ ...current, [document.documentId]: url }));
      } catch { /* the authorized Download action remains available */ }
    }));
    return () => { active = false; urls.forEach((url) => URL.revokeObjectURL(url)); };
  }, [documents]);

  if (documents === null) return <div className="loading-spinner">Loading evidence…</div>;
  if (documents.length === 0) return <>{error && <p className="error-alert" role="alert">{error}</p>}<p className="muted-text">{emptyLabel}</p></>;

  const runDocumentAction = (action, document) => {
    setActionError('');
    action(document).catch(() => setActionError('Unable to open this document. Please try Download instead.'));
  };

  return <div className="document-list">{(error || actionError) && <p className="error-alert" role="alert">{error || actionError}</p>}{documents.map((document) => {
    const image = document.mimeType?.startsWith('image/');
    return <div className="document-row achievement-evidence-row" key={document.documentId}>
      <div className="achievement-evidence-info">
        {image && previews[document.documentId] ? <a href={previews[document.documentId]} target="_blank" rel="noreferrer"><img className="achievement-evidence-thumb" src={previews[document.documentId]} alt={document.originalFileName} /></a> : image ? <Image size={24} /> : <FileText size={24} />}
        <span><strong>{document.documentName}</strong><small>{document.originalFileName}{document.uploadedAt ? ` · ${new Date(document.uploadedAt).toLocaleDateString()}` : ''}</small></span>
      </div>
      <div className="achievement-evidence-actions"><button type="button" className="btn btn-secondary btn-sm" onClick={() => runDocumentAction(viewDocument, document)}><ExternalLink size={15} /> View</button><button type="button" className="btn btn-secondary btn-sm" onClick={() => runDocumentAction(downloadDocument, document)}><Download size={15} /> Download</button></div>
    </div>;
  })}</div>;
};

export default AchievementEvidence;
