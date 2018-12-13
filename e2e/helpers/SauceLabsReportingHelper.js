/* eslint-disable no-process-env, func-names */

const event = require('codeceptjs').event;
const container = require('codeceptjs').container;
const exec = require('child_process').exec;
const saucelabs_username = 'username';
const saucelabs_accesskey = 'privatekey';
const sauceUsername = process.env.SAUCE_USERNAME || saucelabs_username;
const sauceKey = process.env.SAUCE_ACCESS_KEY || saucelabs_accesskey;

function updateSauceLabsResult(result, sessionId) {
  console.log('SauceOnDemandSessionID=' + sessionId + ' job-name=fpl-ccd-configuration'); /* eslint-disable-line no-console, prefer-template */
  return 'curl -X PUT -s -d \'{"passed": ' + result + '}\' -u ' + sauceUsername + ':' + sauceKey + ' https://saucelabs.com/rest/v1/' + sauceUsername + '/jobs/' + sessionId;
}

module.exports = function() {

  // Setting test success on SauceLabs
  event.dispatcher.on(event.test.passed, () => {

    let sessionId = container.helpers('WebDriverIO').browser.requestHandler.sessionID;
    exec(updateSauceLabsResult('true', sessionId));

  });

  // Setting test failure on SauceLabs
  event.dispatcher.on(event.test.failed, () => {

    let sessionId = container.helpers('WebDriverIO').browser.requestHandler.sessionID;
    exec(updateSauceLabsResult('false', sessionId));

  });
};
