const {I} = inject();
const config = require('../config');

module.exports = {

  fields: {
    jurisdiction: '#wb-jurisdiction',
    caseType: '#wb-case-type',
    caseState: '#wb-case-state',
    evidenceHandled: '#evidenceHandled-Yes',
    evidenceNotHandled: '#evidenceHandled-No',
    caseName: '#caseName',
    search: 'Apply',
    caseList: 'Case list',
  },

  navigate(){
    I.click(this.fields.caseList);
  },

  changeStateFilter(desiredState) {
    this.searchForCase(desiredState);
    I.click(this.fields.search);
  },

  searchForCasesWithHandledEvidences(submittedAt, state='Any') {
    this.searchForCase(state);
    I.waitForElement(this.fields.evidenceHandled);
    I.fillDate(submittedAt);
    I.click(this.fields.evidenceHandled);
    I.click(this.fields.search);
  },

  searchForCasesWithUnhandledEvidences() {
    I.click(this.fields.evidenceNotHandled);
    I.click(this.fields.search);
  },

  searchForCasesWithName(caseName, state='Any') {
    this.searchForCase(state);
    // wait for our filters to load
    I.waitForVisible(this.fields.caseName, 10);
    I.fillField(this.fields.caseName, caseName);
    I.click(this.fields.search);
  },

  searchForCase(state='Any') {
    // wait for initial filters to load
    I.waitForVisible(this.fields.jurisdiction, 20);
    I.selectOption(this.fields.jurisdiction, config.definition.jurisdictionFullDesc);
    I.selectOption(this.fields.caseType, config.definition.caseTypeFullDesc);
    I.selectOption(this.fields.caseState, state);
  },

  locateCase(caseId){
    return locate(`//ccd-search-result/table//tr[//a[contains(@href,'${caseId}')]]`);
  },

  locateCaseProperty(caseId, columnNumber){
    const caseRow = this.locateCase(caseId);
    const caseProperty = locate(`//td[${columnNumber}]`);
    return caseProperty.inside(caseRow);
  },

};
