/* global process */

module.exports = class HooksHelpers extends Helper {
  _test(test) {
    test.retries(parseInt(process.env.TEST_RETRIES || '0'));
  }

  _afterStep(step) {
    if (step.name === 'attachFile') {
      return this.helpers['Puppeteer'].wait(2); // in seconds; time needed for document store to store uploaded files
    }
  }
};
