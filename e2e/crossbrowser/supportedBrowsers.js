const LATEST_MAC = 'macOS 11.00';
const LATEST_WINDOWS = 'Windows 10';

const defaultSauceOptions = {
  username: process.env.SAUCE_USERNAME,
  accessKey: process.env.SAUCE_ACCESS_KEY,
  tunnelIdentifier: process.env.TUNNEL_IDENTIFIER || 'reformtunnel',
  acceptSslCerts: true,
  tags: ['FPL'],
  screenResolution: '1600x1200',
};

const IE11_WIN = {
  browserName: 'internet explorer',
  platformName: LATEST_WINDOWS,
  browserVersion: 'latest',
  'sauce:options': {
    name: 'FPLA: IE11',
    screenResolution: '1400x1050',
  },
};

const EDGE_WIN_LATEST = {
  browserName: 'MicrosoftEdge',
  platformName: LATEST_WINDOWS,
  browserVersion: 'latest',
  'sauce:options': {
    name: 'FPLA: Edge_Win10',
    screenResolution: '1400x1050',
  },
};

const SAFARI_MAC_LATEST = {
  browserName: 'safari',
  platformName: LATEST_MAC,
  browserVersion: 'latest',
  'sauce:options': {
    name: 'FPLA: MAC_SAFARI',
    seleniumVersion: '3.141.59',
    screenResolution: '1376x1032',
  },
};

const CHROME_WIN_LATEST = {
  browserName: 'chrome',
  platformName: LATEST_WINDOWS,
  browserVersion: 'latest',
  'sauce:options': {
    name: 'FPLA: WIN_CHROME_LATEST',
  },
};

const CHROME_MAC_LATEST = {
  browserName: 'chrome',
  platformName: LATEST_MAC,
  browserVersion: 'latest',
  'sauce:options': {
    name: 'FPLA: MAC_CHROME_LATEST',
  },
};

const FIREFOX_WIN_LATEST = {
  browserName: 'firefox',
  platformName: LATEST_WINDOWS,
  browserVersion: 'latest',
  'sauce:options': {
    name: 'FPLA: WIN_FIREFOX_LATEST',
  },
};

const FIREFOX_MAC_LATEST = {
  browserName: 'firefox',
  platformName: LATEST_MAC,
  browserVersion: 'latest',
  'sauce:options': {
    name: 'FPLA: MAC_FIREFOX_LATEST',
  },
};

function merge(intoObject, fromObject) {
  return Object.assign({}, intoObject, fromObject);
}

function generateBrowserConfig(candidateCapabilities) {
  candidateCapabilities['sauce:options'] = merge(
    defaultSauceOptions, candidateCapabilities['sauce:options'],
  );
  return [{
    browser: candidateCapabilities.browserName,
    capabilities: candidateCapabilities,
  }];
}

module.exports = {
  ie11: {
    browsers: generateBrowserConfig(IE11_WIN),
  },
  edge: {
    browsers: generateBrowserConfig(EDGE_WIN_LATEST),
  },
  chrome_mac: {
    browsers: generateBrowserConfig(CHROME_MAC_LATEST),
  },
  chrome_win: {
    browsers: generateBrowserConfig(CHROME_WIN_LATEST),
  },
  firefox_mac: {
    browsers: generateBrowserConfig(FIREFOX_MAC_LATEST),
  },
  firefox_win: {
    browsers: generateBrowserConfig(FIREFOX_WIN_LATEST),
  },
  safari: {
    browsers: generateBrowserConfig(SAFARI_MAC_LATEST),
  },
  name: 'FPLA FrontEnd Cross-Browser Tests',
};
