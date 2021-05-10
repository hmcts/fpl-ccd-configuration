const config = require('../config');
const finalHearing = require('../fixtures/caseData/finalHearing.json');
const cmoHelper = require('../helpers/cmo_helper');

let caseId;

Feature('Review CMO state progression').retry(config.maxTestRetries);

Scenario('Judge transitions CMO to final hearing case state', async ({I, caseViewPage, uploadCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage}) => {
  caseId = await I.submitNewCaseWithData(finalHearing);
  await cmoHelper.judgeSendsReviewedCmoToAllParties(I, caseId, caseViewPage, uploadCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage);
  caseViewPage.selectTab(caseViewPage.tabs.history);
  I.seeEndStateForEvent(config.applicationActions.approveOrders, 'Final hearing');
});
