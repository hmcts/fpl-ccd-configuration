const defaultPassword = 'Password12';
const baseUrl = process.env.URL || 'http://localhost:3000';

module.exports = {
  swanseaLocalAuthorityUserOne: {
    email: 'kurt@swansea.gov.uk',
    password: process.env.LA_USER_PASSWORD || defaultPassword,
    forename: 'kurt@swansea.gov.uk',
    surname: '(local-authority)',
    login: async function(I) { await I.signIn(this); },
    check: async function(I) { await I.checkLoggedIn(); },
    fetch: (I) => I.grabCookie(),
    restore: (I, cookie) => { I.amOnPage(baseUrl); I.setCookie(cookie); },
  },
  swanseaLocalAuthorityUserTwo: {
    email: 'damian@swansea.gov.uk',
    password: process.env.LA_USER_PASSWORD || defaultPassword,
    forename: 'damian@swansea.gov.uk',
    surname: '(local-authority)',
    login: async function(I) { await I.signIn(this); },
    check: async function(I) { await I.checkLoggedIn(); },
    fetch: (I) => I.grabCookie(),
    restore: (I, cookie) => { I.amOnPage(baseUrl); I.setCookie(cookie); },
  },
  hillingdonLocalAuthorityUserOne: {
    email: 'sam@hillingdon.gov.uk',
    password: process.env.LA_USER_PASSWORD || defaultPassword,
    forename: 'sam@hillingdon.gov.uk',
    surname: '(local-authority)',
    login: async function(I) { await I.signIn(this); },
    check: async function(I) { await I.checkLoggedIn(); },
    fetch: (I) => I.grabCookie(),
    restore: (I, cookie) => { I.amOnPage(baseUrl); I.setCookie(cookie); },
  },
  hillingdonLocalAuthorityUserTwo: {
    email: 'siva@hillingdon.gov.uk',
    password: process.env.LA_USER_PASSWORD || defaultPassword,
    forename: 'siva@hillingdon.gov.uk',
    surname: '(local-authority)',
    login: async function(I) { await I.signIn(this); },
    check: async function(I) { await I.checkLoggedIn(); },
    fetch: (I) => I.grabCookie(),
    restore: (I, cookie) => { I.amOnPage(baseUrl); I.setCookie(cookie); },
  },
  wiltshireLocalAuthorityUserOne: {
    email: 'raghu@wiltshire.gov.uk',
    password: process.env.LA_USER_PASSWORD || defaultPassword,
    forename: 'raghu@wiltshire.gov.uk',
    surname: '(local-authority)',
    login: async function(I) { await I.signIn(this); },
    check: async function(I) { await I.checkLoggedIn(); },
    fetch: (I) => I.grabCookie(),
    restore: (I, cookie) => { I.amOnPage(baseUrl); I.setCookie(cookie); },
  },
  wiltshireLocalAuthorityUserTwo: {
    email: 'sam@wiltshire.gov.uk',
    password: process.env.LA_USER_PASSWORD || defaultPassword,
    forename: 'sam@wiltshire.gov.uk',
    surname: '(local-authority)',
    login: async function(I) { await I.signIn(this); },
    check: async function(I) { await I.checkLoggedIn(); },
    fetch: (I) => I.grabCookie(),
    restore: (I, cookie) => { I.amOnPage(baseUrl); I.setCookie(cookie); },
  },
  localAuthorityBarristerUserOne: {
    email: 'la-barrister@mailnesia.com',
    password: process.env.LA_BARRISTER_USER_PASSWORD || defaultPassword,
    forename: 'la-barrister@mailnesia.com',
    surname: '(local-authority-barrister)',
    login: async function(I) { await I.signIn(this); },
    check: async function(I) { await I.checkLoggedIn(); },
    fetch: (I) => I.grabCookie(),
    restore: (I, cookie) => { I.amOnPage(baseUrl); I.setCookie(cookie); },
  },
  hmctsAdminUser: {
    email: 'hmcts-admin@example.com',
    password: process.env.CA_USER_PASSWORD || defaultPassword,
    login: async function(I) { await I.signIn(this); },
    check: async function(I) { await I.checkLoggedIn(); },
    fetch: (I) => I.grabCookie(),
    restore: (I, cookie) => { I.amOnPage(baseUrl); I.setCookie(cookie); },
  },
  hmctsSuperUser: {
    email: 'hmcts-superuser@mailnesia.com',
    password: process.env.SUPER_USER_PASSWORD || defaultPassword,
    login: async function(I) { await I.signIn(this); },
    check: async function(I) { await I.checkLoggedIn(); },
    fetch: (I) => I.grabCookie(),
    restore: (I, cookie) => { I.amOnPage(baseUrl); I.setCookie(cookie); },
  },
  cafcassUser: {
    email: 'cafcass@example.com',
    password: process.env.CAFCASS_USER_PASSWORD || defaultPassword,
    login: async function(I) { await I.signIn(this); },
    check: async function(I) { await I.checkLoggedIn(); },
    fetch: (I) => I.grabCookie(),
    restore: (I, cookie) => { I.amOnPage(baseUrl); I.setCookie(cookie); },
  },
  gateKeeperUser: {
    email: 'gatekeeper-only@mailnesia.com',
    password: process.env.GATEKEEPER_USER_PASSWORD || defaultPassword,
    login: async function(I) { await I.signIn(this); },
    check: async function(I) { await I.checkLoggedIn(); },
    fetch: (I) => I.grabCookie(),
    restore: (I, cookie) => { I.amOnPage(baseUrl); I.setCookie(cookie); },
  },
  judicaryUser: {
    email: 'judiciary-only@mailnesia.com',
    password: process.env.JUDICIARY_USER_PASSWORD || defaultPassword,
    login: async function(I) { await I.signIn(this); },
    check: async function(I) { await I.checkLoggedIn(); },
    fetch: (I) => I.grabCookie(),
    restore: (I, cookie) => { I.amOnPage(baseUrl); I.setCookie(cookie); },
  },
  magistrateUser: {
    email: 'magistrate@mailnesia.com',
    password: process.env.MAGISTRATE_USER_PASSWORD || defaultPassword,
    login: async function(I) { await I.signIn(this); },
    check: async function(I) { await I.checkLoggedIn(); },
    fetch: (I) => I.grabCookie(),
    restore: (I, cookie) => { I.amOnPage(baseUrl); I.setCookie(cookie); },
  },
  systemUpdateUser: {
    email: process.env.SYSTEM_UPDATE_USER_USERNAME || 'fpl-system-update@mailnesia.com',
    password: process.env.SYSTEM_UPDATE_USER_PASSWORD || defaultPassword,
    login: async function(I) { await I.signIn(this); },
    check: async function(I) { await I.checkLoggedIn(); },
    fetch: (I) => I.grabCookie(),
    restore: (I, cookie) => { I.amOnPage(baseUrl); I.setCookie(cookie); },
  },
  smokeTestUser: {
    email: process.env.SMOKE_TEST_LA_USER_USERNAME || 'james@swansea.gov.uk',
    password: process.env.SMOKE_TEST_LA_USER_PASSWORD || defaultPassword,
    login: async function(I) { await I.signIn(this); },
    check: async function(I) { await I.checkLoggedIn(); },
    fetch: (I) => I.grabCookie(),
    restore: (I, cookie) => { I.amOnPage(baseUrl); I.setCookie(cookie); },
  },
  hmctsUser: {
    email: process.env.HMCTS_USER_USERNAME,
    password: process.env.HMCTS_USER_PASSWORD,
    login: async function(I) { await I.signIn(this); },
    check: async function(I) { await I.checkLoggedIn(); },
    fetch: (I) => I.grabCookie(),
    restore: (I, cookie) => { I.amOnPage(baseUrl); I.setCookie(cookie); },
  },
  privateSolicitorOne: {
    email: 'solicitor1@solicitors.uk',
    password: process.env.LA_USER_PASSWORD || defaultPassword,
    login: async function(I) { await I.signIn(this); },
    check: async function(I) { await I.checkLoggedIn(); },
    fetch: (I) => I.grabCookie(),
    restore: (I, cookie) => { I.amOnPage(baseUrl); I.setCookie(cookie); },
  },
  privateSolicitorTwo: {
    email: 'solicitor2@solicitors.uk',
    password: process.env.LA_USER_PASSWORD || defaultPassword,
    login: async function(I) { await I.signIn(this); },
    check: async function(I) { await I.checkLoggedIn(); },
    fetch: (I) => I.grabCookie(),
    restore: (I, cookie) => { I.amOnPage(baseUrl); I.setCookie(cookie); },
  },
  localAuthorityPassword: process.env.LA_USER_PASSWORD || defaultPassword,
  baseUrl: baseUrl,
  fplServiceUrl: process.env.CASE_SERVICE_URL || 'http://localhost:4013',
  idamApiUrl: process.env.IDAM_API_URL || 'https://idam-api.aat.platform.hmcts.net',
  swanseaOrgId: process.env.SWANSEA_ORG_ID || 'ORGSA',
  mockedPayment: (process.env.MOCKED_PAYMENTS || 'true').toLowerCase() === 'true',
  ctscEmail: process.env.CTSC_EMAIL || 'FamilyPublicLaw+ctsc@gmail.com',
  TestOutputDir: process.env.E2E_OUTPUT_DIR || './output',
  TestForAccessibility: process.env.TESTS_FOR_ACCESSIBILITY === 'true',
  definition: {
    jurisdiction: 'PUBLICLAW',
    jurisdictionFullDesc: 'Public Law',
    caseType: 'CARE_SUPERVISION_EPO',
    caseTypeFullDesc: 'Public Law Applications',
  },
  // actions
  applicationActions: {
    enterOrdersAndDirectionsNeeded: 'Orders and directions sought',
    enterHearingNeeded: 'Hearing urgency',
    enterChildren: 'Child\'s details',
    enterRespondents: 'Respondents\' details',
    enterApplicant: 'Applicant\'s details',
    enterLocalAuthority: 'Applicant\'s details',
    enterOthers: 'Other people in the case',
    enterGrounds: 'Grounds for the application',
    enterRiskAndHarmToChildren: 'Risk and harm to children',
    enterFactorsAffectingParenting: 'Factors affecting parenting',
    enterInternationalElement: 'International element',
    enterOtherProceedings: 'Other proceedings',
    enterAllocationProposal: 'Allocation proposal',
    enterAllocationDecision: 'Allocation decision',
    enterAttendingHearing: 'Court services needed',
    uploadDocuments: 'Upload documents',
    changeCaseName: 'Change case name',
    submitCase: 'Submit application',
    deleteApplication: 'Delete an application',
    uploadCMO: 'Upload draft orders',
    approveOrders: 'Approve orders',
    allocatedJudge: 'Allocated Judge',
    extend26WeekTimeline: 'Extend 26-week timeline',
    manageLegalRepresentatives: 'Add counsel/external solicitor',
    addApplicationDocuments: 'Application documents',
    manageDocumentsLA: 'Manage documents',
    messageJudge: 'Send and reply to messages',
    removeManagingOrganisation: 'Remove managing organisation',
    languageRequirement: 'Welsh language requirements',
    selectCourt: 'Select court to issue',
    addOrRemoveLegalCounsel: 'Add or remove legal counsel',
  },
  administrationActions: {
    addCaseFlag: 'Add case flag',
    addFamilyManCaseNumber: 'Add case number',
    changeCaseName: 'Change case name',
    sendToGatekeeper: 'Send to gatekeeper',
    notifyGatekeeper: 'Notify gatekeeper',
    amendChildren: 'Children',
    amendRespondents: 'Respondents',
    amendOther: 'Others to be given notice',
    amendInternationalElement: 'International element',
    amendOtherProceedings: 'Other proceedings',
    amendAttendingHearing: 'Attending the hearing',
    amendRepresentatives: 'Manage representatives',
    manageHearings: 'Manage hearings',
    createNoticeOfProceedings: 'Create notice of proceedings',
    addStatementOfService: 'Add statement of service (c9)',
    uploadC2Documents: 'Upload a C2',
    addGatekeepingOrder: 'Add the gatekeeping order',
    createOrder: 'Create or upload an order',
    placement: 'Placement',
    placementHearing: 'Notice of Placement - Hearing',
    handleSupplementaryEvidence: 'Handle supplementary evidence',
    bulkScan: 'Attach scanned docs',
    addNote: 'Add a case note',
    addExpertReportLog: 'Log expert report',
    recordFinalDecisions: 'Record final decisions',
    returnApplication: 'Return application',
    manageDocuments: 'Manage documents',
    uploadAdditionalApplications: 'Upload additional applications',
    manageOrders: 'Manage orders',
    uploadWelshTranslations: 'Upload translated documents',
    languageRequirement: 'Welsh language requirements',
    manageLocalAuthorities: 'Manage LAs / Transfer to court',
  },
  superUserActions: {
    removeOrdersAndApplications: 'Remove orders and applications',
    changeCaseState: 'Change case state',
  },
  internalActions: {
    updateCase: 'internal-change-UPDATE_CASE',
  },
  // files
  testFile: './e2e/fixtures/testFiles/mockFile.txt',
  testFile2: './e2e/fixtures/testFiles/mockFile2.txt',
  testFile3: './e2e/fixtures/testFiles/mockFile3.txt',
  testFile4: './e2e/fixtures/testFiles/mockFile4.txt',
  testFile5: './e2e/fixtures/testFiles/mockFile5.txt',
  testFile6: './e2e/fixtures/testFiles/mockFile6.txt',
  testFile7: './e2e/fixtures/testFiles/mockFile7.txt',
  testFile8: './e2e/fixtures/testFiles/mockFile8.txt',
  testFile9: './e2e/fixtures/testFiles/mockFile9.txt',
  testFile10: './e2e/fixtures/testFiles/mockFile10.txt',
  testFile11: './e2e/fixtures/testFiles/mockFile11.txt',
  testFile12: './e2e/fixtures/testFiles/mockFile12.txt',
  testFile13: './e2e/fixtures/testFiles/mockFile13.txt',
  testFile14: './e2e/fixtures/testFiles/mockFile14.txt',
  testFile15: './e2e/fixtures/testFiles/mockFile15.txt',
  testFile16: './e2e/fixtures/testFiles/mockFile16.txt',
  testPdfFile: './e2e/fixtures/testFiles/mockFile.pdf',
  testWordFile: './e2e/fixtures/testFiles/mockFile.docx',
  testWordFileSdo: './e2e/fixtures/testFiles/sdo.docx',
  testPdfFileUho: './e2e/fixtures/testFiles/uho.pdf',
  // urls
  presidentsGuidanceUrl: 'https://www.judiciary.uk/wp-content/uploads/2013/03/President%E2%80%99s-Guidance-on-Allocation-and-Gatekeeping.pdf',
  scheduleUrl: 'https://www.judiciary.uk/wp-content/uploads/2013/03/Schedule-to-the-President%E2%80%99s-Guidance-on-Allocation-and-Gatekeeping.pdf',
  otherProposalUrl: '/otherProposal/otherProposal1',
};
