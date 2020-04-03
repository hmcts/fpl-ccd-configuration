const {I} = inject();

module.exports = {

  fields: {
    caseState: '#wb-case-state',
    evidenceHandled: '#evidenceHandled-Yes',
    evidenceNotHandled: '#evidenceHandled-No',
    search: 'Apply',
    caseList: 'Case List',
  },

  navigate(){
    I.click(this.fields.caseList);
  },

  changeStateFilter(desiredState) {
    I.selectOption(this.fields.caseState, desiredState);
    I.click(this.fields.search);
  },

  searchForCasesWithHandledEvidences() {
    I.waitForElement(this.fields.evidenceHandled);
    I.click(this.fields.evidenceHandled);
    I.click(this.fields.search);
  },

  searchForCasesWithUnhandledEvidences() {
    I.click(this.fields.evidenceNotHandled);
    I.click(this.fields.search);
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
