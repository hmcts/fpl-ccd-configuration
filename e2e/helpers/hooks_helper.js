/* global process */
const recorder = require('codeceptjs').recorder;

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
      when: err => err.message.indexOf('Execution context was destroyed') > -1 || err.message.indexOf('Node is') > -1
      ,
    });
  }

  _afterStep(step) {
    if (step.name === 'attachFile') {
      return this.helpers['Puppeteer'].wait(2); // in seconds; time needed for document store to store uploaded files
    }
  }
};
