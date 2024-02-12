const recorder = require('codeceptjs').recorder;
//const output = require('codeceptjs').output;
const lodash = require('lodash');
const retryableErrors = [
  'Execution context was destroyed',
  'Node is either not visible or not an HTMLElement',
  'Node is detached from document',
  'net::ERR_ABORTED'];

module.exports = class HooksHelpers extends Helper {
  getHelper() {
    return this.helpers['Puppeteer'] || this.helpers['WebDriver'];
  }

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

  //_beforeStep(step) {
  //const helper = this.getHelper();
  // if (step.name !== 'amOnPage') {
  //   return helper.waitForInvisible('xuilib-loading-spinner', 30);
  // }
  //}

  // _afterStep(step) {
  //   const helper = this.getHelper();
  //   if (step.name === 'attachFile') {
  //     output.debug('Waiting for file to finish "Uploading..."');
  //     return helper.waitForInvisible('//*[contains(text(), "Uploading...")]', 20);
  //   }
  // }
};
