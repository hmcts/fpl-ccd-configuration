const I = actor();

module.exports = {

  caseIdField: '#familyManCaseID',
  
  enterCaseID(caseId = 'mock case ID') {
    I.fillField(this.caseIdField, caseId);
  },
};