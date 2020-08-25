const issueResolution = require('../fixtures/issueResolution.json');
const finalHearing = require('../fixtures/finalHearing.json');
const cmoHelper = require('../helpers/cmo_helper');

let issueResolutionCaseId, finalHearingCaseId;

Feature('Review CMO state progression');

BeforeSuite(async (I) => {
  issueResolutionCaseId = await I.submitNewCaseWithData(issueResolution);
  finalHearingCaseId = await I.submitNewCaseWithData(finalHearing);
});

Scenario('Judge transitions CMO to issue resolution case state', async (I, caseViewPage, caseListPage, uploadCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage) => {
  await cmoHelper.judgeSendsReviewedCmoToAllParties(I, issueResolutionCaseId, caseViewPage, caseListPage, uploadCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage);
  caseListPage.navigate();
  caseListPage.changeStateFilter('Issue resolution');
  I.click(caseListPage.locateCase(issueResolutionCaseId));
});

Scenario('Judge transitions CMO to final hearing case state', async (I, caseViewPage, caseListPage, uploadCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage) => {
  await cmoHelper.judgeSendsReviewedCmoToAllParties(I, finalHearingCaseId, caseViewPage, caseListPage, uploadCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage);
  caseListPage.navigate();
  caseListPage.changeStateFilter('Final hearing');
  I.click(caseListPage.locateCase(finalHearingCaseId));
});
