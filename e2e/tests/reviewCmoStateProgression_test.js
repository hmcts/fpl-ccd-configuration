const issueResolution = require('../fixtures/issueResolution.json');
const finalHearing = require('../fixtures/finalHearing.json');
const cmoHelper = require('../helpers/cmo_helper');

let caseId;

Feature('Review CMO state progression');

Scenario('Judge transitions CMO to issue resolution case state', async (I, caseViewPage, caseListPage, uploadCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage) => {
  caseId = await I.submitNewCaseWithData(issueResolution);
  await cmoHelper.judgeSendsReviewedCmoToAllParties(I, caseId, caseViewPage, caseListPage, uploadCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage);
  caseListPage.navigate();
  caseListPage.changeStateFilter('Issue resolution');
  I.click(caseListPage.locateCase(caseId));
});

Scenario('Judge transitions CMO to final hearing case state', async (I, caseViewPage, caseListPage, uploadCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage) => {
  caseId = await I.submitNewCaseWithData(finalHearing);
  await cmoHelper.judgeSendsReviewedCmoToAllParties(I, caseId, caseViewPage, caseListPage, uploadCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage);
  caseListPage.navigate();
  caseListPage.changeStateFilter('Final hearing');
  I.click(caseListPage.locateCase(caseId));
});
