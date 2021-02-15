const { I } = inject();

module.exports = {

  fields: {
    caseId: '#familyManCaseNumber',
  },

  async enterCaseID(caseId) {
    //await I.runAccessibilityTest();
    I.fillField(this.fields.caseId, caseId);
  },
};
