const TestData = require('./e2e/config.js');
const config = require('../config.js');

module.exports = {
    timeout: 360000,
    allowedStandards: ['WCAG2AA', 'HMCTS Standards'],
    actions: [
      'wait for element h1.heading-large to be visible',
      'set field #username to ' + TestData.localAuthorityEmail,
      'set field #password to ' + TestData.localAuthorityPassword,
      'click element input[value="Sign in"]',
      'wait for path to not be /login',
      //'wait for element #global-header to be visible'
      //'set field .ccd-dropdown to ' + config.applicationActions.attendingHearing,
      //'click element div.heading-top > a.button',
      //'set field .ccd-dropdown to ' + config.applicationActions.attendingHearing,
      //'set field .ccd-dropdown to ' + config.applicationActions.attendingHearing,
      //'set field .ccd-dropdown to ' + config.applicationActions.attendingHearing
    ]
};
