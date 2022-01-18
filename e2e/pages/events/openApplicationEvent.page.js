const { I } = inject();
const config = require('../../config');

module.exports = {

  fields: {
    jurisdiction: '#cc-jurisdiction',
    caseType: '#cc-case-type',
    event: '#cc-event',
    outsourcingLAs: '#outsourcingLAs',
  },
  enterCaseNamePage: {
    caseName: '#caseName',
  },
  startButton: 'Start',
  continueButton: 'Continue',

  populateForm(caseName, outsourcingLA) {
    // wait until the dropdown is populated
    I.runAccessibilityTest();
    I.waitForElement(`${this.fields.jurisdiction} > option[value="${config.definition.jurisdiction}"]`, 30);
    I.selectOption(this.fields.jurisdiction, config.definition.jurisdictionFullDesc);
    I.grabCurrentUrl();
    I.waitForElement(`${this.fields.caseType} > option[value="${config.definition.caseType}"]`, 30);
    I.selectOption(this.fields.caseType, config.definition.caseTypeFullDesc);
    I.grabCurrentUrl();
    I.waitForElement(`${this.fields.event} > option[value="openCase"]`, 30);
    I.selectOption(this.fields.event, 'Start application');
    I.grabCurrentUrl();
    if(outsourcingLA) {
      I.click(this.startButton);
      I.waitForSelector(this.fields.outsourcingLAs);
      I.selectOption(this.fields.outsourcingLAs, outsourcingLA);
      I.click('Continue');
      I.grabCurrentUrl();
      I.waitForSelector(this.enterCaseNamePage.caseName);
    } else {
      I.click(this.startButton);
      I.grabCurrentUrl();
      I.waitForSelector(this.enterCaseNamePage.caseName);
    }
    this.enterCaseName(caseName);
  },

  enterCaseName(caseName = 'Barnet Council v Smith') {
    I.waitForElement(this.enterCaseNamePage.caseName, 5);
    I.fillField(this.enterCaseNamePage.caseName, caseName);
  },
};
