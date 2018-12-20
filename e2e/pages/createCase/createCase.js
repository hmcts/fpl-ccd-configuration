const I = actor();

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

  enterCaseName(caseName = 'Barnet Council v Smith') {
    I.fillField(this.enterCaseNamePage.caseName, caseName);
  },

  createNewCase() {
    I.selectOption(this.fields.jurisdiction, 'Family Public Law');
    I.selectOption(this.fields.caseType, 'Care, supervision and EPOs');
    I.selectOption(this.fields.event, 'Open case');
    I.click(this.startButton);
    this.enterCaseName();
    I.click(this.continueButton);
  },
};
