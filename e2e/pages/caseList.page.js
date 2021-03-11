const {I} = inject();
const config = require('../config');

module.exports = {

  fields: {
    jurisdiction: '#wb-jurisdiction',
    caseType: '#wb-case-type',
    caseState: '#wb-case-state',
    evidenceHandled: '#evidenceHandled-Yes',
    evidenceNotHandled: '#evidenceHandled-No',
    caseId: 'CCD Case Number',
    caseName: '#caseName',
    search: 'Apply',
    caseList: 'Case list',
  },

  navigate(){
    I.click(this.fields.caseList);
  },

  async changeStateFilter(desiredState) {
    await this.setInitialSearchFields(desiredState);
    I.click(this.fields.search);
  },

  async searchForCasesWithHandledEvidences(caseId, state = 'Any') {
    await this.setInitialSearchFields(state);
    I.waitForElement(this.fields.evidenceHandled, 30);
    I.fillField(this.fields.caseId, caseId);
    I.click(this.fields.evidenceHandled);
    I.click(this.fields.search);
  },

  async searchForCasesWithId(caseId, state = 'Any') {
    await this.setInitialSearchFields(state);
    I.fillField(this.fields.caseId, caseId);
    I.click(this.fields.search);
  },

  searchForCasesWithUnhandledEvidences() {
    I.click(this.fields.evidenceNotHandled);
    I.click(this.fields.search);
  },

  async searchForCasesWithName(caseName, state='Any') {
    await this.setInitialSearchFields(state);
    // wait for our filters to load
    I.waitForVisible(this.fields.caseName, 30);
    I.fillField(this.fields.caseName, caseName);
    I.click(this.fields.search);
    await I.runAccessibilityTest();
  },

  async setInitialSearchFields(state='Any') {
    // wait for initial filters to load
    await I.waitForSpinnerToFinish();
    I.waitForVisible(this.fields.jurisdiction, 30);
    I.selectOption(this.fields.jurisdiction, config.definition.jurisdictionFullDesc);
    I.selectOption(this.fields.caseType, config.definition.caseTypeFullDesc);
    I.selectOption(this.fields.caseState, state);
  },

  locateCase(caseId){
    return `a[href$='${caseId}']`;
  },

  locateCaseProperty(caseId, columnNumber){
    const caseRow = this.locateCase(caseId);
    const caseProperty = locate(`//td[${columnNumber}]`);
    return caseProperty.inside(caseRow);
  },

  async verifyCaseIsShareable(caseId){
    I.navigateToCaseList();
    await I.retryUntilExists(async () => await this.searchForCasesWithId(caseId), this.locateCase(caseId), false);
    I.seeElement(`#select-${caseId}:not(:disabled)`);
  },

};
