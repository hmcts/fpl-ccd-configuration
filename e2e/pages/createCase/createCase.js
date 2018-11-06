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
  testCaseName: 'Barnet Council v Smith',
  continueButton: 'Continue',  
  startButton: 'Start',

  enterCaseName() {
    I.fillField(this.enterCaseNamePage.caseName, this.testCaseName);
  },

  createNewCase() {
    I.selectOption(this.fields.jurisdiction, 'Public Law DRAFT');
    I.selectOption(this.fields.caseType, 'Shared_Storage_DRAFT_v0.3');
    I.selectOption(this.fields.event, 'Open case');
    I.click(this.startButton);
    this.enterCaseName();
    I.click(this.continueButton);
  },
};
