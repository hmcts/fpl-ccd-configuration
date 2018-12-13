// /* eslint-disable no-console */
const supportedBrowsers = require('./e2e/crossbrowser/supportedBrowsers.js');

const saucelabs_tunnelId= 'reform_tunnel';
const saucelabs_username = 'username';
const saucelabs_accesskey = 'privatekey';
const tunnelName = process.env.SAUCE_TUNNEL_IDENTIFIER || saucelabs_tunnelId;
const saucelabs_browserName = 'chrome';


const { config } = require('./codecept.conf');

//console.log('before', config);

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
