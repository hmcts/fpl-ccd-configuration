/* eslint-disable no-console */

const testConfig = require('./e2e/config');

const waitForTimeout = parseInt(process.env.WAIT_FOR_TIMEOUT) || 35000;
const smartWait = parseInt(process.env.SMART_WAIT) || 30000;
const browser = process.env.SAUCELABS_BROWSER || 'chrome';

const setupConfig = {
  tests: './e2e/tests/*_test.js',
  output: `${process.cwd()}/${testConfig.TestOutputDir}`,
  helpers: {
    WebDriver: {
      url: testConfig.baseUrl,
      restart: false,
      keepCookies: true,
      browser,
      smartWait,
      waitForTimeout,
      cssSelectorsEnabled: 'true',
      host: 'ondemand.eu-central-1.saucelabs.com',
      port: 80,
      region: 'eu',
      capabilities: {},
    },
    SauceLabsReportingHelper: {
      require: './e2e/helpers/SauceLabsReportingHelper.js',
    },
    HooksHelper: {
      require: './e2e/helpers/hooks_helper.js',
    },
    BrowserHelpers: {
      require: './e2e/helpers/browser_helper.js',
    },
    DumpBrowserLogsHelper: {
      require: './e2e/helpers/dump_browser_logs_helper.js',
    },
  },
  plugins: {
    retryFailedStep: {
      enabled: true,
      retries: 2,
    },
    autoDelay: {
      enabled: true,
      delayAfter: 2000,
    },
    screenshotOnFail: {
      enabled: true,
      fullPageScreenshots: true,
    },
  },
  include: {
    config: './e2e/config.js',
    I: './e2e/actors/main.js',
    loginPage: './e2e/pages/login.page.js',
    caseListPage: './e2e/pages/caseList.page.js',
    caseViewPage: './e2e/pages/caseView.page.js',
    paymentHistoryPage: './e2e/pages/paymentHistory.page.js',
    eventSummaryPage: './e2e/pages/eventSummary.page.js',
    openApplicationEventPage: './e2e/pages/events/openApplicationEvent.page.js',
    deleteApplicationEventPage: './e2e/pages/events/deleteApplicationEvent.page.js',
    submitApplicationEventPage: './e2e/pages/events/submitApplicationEvent.page.js',
    changeCaseNameEventPage: './e2e/pages/events/changeCaseNameEvent.page.js',
    enterOrdersAndDirectionsNeededEventPage: './e2e/pages/events/enterOrdersAndDirectionsNeededEvent.page.js',
    enterHearingNeededEventPage: './e2e/pages/events/enterHearingNeededEvent.page.js',
    enterChildrenEventPage: './e2e/pages/events/enterChildrenEvent.page.js',
    enterRespondentsEventPage: './e2e/pages/events/enterRespondentsEvent.page.js',
    enterRepresentativesEventPage: './e2e/pages/events/enterRepresentativesEvent.page.js',
    enterApplicantEventPage: './e2e/pages/events/enterApplicantEvent.page.js',
    enterOthersEventPage: './e2e/pages/events/enterOthersEvent.page.js',
    enterGroundsForApplicationEventPage: './e2e/pages/events/enterGroundsForApplicationEvent.page.js',
    enterRiskAndHarmToChildrenEventPage: './e2e/pages/events/enterRiskAndHarmToChildrenEvent.page.js',
    enterFactorsAffectingParentingEventPage: './e2e/pages/events/enterFactorsAffectingParentingEvent.page.js',
    enterInternationalElementEventPage: './e2e/pages/events/enterInternationalElementEvent.page.js',
    enterOtherProceedingsEventPage: './e2e/pages/events/enterOtherProceedingsEvent.page.js',
    enterAllocationProposalEventPage: './e2e/pages/events/enterAllocationProposalEvent.page.js',
    enterAllocationDecisionEventPage: './e2e/pages/events/enterAllocationDecisionEvent.page.js',
    enterAttendingHearingEventPage: './e2e/pages/events/enterAttendingHearingEvent.page.js',
    uploadDocumentsEventPage: './e2e/pages/events/uploadDocumentsEvent.page.js',
    enterFamilyManCaseNumberEventPage: './e2e/pages/events/enterFamilyManCaseNumberEvent.page.js',
    sendCaseToGatekeeperEventPage: './e2e/pages/events/sendCaseToGatekeeperEvent.page.js',
    notifyGatekeeperEventPage: './e2e/pages/events/sendCaseToGatekeeperEvent.page.js',
    createNoticeOfProceedingsEventPage: './e2e/pages/events/createNoticeOfProceedingsEvent.page.js',
    manageHearingsEventPage: './e2e/pages/events/manageHearingsEvent.page.js',
    addStatementOfServiceEventPage: './e2e/pages/events/addStatementOfServiceEvent.page.js',
    uploadC2DocumentsEventPage: './e2e/pages/events/uploadC2DocumentsEvent.page.js',
    draftStandardDirectionsEventPage: './e2e/pages/events/draftStandardDirectionsEvent.page.js',
    createOrderEventPage: './e2e/pages/events/createOrderEvent.page.js',
    placementEventPage: './e2e/pages/events/placementEvent.page.js',
    allocatedJudgeEventPage: './e2e/pages/events/enterAllocatedJudgeEvent.page.js',
    handleSupplementaryEvidenceEventPage: './e2e/pages/events/handleSupplementaryEvidenceEvent.page.js',
    attachScannedDocsEventPage: './e2e/pages/events/attachScannedDocsEvent.page.js',
    addNoteEventPage: './e2e/pages/events/addNoteEvent.page.js',
    addExpertReportEventPage: './e2e/pages/events/addExpertReportEvent.page.js',
    addExtend26WeekTimelineEventPage: './e2e/pages/events/addExtend26WeekTimelineEvent.page.js',
    closeTheCaseEventPage: './e2e/pages/events/closeTheCaseEvent.page.js',
    returnApplicationEventPage: './e2e/pages/events/returnApplicationEvent.page.js',
    uploadCaseManagementOrderEventPage: './e2e/pages/events/uploadCaseManagementOrderEvent.page.js',
    reviewAgreedCaseManagementOrderEventPage: './e2e/pages/events/reviewAgreedCaseManagementOrderEvent.page.js',
    removeOrderEventPage: './e2e/pages/events/removeOrderEvent.page.js',
    manageDocumentsEventPage: './e2e/pages/events/manageDocumentsEvent.page.js',
    manageDocumentsLAEventPage: './e2e/pages/events/manageDocumentsLAEvent.page.js',
    changeCaseStateEventPage: './e2e/pages/events/changeCaseStateEvent.page.js',
    manageLegalRepresentativesEventPage: './e2e/pages/events/manageLegalRepresentativesEvent.page.js',
    addApplicationDocumentsEventPage: './e2e/pages/events/addApplicationDocumentsEvent.page.js',
    messageJudgeOrLegalAdviserEventPage: './e2e/pages/events/messageJudgeOrLegalAdviserEvent.page.js',
    manageOrdersEventPage: './e2e/pages/events/manageOrders.page.js',
  },
  mocha: {
    reporterOptions: {
      'codeceptjs-cli-reporter': {
        stdout: '-',
        options: {steps: true},
      },
      'mocha-junit-reporter': {
        stdout: '-',
        options: {mochaFile: `${testConfig.TestOutputDir}/result.xml`},
      },
      mochawesome: {
        stdout: testConfig.TestOutputDir + '/console.log',
        options: {
          reportDir: testConfig.TestOutputDir,
          reportName: 'index',
          reportTitle: 'Crossbrowser results',
          inlineAssets: true,
        },
      },
    },
  },
  multiple: require('./e2e/crossbrowser/supportedBrowsers.js'),
};

exports.config = setupConfig;
