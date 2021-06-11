'use strict';

const event = require('codeceptjs').event;
const container = require('codeceptjs').container;
const exec = require('child_process').exec;

function updateSauceLabsResult(result, sessionId) {
  console.log('SauceOnDemandSessionID=' + sessionId + ' job-name=fpl-ccd-configuration');
  return `curl -X PUT -s -d '{"passed": ${result}}' -u ${process.env.SAUCE_USERNAME}:${process.env.SAUCE_ACCESS_KEY} https://eu-central-1.saucelabs.com/rest/v1/${process.env.SAUCE_USERNAME}/jobs/${sessionId}`;
}

module.exports = function () {
  let failedTests = new Set();

  event.dispatcher.on(event.test.passed, (test) => {
    failedTests.delete(test.title);
  });

  event.dispatcher.on(event.test.failed, (test) => {
    failedTests.add(test.title);
  });

  // Setting overall test result on SauceLabs
  event.dispatcher.on(event.all.after, () => {
    const sessionId = container.helpers('WebDriver').browser.sessionId;
    const overallResult = failedTests.size === 0;
    console.log(`Updating Saucelabs with overall result: ${overallResult ? 'PASS' : 'FAIL'}`);
    if (!overallResult) {
      console.log('Failed Tests:');
      failedTests.forEach(test => console.log(`"${test}"`));
    }
    exec(updateSauceLabsResult(overallResult, sessionId));
  });
};
