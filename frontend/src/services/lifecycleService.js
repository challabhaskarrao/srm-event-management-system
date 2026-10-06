import { downloadApi, fetchApi } from './api.js';

export const getDocumentTypes = () => fetchApi('/documents/types');
export const getDocuments = (applicationId) => fetchApi(`/applications/${applicationId}/documents`);
export const uploadDocument = (applicationId, documentTypeId, file) => {
  const data = new FormData();
  data.append('file', file);
  return fetchApi(`/applications/${applicationId}/documents?documentTypeId=${encodeURIComponent(documentTypeId)}`, {
    method: 'POST', body: data,
  });
};
export const downloadDocument = async (document) => {
  const blob = await downloadApi(`/documents/${document.documentId}/download`);
  const url = URL.createObjectURL(blob);
  const anchor = window.document.createElement('a');
  anchor.href = url;
  anchor.download = document.originalFileName;
  anchor.click();
  URL.revokeObjectURL(url);
};
export const viewDocument = async (document) => {
  const blob = await downloadApi(`/documents/${document.documentId}/download`);
  const url = URL.createObjectURL(blob);
  const tab = window.open(url, '_blank', 'noopener,noreferrer');
  if (!tab) {
    const anchor = window.document.createElement('a');
    anchor.href = url;
    anchor.target = '_blank';
    anchor.rel = 'noreferrer';
    anchor.click();
  }
  window.setTimeout(() => URL.revokeObjectURL(url), 60_000);
};
export const getAchievement = (applicationId) => fetchApi(`/applications/${applicationId}/achievement`);
export const submitAchievement = (applicationId, payload) => fetchApi(`/applications/${applicationId}/achievement`, {
  method: 'POST', body: JSON.stringify(payload),
});
export const reviewAchievement = (applicationId, payload) => fetchApi(`/applications/${applicationId}/achievement`, {
  method: 'PUT', body: JSON.stringify(payload),
});
export const submitPostEventReport = (applicationId, payload) => fetchApi(`/participations/${applicationId}/post-event`, {
  method: 'POST', body: JSON.stringify(payload),
});
