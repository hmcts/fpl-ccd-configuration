const event = require('codeceptjs').event;
const container = require('codeceptjs').container;
let exec = require('child_process').exec;
//const SAUCE_USERNAME='SivaK';
//const SAUCE_ACCESS_KEY='65e1e5c6-ae4b-4432-9854-276fff0610d8';
//const sauceUsername = process.env.SAUCE_USERNAME;
//const sauceKey = process.env.SAUCE_ACCESS_KEY;

const sauceUsername='SivaK';
const sauceKey = '65e1e5c6-ae4b-4432-9854-276fff0610d8';

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
