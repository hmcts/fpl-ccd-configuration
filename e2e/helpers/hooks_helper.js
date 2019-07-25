/* global process */

module.exports = class HooksHelpers extends Helper {
  _test(test) {
    test.retries(parseInt(process.env.TEST_RETRIES || '2'));
  }
};
