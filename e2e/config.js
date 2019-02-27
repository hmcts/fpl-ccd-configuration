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
  swanseaLocalAuthorityEmailUserOne: process.env.LA_USER_USERNAME || 'kurt@swansea.gov.uk',
  swanseaLocalAuthorityEmailUserTwo: 'damian@swansea.gov.uk',
  hillingdonLocalAuthorityEmailUserOne: 'sam@hillingdon.gov.uk',
  localAuthorityPassword: process.env.LA_USER_PASSWORD || 'Password12',
  hmctsAdminEmail: process.env.CA_USER_USERNAME || 'hmcts-admin@example.com',
  hmctsAdminPassword: process.env.CA_USER_PASSWORD || 'Password12',
  cafcassEmail: process.env.CAFCASS_USER_USERNAME || 'cafcass@example.com',
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
  sendToGatekeeper: 'Send to gatekeeper',
  // files
  testFile: './e2e/fixtures/mockFile.txt',
  // urls
  presidentsGuidanceUrl: 'https://www.judiciary.uk/wp-content/uploads/2013/03/President%E2%80%99s-Guidance-on-Allocation-and-Gatekeeping.pdf',
  scheduleUrl: 'https://www.judiciary.uk/wp-content/uploads/2013/03/Schedule-to-the-President%E2%80%99s-Guidance-on-Allocation-and-Gatekeeping.pdf',
  otherProposalUrl: '/otherProposal/otherProposal1',
};
