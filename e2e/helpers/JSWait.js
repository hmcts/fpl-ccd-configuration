const {getAccessibilityTestResult} = require('./accessibility/runner');
const {generateAccessibilityReport} = require('../tests/reporter/customReporter');
const testConfig = require('../../config.js');

class JSWait extends Helper {

  _finishTest() {
    if (!testConfig.TestForAccessibility) {
      return;
    }
    generateAccessibilityReport(getAccessibilityTestResult());
  }

}
module.exports = JSWait;
