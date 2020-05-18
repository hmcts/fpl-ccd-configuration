const { I } = inject();

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

  async populateForm() {
    await I.retryUntilExists(() => {
      I.selectOption(this.fields.jurisdiction, 'Public Law');
      I.selectOption(this.fields.caseType, 'Care, supervision and EPOs');
      I.selectOption(this.fields.event, 'Start application');
      I.click(this.startButton);
    }, this.enterCaseNamePage.caseName);
    this.enterCaseName();
  },

  enterCaseName(caseName = 'Barnet Council v Smith') {
    I.waitForElement(this.enterCaseNamePage.caseName);
    I.fillField(this.enterCaseNamePage.caseName, caseName);
  },
};
