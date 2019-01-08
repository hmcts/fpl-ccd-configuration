/*global process*/

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
  localAuthorityPassword: process.env.LA_USER_PASSWORD || 'Password12',
  hmctsAdminEmail: 'hmcts-admin@example.com',
  hmctsAdminPassword: process.env.CA_USER_PASSWORD || 'Password12',
  cafcassEmail: 'cafcass@example.com',
  cafcassPassword: process.env.CAFCASS_USER_PASSWORD || 'Password12',
  definition: {
    jurisdiction: 'PUBLICLAW',
    caseType: 'CARE_SUPERVISION_EPO',
  },
  // actions
  applicationActions: {
    selectOrders: 'Orders and directions needed',
    selectHearing: 'Hearing needed',
    enterChildren: 'Children',
    enterRespondents: 'Respondents',
    enterApplicants: 'Applicant',
    enterOthers: 'Others to be given notice',
    enterGrounds: 'Grounds for the application',
    enterRisk: 'Risk and harm to children',
    enterFactorsAffectingParenting: 'Factors affecting parenting',
    enterInternationalElement: 'International element',
    enterOtherProceedings: 'Other proceedings',
    enterAllocationProposal: 'Allocation proposal',
    attendingHearing: 'Attending the hearing',
    uploadDocuments: 'Documents',
    changeCaseName: 'Change case name',
    submitCase: 'Submit application',
  },
  addFamilyManCaseNumber: 'Add case number',
  standardDirections: 'Standard directions',
  sendToGatekeeper: 'Send to gatekeeper',
  // files
  testFile: './e2e/fixtures/mockFile.txt',
  // urls
  presidentsGuidanceUrl: 'https://www.judiciary.uk/wp-content/uploads/2013/03/President%E2%80%99s-Guidance-on-Allocation-and-Gatekeeping.pdf',
  scheduleUrl: 'https://www.judiciary.uk/wp-content/uploads/2013/03/Schedule-to-the-President%E2%80%99s-Guidance-on-Allocation-and-Gatekeeping.pdf',
  otherProposalUrl: '/otherProposal/otherProposal1',
  groundsForApplication: {
    harmIfNotMoved: 'There\'s reasonable cause to believe the child is likely to suffer significant harm if they\'re not moved to accommodation provided by you, or on your behalf',
    harmIfMoved: 'There\'s reasonable cause to believe the child is likely to suffer significant harm if they don\'t stay in their current accommodation',
    urgentAccessRequired: 'You\'re making enquiries and need urgent access to the child to find out about their welfare, and access is being unreasonably refused',
  },
};
