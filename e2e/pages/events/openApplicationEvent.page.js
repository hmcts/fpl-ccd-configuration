const { I } = inject();
const config = require('../../config');

module.exports = {

  fields: {
    jurisdiction: '#cc-jurisdiction',
    caseType: '#cc-case-type',
    event: '#cc-event',
    outsourcingLAs: '#outsourcingLAs',
    representativeType: '#representativeType',
    isOutsourcingLA: '#isOutsourcingLA',
  },
  enterCaseNamePage: {
    caseName: '#caseName',
  },
  startButton: 'Start',
  continueButton: 'Continue',
  localAuthorityRadioButton: '#representativeType-LOCAL_AUTHORITY',
  isOutsourcingRadioButton: '#isOutsourcingLA_Yes',
  notOutsourcingRadioButton: '#isOutsourcingLA_No',

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
      I.waitForSelector(this.fields.representativeType);
      I.click(this.localAuthorityRadioButton);
      I.waitForSelector(this.fields.isOutsourcingLA);
      I.click(this.notOutsourcingRadioButton);
      I.waitForSelector(this.fields.outsourcingLAs);
      I.selectOption(this.fields.outsourcingLAs, outsourcingLA);
      I.click(this.continueButton);
      I.grabCurrentUrl();
      I.waitForSelector(this.enterCaseNamePage.caseName);
    } else {
      I.click(this.startButton);
      I.waitForSelector(this.fields.representativeType);
      I.click(this.localAuthorityRadioButton);
      I.waitForSelector(this.fields.isOutsourcingLA);
      I.click(this.notOutsourcingRadioButton);
      I.click(this.continueButton);
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
