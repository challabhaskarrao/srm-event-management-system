export const normalizeEventList = (response) => (
  Array.isArray(response)
    ? response.filter((event) => event && Number.isInteger(Number(event.eventId)) && event.eventName)
    : []
);

export const buildApplicationPayload = (eventId, form) => ({
  eventId: Number(eventId),
  participationType: form.participationType,
  travelRequired: Boolean(form.travelRequired),
  outsideState: Boolean(form.outsideState),
  teamName: form.participationType === 'TEAM' ? form.teamName.trim() : null,
  teamMemberRegistrationNumbers: form.participationType === 'TEAM'
    ? (form.teamMemberRegistrationNumbers || '').split(',').map((value) => value.trim()).filter(Boolean)
    : [],
});

export const buildEventPayload = (form) => ({
  name: form.name.trim(),
  categoryId: Number(form.categoryId),
  organizer: form.organizer.trim(),
  startDate: form.startDate,
  endDate: form.endDate,
  mode: form.mode,
  venue: form.venue.trim() || null,
  eventUrl: form.eventUrl.trim() || null,
});

export const applicationDetailPath = (application) => {
  if (!application?.applicationCode) {
    throw new Error('The backend did not return an application code.');
  }
  return `/student/applications/${encodeURIComponent(application.applicationCode)}`;
};

export const normalizeStudentProfile = (profile = {}) => ({
  name: profile.name || null,
  registrationNumber: profile.registrationNumber || null,
  email: profile.email || null,
  department: profile.departmentName || profile.departmentCode || null,
  departmentCode: profile.departmentCode || null,
  section: profile.sectionName || null,
  academicYear: profile.academicYear || null,
  studentYear: profile.studentYear || null,
  semester: profile.semester || null,
  batch: profile.batch || null,
});

export const statusLabel = (status) => ({
  SUBMITTED: 'Submitted — awaiting Mentor review',
  PENDING_MENTOR_APPROVAL: 'Awaiting Mentor review',
  PENDING_HOD_APPROVAL: 'Mentor recommended — awaiting HOD review',
  RETURNED_BY_MENTOR: 'Returned by Mentor for correction',
  RETURNED_BY_HOD: 'Returned by HOD for correction',
  APPROVED: 'Approved',
  REJECTED: 'Rejected',
  REJECTED_BY_MENTOR: 'Rejected by Mentor',
  REJECTED_BY_HOD: 'Rejected by HOD',
  CANCELLED: 'Cancelled',
}[status] || status || 'Unknown status');

export const postEventStatusLabel = (application = {}) => {
  if (application.postEventStatus === 'PENDING') return 'Post-Activity Report Pending';
  if (application.postEventStatus === 'SUBMITTED') return 'Evidence Incomplete';
  if (application.postEventStatus === 'COMPLETE') return 'Completed';
  if (application.status === 'APPROVED' && application.postEventStatus === 'NOT_DUE') return 'Approved — Event Upcoming';
  return null;
};

export const postEventActionLabel = (application = {}) => {
  if (application.postEventStatus === 'PENDING') return 'Submit Participation Details';
  if (application.postEventStatus === 'SUBMITTED') return 'Complete Evidence';
  return 'View Details';
};
