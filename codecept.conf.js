/* global process */

exports.config = {
  output: './output',
  multiple: {
    parallel: {
      chunks: parseInt(process.env.PARALLEL_CHUNKS || '3'),
    },
  },
  helpers: {
    Puppeteer: {
      show: process.env.SHOW_BROWSER_WINDOW || false,
      restart: true,
      waitForTimeout: 5000,
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
  },
  include: {
    config: './e2e/config.js',
    I: './e2e/steps_file.js',
    loginPage: './e2e/pages/login/loginPage.js',
    caseListPage: './e2e/pages/caseList/caseList.js',
    createCasePage: './e2e/pages/createCase/createCase.js',
    eventSummaryPage: './e2e/pages/eventSummary/eventSummary.js',
    caseViewPage: './e2e/pages/caseView/caseView.js',
    selectHearingPage: './e2e/pages/selectHearing/selectHearing.js',
    enterGroundsPage: './e2e/pages/enterGrounds/enterGrounds.js',
    enterFactorsAffectingParentingPage: './e2e/pages/enterFactorsAffectingParenting/enterFactorsAffectingParenting.js',
    enterInternationalElementsPage: './e2e/pages/internationalElements/enterInternationalElements.js',
    enterRiskAndHarmToChildPage: './e2e/pages/enterRiskAndHarmToChild/enterRiskAndHarmToChild.js',
    uploadDocumentsPage: './e2e/pages/uploadDocuments/uploadDocuments.js',
    enterApplicantPage: './e2e/pages/enterApplicant/enterApplicant.js',
    enterChildrenPage: './e2e/pages/enterChildren/enterChildren.js',
    enterOtherProceedingsPage: './e2e/pages/enterOtherProceedings/enterOtherProceedings.js',
    attendingHearingPage: './e2e/pages/attendingHearing/attendingHearing.js',
    enterAllocationProposalPage: './e2e/pages/enterAllocationProposal/enterAllocationProposal.js',
    enterRespondentsPage: './e2e/pages/enterRespondents/enterRespondents.js',
    enterOthersPage: './e2e/pages/enterOthers/enterOthers.js',
    ordersNeededPage: './e2e/pages/ordersNeeded/ordersNeeded.js',
    enterFamilyManPage: './e2e/pages/enterFamilyMan/enterFamilyMan.js',
    changeCaseNamePage: './e2e/pages/changeCaseName/changeCaseName.js',
    submitApplicationPage: './e2e/pages/submitApplication/submitApplication.js',
    sendToGatekeeperPage: './e2e/pages/sendToGatekeeper/sendToGatekeeper.js',
    standardDirectionsPage: './e2e/pages/uploadDocuments/standardDirections.js',
    deleteApplicationPage: './e2e/pages/deleteApplication/deleteApplication.js',
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
  tests: './e2e/paths/*_test.js',
};
