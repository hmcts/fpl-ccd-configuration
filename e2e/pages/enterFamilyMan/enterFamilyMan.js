const I = actor();

module.exports = {

  caseIdField: '#familyManCaseNumber',

  enterCaseID(caseId = 'mock case ID') {
    I.fillField(this.caseIdField, caseId);
  },
};