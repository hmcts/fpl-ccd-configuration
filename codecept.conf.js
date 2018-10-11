/*global process*/

exports.config = {
  output: "./output",
  multiple: {
    parallel: {
      chunks: parseInt(process.env.PARALLEL_CHUNKS || '3')
    }
  },
  helpers: {
    Puppeteer: {
      show: process.env.SHOW_BROWSER_WINDOW || false,
      restart: false,
      chrome: {
        ignoreHTTPSErrors: true
      }
    }
  },
  include: {
    config: "./e2e/config.js",
    I: "./e2e/steps_file.js",
    loginPage: "./e2e/pages/login/loginPage.js",
    createCasePage: "./e2e/pages/createCase/createCase.js",
    addEventSummaryPage: "./e2e/pages/createCase/addEventSummary.js",
    caseViewPage: "./e2e/pages/caseView/caseView.js",
    selectHearingPage: "./e2e/pages/selectHearing/selectHearing.js",
    enterGroundsPage: "./e2e/pages/enterGrounds/enterGrounds.js",
    enterFactorsAffectingParentingPage: "./e2e/pages/enterFactorsAffectingParenting/enterFactorsAffectingParenting.js",
    enterInternationalElementsPage: "./e2e/pages/internationalElements/enterInternationalElements.js",
    enterRiskAndHarmToChildPage: "./e2e/pages/enterRiskAndHarmToChild/enterRiskAndHarmToChild.js",
    uploadDocumentsPage: "./e2e/pages/uploadDocuments/uploadDocuments.js",
    enterApplicantPage: "./e2e/pages/enterApplicant/enterApplicant.js",
    enterChildrenPage: "./e2e/pages/enterChildren/enterChildren.js",
    enterOtherProceedingsPage: "./e2e/pages/enterOtherProceedings/enterOtherProceedings.js",
    attendingHearingPage: "./e2e/pages/attendingHearing/attendingHearing.js",
  },
  plugins: {
    screenshotOnFail: {
      enabled: true
    }
  },
  tests: "./e2e/paths/*_test.js",
  timeout: 10000,
};
