const config = require('../config');
const issueResolution = require('../fixtures/caseData/issueResolution.json');
const finalHearing = require('../fixtures/caseData/finalHearing.json');
const cmoHelper = require('../helpers/cmo_helper');

let caseId;

Feature('Review CMO state progression');

Scenario('Judge transitions CMO to issue resolution case state', async (I, caseViewPage, caseListPage, uploadCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage) => {
  caseId = await I.submitNewCaseWithData(issueResolution);
  await cmoHelper.judgeSendsReviewedCmoToAllParties(I, caseId, caseViewPage, caseListPage, uploadCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage);
  caseViewPage.selectTab(caseViewPage.tabs.history);
  await I.seeEndStateForEvent(config.applicationActions.reviewAgreedCmo, 'Issue resolution');
});

Scenario('Judge transitions CMO to final hearing case state', async (I, caseViewPage, caseListPage, uploadCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage) => {
  caseId = await I.submitNewCaseWithData(finalHearing);
  await cmoHelper.judgeSendsReviewedCmoToAllParties(I, caseId, caseViewPage, caseListPage, uploadCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage);
  caseViewPage.selectTab(caseViewPage.tabs.history);
  await I.seeEndStateForEvent(config.applicationActions.reviewAgreedCmo, 'Final hearing');
});
