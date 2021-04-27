require('./e2e/helpers/event_listener');
const lodash = require('lodash');

exports.config = {
  output: './output',
  multiple: {
    parallel: {
      chunks: (files) => {

        const splitFiles = (list, size) => {
          const sets = [];
          const chunks = list.length / size;
          let i = 0;

          while (i < chunks) {
            sets[i] = list.splice(0, size);
            i++;
          }
          return sets;
        };

        const buckets = parseInt(process.env.PARALLEL_CHUNKS || '5');
        const slowTests = lodash.filter(files, file => file.includes('@slow'));
        const otherTests = lodash.difference(files, slowTests);

        let chunks = [];
        if (buckets > slowTests.length + 1) {
          const slowTestChunkSize = 1;
          const regularChunkSize = Math.ceil((files.length - slowTests.length) / (buckets - slowTests.length));
          chunks = lodash.union(splitFiles(slowTests, slowTestChunkSize), splitFiles(otherTests, regularChunkSize));
        } else {
          chunks = splitFiles(files, Math.ceil(files.length / buckets));
        }

        console.log(chunks);

        return chunks;
      },
    },
  },
  helpers: {
    Puppeteer: {
      show: process.env.SHOW_BROWSER_WINDOW || false,
      restart: false,
      keepCookies: true,
      waitForTimeout: parseInt(process.env.WAIT_FOR_TIMEOUT || '20000'),
      chrome: {
        ignoreHTTPSErrors: true,
        args: process.env.PROXY_SERVER ? [
          `--proxy-server=${process.env.PROXY_SERVER}`,
        ] : [],
        devtools: process.env.SHOW_BROWSER_WINDOW || false,
      },
      windowSize: '1280x960',
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
    GenerateReportHelper: {
      require: './e2e/helpers/generate_report_helper.js',
    },
    TabAssertions: {
      require: './e2e/helpers/tab_assertions_helper.js',
    },
  },
  include: {
    config: './e2e/config.js',
    I: './e2e/steps_file.js',
    loginPage: './e2e/pages/login.page.js',
    caseListPage: './e2e/pages/caseList.page.js',
    caseViewPage: './e2e/pages/caseView.page.js',
    paymentHistoryPage: './e2e/pages/paymentHistory.page.js',
    eventSummaryPage: './e2e/pages/eventSummary.page.js',
    noticeOfChangePage: './e2e/pages/noticeOfChange.page.js',
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
    uploadAdditionalApplicationsEventPage: './e2e/pages/events/uploadAdditionalApplicationsEvent.page.js',
    manageOrdersEventPage: './e2e/pages/events/manageOrders.page.js',
  },
  plugins: {
    autoDelay: {
      enabled: true,
      methods: [
        'click',
        'doubleClick',
        'rightClick',
        'fillField',
        'checkOption',
        'selectOption',
      ],
      delayAfter: 550,
    },
    retryFailedStep: {
      enabled: true,
    },
    screenshotOnFail: {
      enabled: true,
      fullPageScreenshots: true,
    },
  },
  tests: './e2e/tests/*_test.js',
  teardownAll: require('./e2e/hooks/aggregate-metrics'),
  mocha: {
    reporterOptions: {
      'codeceptjs-cli-reporter': {
        stdout: '-',
        options: {
          steps: false,
        },
      },
      'mocha-junit-reporter': {
        stdout: '-',
        options: {
          mochaFile: 'test-results/result.xml',
        },
      },
      'mochawesome': {
        stdout: '-',
        options: {
          reportDir: './output',
          inlineAssets: true,
          json: false,
        },
      },
      '../../e2e/reporters/json-file-reporter/reporter': {
        stdout: '-',
      },
    },
  },
};
