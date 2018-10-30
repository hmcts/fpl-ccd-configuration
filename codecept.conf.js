/* global process */


let proxyServer = undefined;

if (process.env.http_proxy || process.env.https_proxy) {
  const urlUtils = require('url');

  if (process.env.http_proxy) {
    proxyServer = `${urlUtils.parse(process.env.http_proxy).host}`;
  }
  if (process.env.https_proxy) {
    proxyServer = `${urlUtils.parse(process.env.https_proxy).host}`;
  }
}

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
      waitForNavigation: 'networkidle0',
      waitForTimeout: 30000,
      chrome: {
        ignoreHTTPSErrors: true,
        args: proxyServer ? [
          `--proxy-server=${proxyServer}`,
        ] : [],
      },
      windowSize: '1280x960',
    },
    MyHelpers: {
      require: './e2e/helpers/browserback_helper.js',
    },
  },
  include: {
    config: './e2e/config.js',
    I: './e2e/steps_file.js',
    loginPage: './e2e/pages/login/loginPage.js',
    caseListPage: './e2e/pages/caseList/caseList.js',
    createCasePage: './e2e/pages/createCase/createCase.js',
    addEventSummaryPage: './e2e/pages/createCase/addEventSummary.js',
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
  },
  plugins: {
    autoDelay: {
      enabled: true,
    },
    retryFailedStep: {
      enabled: true,
    },
    screenshotOnFail: {
      enabled: true,
    },
  },
  tests: './e2e/paths/*_test.js',
  timeout: 30000,
  mocha: {
    reporterOptions: {
      mochaFile: 'test-results/result.xml',
    },
  },
};
