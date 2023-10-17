const { output, event } = require('codeceptjs');
const { Helper } = require('codeceptjs');
//const lodash = require('lodash');
//const retryableErrors = [
//  'Execution context was destroyed',
//  'Node is either not visible or not an HTMLElement',
//  'Node is detached from document',
//  'net::ERR_ABORTED'
//];

module.exports = class HooksHelpers extends Helper {
  getHelper() {
    return this.helpers['Playwright'];
  }

  _init() {
    // Register event listeners
    event.dispatcher.on(event.step.before, (step) => this._beforeStep(step));
    event.dispatcher.on(event.step.after, (step) => this._afterStep(step));
  }

  _test(test) {
    const retries = parseInt(process.env.TEST_RETRIES || '-1');
    if (retries !== -1 || test.retries() === -1) {
      test.retries(retries);
    }
  }

  _beforeStep(step) {
    const helper = this.getHelper();
    if (step.name !== 'amOnPage') {
      return helper.waitForInvisible('xuilib-loading-spinner', 30);
    }
  }

  _afterStep(step) {
    const helper = this.getHelper();
    if (step.name === 'attachFile') {
      output.debug('Waiting for file to finish "Uploading..."');
      return helper.waitForInvisible('//*[contains(text(), "Uploading...")]', 20);
    }
  }
};
