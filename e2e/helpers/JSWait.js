const {getAccessibilityTestResult} = require('./accessibility/runner');
const {generateAccessibilityReport} = require('../tests/reporter/customReporter');
const testConfig = require('e2e/config.js');

class JSWait extends codecept_helper {

  _finishTest() {
    if (!testConfig.TestForAccessibility) {
      return;
    }
    generateAccessibilityReport(getAccessibilityTestResult());
  }

}

