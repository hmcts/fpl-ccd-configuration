const { I } = inject();

module.exports = {

  fields: {
    caseId: '#familyManCaseNumber',
  },

  enterCaseID(caseId) {
    I.fillField(this.fields.caseId, caseId);
  },
};
