const recorder = require('codeceptjs').recorder;
const lodash = require('lodash');
const retryableErrors = [
  'Execution context was destroyed',
  'Node is either not visible or not an HTMLElement',
  'Node is detached from document',
  'net::ERR_ABORTED'];

module.exports = class HooksHelpers extends Helper {
  _test(test) {
    const retries = parseInt(process.env.TEST_RETRIES || '-1');
    if (retries !== -1 || test.retries() === -1) {
      test.retries(retries);
    }
  }

  _beforeSuite() {
    recorder.retry({
      retries: 10,
      minTimeout: 1000,
      when: err => lodash.some(retryableErrors, retryableError => err.message.indexOf(retryableError) > -1),
    });
  }

  _beforeStep() {
    const helper = this.helpers['Puppeteer'] || this.helpers['WebDriver'];
    return helper.waitForInvisible('xuilib-loading-spinner', 20);
  }

};
