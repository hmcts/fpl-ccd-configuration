const supportedBrowsers = {
  microsoft: {
    ie11_win10: {
      browserName: 'internet explorer',
      name: 'IE11_Win10',
      platform: 'Windows 10',
      ignoreZoomSetting: true,
      nativeEvents: false,
      ignoreProtectedModeSettings: true,
      version: '11',
    },
    edge_win10: {
      browserName: 'MicrosoftEdge',
      name: 'Edge_Win10',
      platform: 'Windows 10',
      ignoreZoomSetting: true,
      nativeEvents: false,
      ignoreProtectedModeSettings: true,
      version: '17.17134',
    },
  },
  chrome: {
    chrome_win_latest: {
      browserName: 'chrome',
      name: 'FPL_WIN_CHROME_LATEST',
      platform: 'Windows 10',
      version: 'latest',
    },
    chrome_mac_latest: {
      browserName: 'chrome',
      name: 'MAC_CHROME_LATEST',
      platform: 'macOS 10.13',
      version: 'latest',
    },
  },
  firefox: {
    firefox_win_latest: {
      browserName: 'firefox',
      name: 'WIN_FIREFOX_LATEST',
      platform: 'Windows 10',
      version: 'latest',
    },
    firefox_mac_latest: {
      browserName: 'firefox',
      name: 'MAC_FIREFOX_LATEST',
      platform: 'macOS 10.13',
      version: 'latest',
    },
  },
  safari: {
    safari11: {
      browserName: 'safari',
      name: 'FPL_SAFARI_11',
      platform: 'macOS 10.13',
      version: '11.1',
      avoidProxy: true,
    },
  },
};

module.exports = supportedBrowsers;
