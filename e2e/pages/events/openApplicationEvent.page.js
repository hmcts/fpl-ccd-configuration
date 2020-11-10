const { I } = inject();
const config = require('../../config');

module.exports = {

  fields: {
    jurisdiction: 'jurisdiction',
    caseType: 'case-type',
    event: 'event',
  },
  enterCaseNamePage: {
    caseName: '#caseName',
  },
  startButton: 'Start',
  continueButton: 'Continue',

  async populateForm(caseName) {
    await I.goToNextPage2(() => {
      I.selectOption(this.fields.jurisdiction, config.definition.jurisdictionFullDesc);
      I.selectOption(this.fields.caseType, config.definition.caseTypeFullDesc);
      I.selectOption(this.fields.event, 'Start application');
      I.click(this.startButton);
    });
    this.enterCaseName(caseName);
  },

  enterCaseName(caseName = 'Barnet Council v Smith') {
    I.waitForElement(this.enterCaseNamePage.caseName);
    I.fillField(this.enterCaseNamePage.caseName, caseName);
  },
};
