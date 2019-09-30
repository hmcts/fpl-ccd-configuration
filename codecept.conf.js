/* global process */

exports.config = {
  output: './output',
  multiple: {
    parallel: {
      chunks: parseInt(process.env.PARALLEL_CHUNKS || '5'),
    },
  },
  helpers: {
    Puppeteer: {
      show: process.env.SHOW_BROWSER_WINDOW || false,
      restart: false,
      keepCookies: true,
      waitForTimeout: 10000,
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
    PuppeteerHelpers: {
      require: './e2e/helpers/puppeter_helper.js',
    },
    DumpBrowserLogsHelper: {
      require: './e2e/helpers/dump_browser_logs_helper.js',
    },
  },
  include: {
    config: './e2e/config.js',
    I: './e2e/steps_file.js',
    loginPage: './e2e/pages/login.page.js',
    caseListPage: './e2e/pages/caseList.page.js',
    caseViewPage: './e2e/pages/caseView.page.js',
    eventSummaryPage: './e2e/pages/eventSummary.page.js',
    openApplicationEventPage: './e2e/pages/events/openApplicationEvent.page.js',
    deleteApplicationEventPage: './e2e/pages/events/deleteApplicationEvent.page.js',
    submitApplicationEventPage: './e2e/pages/events/submitApplicationEvent.page.js',
    changeCaseNameEventPage: './e2e/pages/events/changeCaseNameEvent.page.js',
    enterOrdersAndDirectionsNeededEventPage: './e2e/pages/events/enterOrdersAndDirectionsNeededEvent.page.js',
    enterHearingNeededEventPage: './e2e/pages/events/enterHearingNeededEvent.page.js',
    enterChildrenEventPage: './e2e/pages/events/enterChildrenEvent.page.js',
    enterRespondentsEventPage: './e2e/pages/events/enterRespondentsEvent.page.js',
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
    uploadStandardDirectionsDocumentEventPage: './e2e/pages/events/uploadStandardDirectionsDocumentEvent.page.js',
    sendCaseToGatekeeperEventPage: './e2e/pages/events/sendCaseToGatekeeperEvent.page.js',
    addHearingBookingDetailsEventPage: './e2e/pages/events/addHearingBookingDetailsEvent.page.js',
    addStatementOfServiceEventPage: './e2e/pages/events/addStatementOfServiceEvent.page.js',
  },
  plugins: {
    autoDelay: {
      enabled: true,
      methods: [
        'click',
        'doubleClick',
        'rightClick',
        'fillField',
        'pressKey',
        'checkOption',
        'selectOption',
      ],
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
    },
  },
};
