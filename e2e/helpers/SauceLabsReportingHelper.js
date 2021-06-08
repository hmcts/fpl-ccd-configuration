'use strict';

const event = require('codeceptjs').event;
const container = require('codeceptjs').container;
const exec = require('child_process').exec;

function updateSauceLabsResult(result, sessionId) {
  console.log('SauceOnDemandSessionID=' + sessionId + ' job-name=fpl-ccd-configuration');
  return 'curl -X PUT -s -d \'{"passed": ' + result + '}\' -u ' + process.env.SAUCE_USERNAME + ':' + process.env.SAUCE_ACCESS_KEY + ' https://eu-central-1.saucelabs.com/rest/v1/' + process.env.SAUCE_USERNAME + '/jobs/' + sessionId;
}

module.exports = function() {
  let overallResult;

  event.dispatcher.on(event.test.passed, () => {
    // skip if the test run has already failed
    if (overallResult === undefined) {
      overallResult = true;
    }
  });

  event.dispatcher.on(event.test.failed, () => {
    overallResult = false;
  });

  // Setting overall test result on SauceLabs
  event.dispatcher.on(event.all.after, () => {
    const sessionId = container.helpers('WebDriver').browser.sessionId;
    exec(updateSauceLabsResult(!!overallResult, sessionId));
  });
};
