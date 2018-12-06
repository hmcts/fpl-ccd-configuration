// /* eslint-disable no-console */
const supportedBrowsers = require('./e2e/crossbrowser/supportedBrowsers.js');
const E2E_FRONTEND_URL = 'https://www.ccd.demo.platform.hmcts.net';

const saucelabs_tunnelId= 'reform_tunnel';
const saucelabs_username = 'username';
const saucelabs_accesskey = 'privatekey';
const tunnelName = process.env.SAUCE_TUNNEL_IDENTIFIER || saucelabs_tunnelId;
const saucelabs_browserName = 'chrome';


const { config } = require('./codecept.conf');

console.log('before', config);

delete config.helpers.Puppeteer;
config.helpers.WebDriverIO = {
  url: process.env.URL,
  browser: process.env.SAUCE_BROWSER || saucelabs_browserName,
  cssSelectorsEnabled: 'true',
  host: 'ondemand.saucelabs.com',
  port: 80,
  user: process.env.SAUCE_USERNAME || saucelabs_username,
  key: process.env.SAUCE_ACCESS_KEY || saucelabs_accesskey,
  desiredCapabilities: {},
};
config.helpers.SauceLabsReportingHelper = {
  require: './e2e/helpers/SauceLabsReportingHelper.js',
};


const getBrowserConfig = (browserGroup) => {
  const browserConfig = [];
  for (const candidateBrowser in supportedBrowsers[browserGroup]) {
    if (candidateBrowser) {
      const desiredCapability = supportedBrowsers[browserGroup][candidateBrowser];
      desiredCapability.tunnelIdentifier = tunnelName;
      desiredCapability.tags = ['fpl'];
      browserConfig.push({
        browser: desiredCapability.browserName,
        desiredCapabilities: desiredCapability,
      });
    } else {
      console.error('ERROR: supportedBrowsers.js is empty or incorrectly defined');
    }
  }
  return browserConfig;
};


// config.multiple.microsoft = {
//   browsers: getBrowserConfig('microsoft'),
// };

config.multiple.chrome = {
  browsers: getBrowserConfig('chrome'),
};
// config.multiple.firefox = {
//   browsers: getBrowserConfig('firefox'),
// };
// config.multiple.safari = {
//   browsers: getBrowserConfig('safari'),
// };

console.log('after', config);

exports.config = config;


//
// const supportedBrowsers = require('./crossbrowser/supportedBrowsers.js');

//
// const E2E_FRONTEND_URL = 'https://www.ccd.demo.platform.hmcts.net';
// const SAUCE_USERNAME = 'SivaK';
// const SAUCE_ACCESS_KEY = '65e1e5c6-ae4b-4432-9854-276fff0610d8';
// //process.env.SAUCE_BROWSER = 'chrome';
// //process.env.SAUCE_TUNNEL_IDENTIFIER = 'fpltunnel';
// const browser = 'chrome';
// const tunnelName = 'fpltunnel';
//
//
// const getBrowserConfig = (browserGroup) => {
//   const browserConfig = [];
//   for (const candidateBrowser in supportedBrowsers[browserGroup]) {
//     if (candidateBrowser) {
//       const desiredCapability = supportedBrowsers[browserGroup][candidateBrowser];
//       desiredCapability.tunnelIdentifier = tunnelName;
//       desiredCapability.tags = ['fpl'];
//       browserConfig.push({
//         browser: desiredCapability.browserName,
//         desiredCapabilities: desiredCapability,
//       });
//     } else {
//       console.error('ERROR: supportedBrowsers.js is empty or incorrectly defined');
//     }
//   }
//   return browserConfig;
// };
//
// const setupConfig = {
//   tests: './paths/attendingHearing_test.js',
//   output:  './functional-output',
//   helpers: {
//     WebDriverIO: {
//       url: E2E_FRONTEND_URL,
//       browser: browser,
//       cssSelectorsEnabled: 'true',
//       host: 'ondemand.saucelabs.com',
//       port: 80,
//       user: SAUCE_USERNAME,
//       key: SAUCE_ACCESS_KEY,
//       desiredCapabilities: {},
//     },
//     SauceLabsReportingHelper: {
//       require: './helpers/SauceLabsReportingHelper.js',
//     },
//     //JSWait: { require: './helpers/JSWait.js' },
//     //ElementExist: { require: './helpers/ElementExist.js' },
//     //IdamHelper: { require: './helpers/idamHelper.js' },
//     //SessionHelper: { require: './helpers/SessionHelper.js' },
//   },
//
//   include: {
//     //config: './e2e/config.js',
//     I: '../e2e/steps_file.js',
//     loginPage: './pages/login/loginPage.js',
//     caseListPage: './pages/caseList/caseList.js',
//     createCasePage: './pages/createCase/createCase.js',
//     addEventSummaryPage: './pages/createCase/addEventSummary.js',
//     caseViewPage: './pages/caseView/caseView.js',
//     selectHearingPage: './pages/selectHearing/selectHearing.js',
//     enterGroundsPage: './pages/enterGrounds/enterGrounds.js',
//     enterFactorsAffectingParentingPage: './pages/enterFactorsAffectingParenting/enterFactorsAffectingParenting.js',
//     enterInternationalElementsPage: './pages/internationalElements/enterInternationalElements.js',
//     enterRiskAndHarmToChildPage: './pages/enterRiskAndHarmToChild/enterRiskAndHarmToChild.js',
//     uploadDocumentsPage: './pages/uploadDocuments/uploadDocuments.js',
//     enterApplicantPage: './pages/enterApplicant/enterApplicant.js',
//     enterChildrenPage: './pages/enterChildren/enterChildren.js',
//     enterOtherProceedingsPage: './pages/enterOtherProceedings/enterOtherProceedings.js',
//     attendingHearingPage: './pages/attendingHearing/attendingHearing.js',
//     enterAllocationProposalPage: './pages/enterAllocationProposal/enterAllocationProposal.js',
//     enterRespondentsPage: './pages/enterRespondents/enterRespondents.js',
//     enterOthersPage: './pages/enterOthers/enterOthers.js',
//     ordersNeededPage: './pages/ordersNeeded/ordersNeeded.js',
//     enterFamilyManPage: './pages/enterFamilyMan/enterFamilyMan.js',
//     changeCaseNamePage: './pages/changeCaseName/changeCaseName.js'
//   },
//
//
//   // mocha: {
//   //   reporterOptions: {
//   //     'codeceptjs-cli-reporter': {
//   //       stdout: '-',
//   //       options: { steps: true },
//   //     },
//   //     'mocha-junit-reporter': {
//   //       stdout: '-',
//   //       options: { mochaFile: './functional-output/result.xml'},
//   //     },
//   //     mochawesome: {
//   //       stdout: './functional-output/console.log',
//   //       options: {
//   //         reportDir: './functional-output',
//   //         reportName: 'index',
//   //         inlineAssets: true,
//   //       },
//   //     },
//   //   },
//   // },
//
//   multiple: {
//     microsoft: {
//       browsers: getBrowserConfig('microsoft'),
//     },
//   //   chrome: {
//   //     browsers: getBrowserConfig('chrome'),
//   //   },
//   //   firefox: {
//   //     browsers: getBrowserConfig('firefox'),
//   //   },
//   //   safari: {
//   //     browsers: getBrowserConfig('safari'),
//   //   },
//   },
//   name: 'Frontend Tests',
// };
//
// exports.config = setupConfig;
