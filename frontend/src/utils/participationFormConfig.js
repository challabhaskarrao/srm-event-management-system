export const TEAM_LIMITS = { min: 2, max: 10 };

export const supportsTeam = (type) => Boolean(type?.config?.team);
export const supportsTravel = (type, mode) => Boolean(type?.config?.travel) && mode !== 'ONLINE';
export const isInternship = (type) => type?.config?.kind === 'INTERNSHIP';

export const typeSpecificFields = (type) => {
  switch (type?.config?.kind) {
    case 'PUBLICATION': return [['conferenceName', 'Conference / publication name', true], ['paperTitle', 'Paper title', true], ['publicationScope', 'National / International', true], ['publicationStatus', 'Presentation / publication status', false]];
    case 'COURSE': return [['provider', 'Provider / platform', true], ['durationHours', 'Duration (hours)', true], ['enrollmentReference', 'Enrollment / registration reference', false]];
    case 'INTERNSHIP': return [['organizationName', 'Organization name', true], ['internshipRole', 'Internship title / role', true], ['location', 'Location', type.config.mode === 'OFFLINE'], ['stipendAmount', 'Monthly stipend (INR)', false]];
    case 'PATENT': return [['patentTitle', 'Patent / innovation title', true], ['applicationReference', 'Application / reference number', false], ['patentStatus', 'Application status', false]];
    case 'DUAL_DEGREE': return [['university', 'University / institution', true], ['program', 'Program', true], ['expectedEndDate', 'Expected end date', false]];
    case 'STARTUP': return [['ventureName', 'Startup / venture name', true], ['recognition', 'Recognition / incubation details', false]];
    default: return [];
  }
};

export const documentDefinitions = (type, outstation) => {
  const names = type?.config?.documents || [];
  const labels = {
    EVENT_POSTER: ['Event poster / brochure', 'Poster, brochure, or official event notice'], REGISTRATION_PROOF: ['Registration / confirmation proof', 'Registration or selection confirmation'], INVITATION: ['Official invitation / event notice', 'Optional invitation or official notice'], FEE_RECEIPT: ['Registration fee receipt', 'Evidence only; payment is not processed here'], OFFER_LETTER: ['Offer / acceptance letter', 'Offer, admission, or internship confirmation'], ENROLLMENT_CONFIRMATION: ['Course / enrollment confirmation', 'Enrollment or certification confirmation'], PATENT_SUPPORTING_DOCUMENT: ['Patent supporting document', 'Patent application or supporting evidence'], TRAVEL_TICKET: ['Travel ticket / booking proof', 'Optional travel evidence for the demo workflow'], PARENT_CONSENT: ['Parent consent / permission', 'Optional permission document for travel'], ACCOMMODATION_CONFIRMATION: ['Accommodation confirmation', 'Booking or accommodation evidence'], ADDITIONAL_SUPPORTING_DOCUMENT: ['Additional supporting document', 'Any other relevant supporting material'],
  };
  const selected = names.map((code) => ({ code, title: labels[code]?.[0] || code, help: labels[code]?.[1] || 'Relevant supporting material', required: ['EVENT_POSTER', 'REGISTRATION_PROOF'].includes(code) }));
  if (outstation) ['TRAVEL_TICKET', 'PARENT_CONSENT', 'ACCOMMODATION_CONFIRMATION'].forEach((code) => { if (!selected.some((item) => item.code === code)) selected.push({ code, title: labels[code][0], help: labels[code][1], required: false }); });
  return selected;
};
