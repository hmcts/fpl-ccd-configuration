//const LATEST_MAC = 'macOS 10.15';
const LATEST_WINDOWS = 'Windows 10';

const supportedBrowsers = {
  // microsoft: {
  //   ie11_win_latest: {
  //     browserName: 'internet explorer',
  //     platformName: LATEST_WINDOWS,
  //     browserVersion: 'latest',
  //     'sauce:options': {
  //       name: 'FPL: IE11',
  //       screenResolution: '1400x1050',
  //     },
  //   },
  //   edge_win_latest: {
  //     browserName: 'MicrosoftEdge',
  //     platformName: LATEST_WINDOWS,
  //     browserVersion: 'latest',
  //     'sauce:options': {
  //       name: 'FPL: Edge_Win10',
  //     },
  //   },
  // },
  // safari: {
  //   safari_mac_latest: {
  //     browserName: 'safari',
  //     platformName: 'macOS 10.14',
  //     browserVersion: 'latest',
  //     'sauce:options': {
  //       name: 'FPL: MAC_SAFARI_LATEST',
  //       seleniumVersion: '3.141.59',
  //       screenResolution: '1400x1050',
  //     },
  //   },
  // },
  chrome: {
    chrome_win_latest: {
      browserName: 'chrome',
      platformName: LATEST_WINDOWS,
      browserVersion: 'latest',
      'sauce:options': {
        name: 'FPL: WIN_CHROME_LATEST',
      },
    },
  //   chrome_mac_latest: {
  //     browserName: 'chrome',
  //     platformName: LATEST_MAC,
  //     browserVersion: 'latest',
  //     'sauce:options': {
  //       name: 'FPL: MAC_CHROME_LATEST',
  //     },
  //   },
  // },
  // firefox: {
  //   firefox_win_latest: {
  //     browserName: 'firefox',
  //     platformName: LATEST_WINDOWS,
  //     browserVersion: 'latest',
  //     'sauce:options': {
  //       name: 'FPL: WIN_FIREFOX_LATEST',
  //     },
  //   },
  //   firefox_mac_latest: {
  //     browserName: 'firefox',
  //     platformName: LATEST_MAC,
  //     browserVersion: 'latest',
  //     'sauce:options': {
  //       name: 'FPL: MAC_FIREFOX_LATEST',
  //     },
  //   },
  },
};

module.exports = supportedBrowsers;
