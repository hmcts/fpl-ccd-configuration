const {I} = inject();
const config = require('../config');
const dateFormat = require('dateformat');

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

  async searchForCasesWithHandledEvidences(submittedAt, state = 'Any') {
    this.setInitialSearchFields(state);
    I.waitForElement(this.fields.evidenceHandled, 30);
    await I.fillDate(submittedAt);
    I.click(this.fields.evidenceHandled);
    this.search({state: state, evidenceHandled: 'Yes', dateSubmitted: dateFormat(submittedAt, 'isoDate')});
  },

  searchForCasesWithName(caseName, state='Any') {
    this.setInitialSearchFields(state);
    // wait for our filters to load
    I.waitForVisible(this.fields.caseName, 30);
    I.fillField(this.fields.caseName, caseName);

    this.search({state: state, caseName: normaliseCaseName(caseName)});
  },

  search(searchFields) {
    I.click(this.fields.search);

    if (searchFields['state'] === 'Any') {
      delete searchFields.state; // this is not added to the url
    }

    searchFields['ctid'] = config.definition.caseType;

    I.waitForResponse(response => urlContainsSearchFields(response.url(), searchFields) && response.request().method() === 'POST', 240);

    I.wait(1); // give time for the list to populate
  },

  setInitialSearchFields(state='Any') {
    // wait for initial filters to load
    I.waitForVisible(this.fields.jurisdiction, 30);
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

const normaliseCaseName = (caseName) => {
  return caseName.replaceAll(' ', '%20');
};

const urlContainsSearchFields = (url, searchFields) => {
  return Object.keys(searchFields).every(field => url.includes(`${field}=${searchFields[field]}`));
};
