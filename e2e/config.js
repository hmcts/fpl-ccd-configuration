/*global process*/

const defaultPassword = 'Password12';

module.exports = {
  swanseaLocalAuthorityUserOne: {
    email: 'kurt@swansea.gov.uk',
    password: process.env.LA_USER_PASSWORD || defaultPassword,
    forename: 'kurt@swansea.gov.uk',
    surname: '(local-authority)',
  },
  swanseaLocalAuthorityUserTwo: {
    email: 'damian@swansea.gov.uk',
    password: process.env.LA_USER_PASSWORD || defaultPassword,
    forename: 'damian@swansea.gov.uk',
    surname: '(local-authority)',
  },
  hillingdonLocalAuthorityUserOne: {
    email: 'sam@hillingdon.gov.uk',
    password: process.env.LA_USER_PASSWORD || defaultPassword,
    forename: 'sam@hillingdon.gov.uk',
    surname: '(local-authority)',
  },
  hmctsAdminUser: {
    email: 'hmcts-admin@example.com',
    password: process.env.CA_USER_PASSWORD || defaultPassword,
  },
  cafcassUser: {
    email: 'cafcass@example.com',
    password: process.env.CAFCASS_USER_PASSWORD || defaultPassword,
  },
  gateKeeperUser: {
    email: 'gatekeeper-only@mailnesia.com',
    password: process.env.GATEKEEPER_USER_PASSWORD || defaultPassword,
  },
  judicaryUser: {
    email: 'judiciary-only@mailnesia.com',
    password: process.env.JUDICIARY_USER_PASSWORD || defaultPassword,
  },
  systemUpdateUser: {
    email: process.env.SYSTEM_UPDATE_USER_USERNAME || 'fpl-system-update@mailnesia.com',
    password: process.env.SYSTEM_UPDATE_USER_PASSWORD || defaultPassword,
  },
  smokeTestUser: {
    email: process.env.SMOKE_TEST_LA_USER_USERNAME || 'james@swansea.gov.uk',
    password: process.env.SMOKE_TEST_LA_USER_PASSWORD || defaultPassword,
  },
  localAuthorityPassword: process.env.LA_USER_PASSWORD || defaultPassword,
  fplServiceUrl: process.env.CASE_SERVICE_URL || 'http://localhost:4000',
  idamApiUrl: process.env.IDAM_API_URL || 'http://localhost:5000',
  dmStoreUrl: process.env.DM_STORE_URL || 'http://dm-store:8080',
  definition: {
    jurisdiction: 'PUBLICLAW',
    jurisdictionFullDesc: 'Public Law',
    caseType: 'CARE_SUPERVISION_EPO',
    caseTypeFullDesc: 'Care, supervision and EPOs',
  },
  // actions
  applicationActions: {
    enterOrdersAndDirectionsNeeded: 'Orders and directions needed',
    enterHearingNeeded: 'Hearing needed',
    enterChildren: 'Children',
    enterRespondents: 'Respondents',
    enterApplicant: 'Applicant',
    enterOthers: 'Others to be given notice',
    enterGrounds: 'Grounds for the application',
    enterRiskAndHarmToChildren: 'Risk and harm to children',
    enterFactorsAffectingParenting: 'Factors affecting parenting',
    enterInternationalElement: 'International element',
    enterOtherProceedings: 'Other proceedings',
    enterAllocationProposal: 'Allocation proposal',
    enterAllocationDecision: 'Allocation decision',
    enterAttendingHearing: 'Attending the hearing',
    uploadDocuments: 'Documents',
    changeCaseName: 'Change case name',
    submitCase: 'Submit application',
    deleteApplication: 'Delete an application',
    sendAgreedCmoToJudge: 'Send agreed CMO to judge',
    actionCaseManagementOrder: 'Action CMO',
    complyWithDirections: 'Comply with directions',
    complyOnBehalfOf: 'Comply on behalf of others',
    allocatedJudge: 'Allocated Judge',
    extend26WeekTimeline: 'Extend 26-week timeline',
  },
  administrationActions: {
    addFamilyManCaseNumber: 'Add case number',
    sendToGatekeeper: 'Send to gatekeeper',
    notifyGatekeeper: 'Notify gatekeeper',
    amendChildren: 'Children',
    amendRespondents: 'Respondents',
    amendOther: 'Others to be given notice',
    amendInternationalElement: 'International element',
    amendOtherProceedings: 'Other proceedings',
    amendAttendingHearing: 'Attending the hearing',
    amendRepresentatives: 'Manage representatives',
    addHearingBookingDetails: 'Add hearing details',
    createNoticeOfProceedings: 'Create notice of proceedings',
    addStatementOfService: 'Add statement of service (c9)',
    uploadC2Documents: 'Upload a C2',
    draftStandardDirections: 'Draft standard directions',
    createOrder: 'Create an order',
    placement: 'Placement',
    handleSupplementaryEvidence: 'Handle supplementary evidence',
    bulkScan: 'Attach scanned docs',
    addNote: 'Add a case note',
    addExpertReportLog: 'Log expert report',
    closeTheCase: 'Close the case',
    returnApplication: 'Return application',
  },
  // files
  testFile: './e2e/fixtures/mockFile.txt',
  testNonEmptyPdfFile: './e2e/fixtures/mockFile.pdf',
  // urls
  presidentsGuidanceUrl: 'https://www.judiciary.uk/wp-content/uploads/2013/03/President%E2%80%99s-Guidance-on-Allocation-and-Gatekeeping.pdf',
  scheduleUrl: 'https://www.judiciary.uk/wp-content/uploads/2013/03/Schedule-to-the-President%E2%80%99s-Guidance-on-Allocation-and-Gatekeeping.pdf',
  otherProposalUrl: '/otherProposal/otherProposal1',
};
