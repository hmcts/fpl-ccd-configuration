const {I} = inject();
const config = require('../config');

module.exports = {

  fields: {
    jurisdiction: '#wb-jurisdiction',
    caseType: '#wb-case-type',
    caseState: '#wb-case-state',
    evidenceHandled: '#evidenceHandled_Yes',
    evidenceNotHandled: '#evidenceHandled_No',
    caseId: 'CCD Case Number',
    caseName: '#caseName',
    search: 'Apply',
    caseList: 'Case list',
    spinner: 'xuilib-loading-spinner',
  },

  navigate() {
    I.waitForText(this.fields.caseList, 30);
    I.click(this.fields.caseList);
  },

  changeStateFilter(desiredState) {
    this.setInitialSearchFields(desiredState);
    I.click(this.fields.search);
  },

  searchForCasesWithHandledEvidences(caseId, state = 'Any') {
    this.setInitialSearchFields(state);
    I.waitForElement(this.fields.evidenceHandled, 30);
    I.fillField(this.fields.caseId, caseId);
    I.click(this.fields.evidenceHandled);
    I.click(this.fields.search);
  },

  async searchForCasesWithId(caseId, state = 'Any') {
    this.setInitialSearchFields(state);
    await I.grabCurrentUrl();
    I.fillField(this.fields.caseId, caseId);
    await I.grabCurrentUrl();
    I.click(this.fields.search);
    await I.grabCurrentUrl();
  },

  searchForCasesWithUnhandledEvidences() {
    I.click(this.fields.evidenceNotHandled);
    I.click(this.fields.search);
  },

  searchForCasesWithName(caseName, state = 'Any') {
    this.setInitialSearchFields(state);
    // wait for our filters to load
    I.waitForVisible(this.fields.caseName, 30);
    I.fillField(this.fields.caseName, caseName);
    I.click(this.fields.search);
    I.runAccessibilityTest().then(() => {});
  },

  setInitialSearchFields(state = 'Any') {
    // wait for initial filters to load
    I.waitForVisible(this.fields.jurisdiction, 30);
    I.selectOption(this.fields.jurisdiction, config.definition.jurisdictionFullDesc);
    I.selectOption(this.fields.caseType, config.definition.caseTypeFullDesc);
    I.selectOption(this.fields.caseState, state);
  },

  locateCase(caseId) {
    return `a[href$='${caseId}']`;
  },

  locateCaseProperty(caseId, columnNumber) {
    const caseRow = this.locateCase(caseId);
    const caseProperty = locate(`//td[${columnNumber}]`);
    return caseProperty.inside(caseRow);
  },

  async verifyCaseIsShareable(caseId) {
    await I.navigateToCaseList();
    await I.retryUntilExists(async () => await this.searchForCasesWithId(caseId), this.locateCase(caseId), false);
    I.seeElement(`#select-${caseId}:not(:disabled)`);
  },

  async verifyCaseIsNotAccessible(caseId) {
    await I.navigateToCaseList();
    await I.grabCurrentUrl();
    await this.searchForCasesWithId(caseId);
    I.waitForInvisible(this.fields.spinner, 30);
    await I.grabCurrentUrl();
    I.see('No cases found. Try using different filters.');
  },

};
