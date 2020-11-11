/* global process */
const recorder = require('codeceptjs').recorder;
const lodash = require('lodash');
const retryableErrors = ['Execution context was destroyed', 'Node is either not visible or not an HTMLElement', 'Node is detached from document'];

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

  _afterStep(step) {
    if (step.name === 'attachFile') {
      return this.helpers['Puppeteer'].wait(2); // in seconds; time needed for document store to store uploaded files
    }
  }
};
