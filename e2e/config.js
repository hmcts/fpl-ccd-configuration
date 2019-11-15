/*global process*/

const defaultPassword = 'Password12';

module.exports = {
  // users
  swanseaLocalAuthorityUserOne: {
    forename: 'kurt@swansea.gov.uk',
    surname: '(local-authority)',
  },
  swanseaLocalAuthorityUserTwo: {
    forename: 'damian@swansea.gov.uk',
    surname: '(local-authority)',
  },
  hillingdonLocalAuthorityUserOne: {
    forename: 'sam@hillingdon.gov.uk',
    surname: '(local-authority)',
  },
  swanseaLocalAuthorityEmailUserOne: 'kurt@swansea.gov.uk',
  swanseaLocalAuthorityEmailUserTwo: 'damian@swansea.gov.uk',
  hillingdonLocalAuthorityEmailUserOne: 'sam@hillingdon.gov.uk',
  localAuthorityPassword: process.env.LA_USER_PASSWORD || defaultPassword,
  hmctsAdminEmail: 'hmcts-admin@example.com',
  hmctsAdminPassword: process.env.CA_USER_PASSWORD || defaultPassword,
  cafcassEmail: 'cafcass@example.com',
  cafcassPassword: process.env.CAFCASS_USER_PASSWORD || defaultPassword,
  gateKeeperEmail: 'gatekeeper@mailnesia.com',
  gateKeeperPassword: process.env.GATEKEEPER_USER_PASSWORD || defaultPassword,
  judiciaryEmail: 'judiciary@mailnesia.com',
  judiciaryPassword: process.env.JUDICIARY_USER_PASSWORD || defaultPassword,
  smokeTestLocalAuthorityEmail: process.env.SMOKE_TEST_LA_USER_USERNAME || 'james@swansea.gov.uk',
  smokeTestLocalAuthorityPassword: process.env.SMOKE_TEST_LA_USER_PASSWORD || defaultPassword,
  definition: {
    jurisdiction: 'PUBLICLAW',
    caseType: 'CARE_SUPERVISION_EPO',
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
    draftCaseManagementOrder: 'Draft CMO',
    complyWithDirections: 'Comply with directions',
  },
  administrationActions: {
    addFamilyManCaseNumber: 'Add case number',
    sendToGatekeeper: 'Send to gatekeeper',
    amendChildren: 'Children',
    amendRespondents: 'Respondents',
    amendOther: 'Others to be given notice',
    amendInternationalElement: 'International element',
    amendOtherProceedings: 'Other proceedings',
    amendAttendingHearing: 'Attending the hearing',
    addHearingBookingDetails: 'Add hearing details',
    createNoticeOfProceedings: 'Create notice of proceedings',
    addStatementOfService: 'Add statement of service (c9)',
    uploadC2Documents: 'Upload a C2',
    draftStandardDirections: 'Draft standard directions',
  },
  // files
  testFile: './e2e/fixtures/mockFile.txt',
  // urls
  presidentsGuidanceUrl: 'https://www.judiciary.uk/wp-content/uploads/2013/03/President%E2%80%99s-Guidance-on-Allocation-and-Gatekeeping.pdf',
  scheduleUrl: 'https://www.judiciary.uk/wp-content/uploads/2013/03/Schedule-to-the-President%E2%80%99s-Guidance-on-Allocation-and-Gatekeeping.pdf',
  otherProposalUrl: '/otherProposal/otherProposal1',
};
