const {getAccessibilityTestResult} = require('./accessibility/runner');
const {generateAccessibilityReport} = require('../reporters/accessibility-reporter/customReporter');
const testConfig = require('../config.js');

class Generate_report_helper extends Helper {

  _finishTest() {
    if (!testConfig.TestForAccessibility) {
      return;
    }
    generateAccessibilityReport(getAccessibilityTestResult());
  }

}
module.exports = Generate_report_helper;
