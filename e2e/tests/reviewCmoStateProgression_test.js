const issueResolution = require('../fixtures/issueResolution.json');
const finalHearing = require('../fixtures/finalHearing.json');
const cmoHelper = require('../helpers/cmo_helper');

let caseId;

Feature('Review CMO state progression');

Scenario('Judge transitions CMO to issue resolution case state', async (I, caseViewPage, uploadCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage) => {
  caseId = await I.submitNewCaseWithData(issueResolution);
  await cmoHelper.judgeSendsReviewedCmoToAllParties(I, caseId, caseViewPage, uploadCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage);
  caseViewPage.selectTab(caseViewPage.tabs.history);
  I.seeInEventHistoryDetails('End state', 'Issue resolution');
});

Scenario('Judge transitions CMO to final hearing case state', async (I, caseViewPage, uploadCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage) => {
  caseId = await I.submitNewCaseWithData(finalHearing);
  await cmoHelper.judgeSendsReviewedCmoToAllParties(I, caseId, caseViewPage, uploadCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage);
  caseViewPage.selectTab(caseViewPage.tabs.history);
  I.seeInEventHistoryDetails('End state', 'Final hearing');
});
