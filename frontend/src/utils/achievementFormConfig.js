export const isAchievementType = (type) => type?.workflowCategory === 'ACHIEVEMENT';

const fields = {
  PATENT: [
    ['patentTitle', 'Patent title', true], ['applicationReference', 'Application / patent number', false],
    ['patentStatus', 'Patent status', true], ['filingDate', 'Filing date', false],
    ['grantDate', 'Grant date', false], ['inventors', 'Inventors', true],
  ],
  PUBLICATION: [
    ['paperTitle', 'Paper title', true], ['conferenceName', 'Conference / journal name', true],
    ['publicationScope', 'National / international', true], ['publicationDate', 'Publication date', true],
    ['doi', 'DOI', false], ['isbnIssn', 'ISBN / ISSN', false], ['authors', 'Authors', true],
  ],
  COURSE: [
    ['courseName', 'Course name', true], ['provider', 'Issuing organization / provider', true],
    ['durationHours', 'Duration (hours)', true], ['completionDate', 'Completion date', false],
    ['credentialId', 'Credential / certificate ID', false], ['examDate', 'Exam date', false],
    ['score', 'Score', false], ['grade', 'Elite / Silver / Gold / grade', false],
  ],
  STARTUP: [
    ['ventureName', 'Startup name', true], ['studentRole', 'Student role', true],
    ['stage', 'Stage', true], ['recognitionNumber', 'Registration / recognition number', false],
    ['startDate', 'Start date', false], ['website', 'Website', false],
  ],
  INTERNSHIP: [
    ['organizationName', 'Organization', true], ['internshipRole', 'Role', true],
    ['location', 'Location', false], ['stipend', 'Stipend (Yes / No)', true],
    ['stipendAmount', 'Stipend amount', false], ['completionCertificate', 'Completion certificate details', false],
  ],
  DUAL_DEGREE: [
    ['university', 'University', true], ['program', 'Program', true], ['country', 'Country', false],
    ['startDate', 'Start date', false], ['expectedCompletion', 'Expected / actual completion', false],
  ],
};

export const achievementFields = (type) => fields[type?.config?.kind] || [];
export const achievementDocuments = (type) => {
  const configured = Array.isArray(type?.config?.documents) ? type.config.documents : [];
  const codes = configured.length > 0 ? configured : ['ADDITIONAL_SUPPORTING_DOCUMENT'];
  return codes.map((code) => ({
    code,
    title: code === 'ADDITIONAL_SUPPORTING_DOCUMENT' ? 'Supporting evidence' : code.replaceAll('_', ' ').toLowerCase().replace(/(^|\s)\S/g, (letter) => letter.toUpperCase()),
    multiple: code === 'ADDITIONAL_SUPPORTING_DOCUMENT',
    required: false,
  }));
};
