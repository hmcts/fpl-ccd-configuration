const config = require('../config.js');
const finalHearing = require('../fixtures/finalHearing.json');
const cmoHelper = require('../helpers/cmo_helper');

let caseId;

Feature('Final hearing state progression');

BeforeSuite(async (I) => {
  caseId = await I.submitNewCaseWithData(finalHearing);
});

Scenario('Judge transitions CMO to final hearing case state', async (I, caseViewPage, caseListPage, uploadCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage) => {
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  await cmoHelper.localAuthoritySendsAgreedCmo(I, caseViewPage, uploadCaseManagementOrderEventPage, '1 January 2020', true);
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadCMO);
  await cmoHelper.localAuthoritySendsAgreedCmo(I, caseViewPage, uploadCaseManagementOrderEventPage);
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadCMO);
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.reviewAgreedCmo);
  reviewAgreedCaseManagementOrderEventPage.selectCMOToReview('1 January 2020');
  await I.retryUntilExists(() => I.click('Continue'), '#reviewCMODecision_decision');
  reviewAgreedCaseManagementOrderEventPage.selectSealCmo();
  await I.completeEvent('Save and continue', {summary: 'Summary', description: 'Description'});
  I.seeEventSubmissionConfirmation(config.applicationActions.reviewAgreedCmo);
  caseListPage.navigate();
  caseListPage.changeStateFilter('Final hearing');
  I.click(caseListPage.locateCase(caseId));
});
