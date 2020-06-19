const { I } = inject();

module.exports = {

  fields: {
    caseId: '#familyManCaseNumber',
  },

  enterCaseID(caseId = 'mockcaseID') {
    I.fillField(this.fields.caseId, caseId);
  },
};
