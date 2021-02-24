const { I } = inject();

module.exports = {

  fields: {
    caseId: '#familyManCaseNumber',
  },

  async enterCaseID(caseId) {
    await I.runAccessibilityTest();
    console.log('enter family man case number');
    I.fillField(this.fields.caseId, caseId);
  },
};
