const I = actor();

module.exports = {
    
  fields: {
    caseId: '#familyManCaseNumber',
  },

  enterCaseID(caseId = 'mock case ID') {
    I.fillField(this.caseIdField, caseId);
  },
};