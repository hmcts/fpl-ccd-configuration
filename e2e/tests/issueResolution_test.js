const config = require('../config.js');
const issueResolution = require('../fixtures/IssueResolution.json');

let caseId;

Feature('Issue resolution state progression');

BeforeSuite(async (I) => {
  caseId = await I.submitNewCaseWithData(issueResolution);
});

Scenario('Issue resolution', async (I, caseViewPage, uploadCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage) => {
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  await localAuthoritySendsAgreedCmo(I, caseViewPage, uploadCaseManagementOrderEventPage, '1 January 2020', true);
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadCMO);
  await localAuthoritySendsAgreedCmo(I, caseViewPage, uploadCaseManagementOrderEventPage);
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadCMO);
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.reviewAgreedCmo);
  reviewAgreedCaseManagementOrderEventPage.selectSealCmo();
  await I.completeEvent('Save and continue', {summary: 'Summary', description: 'Description'});
  I.seeEventSubmissionConfirmation(config.applicationActions.reviewAgreedCmo);
});

const localAuthoritySendsAgreedCmo = async function (I, caseViewPage, uploadCaseManagementOrderEventPage, hearingDate, multiHearings) {
  await caseViewPage.goToNewActions(config.applicationActions.uploadCMO);

  if (multiHearings) {
    await uploadCaseManagementOrderEventPage.associateHearing(hearingDate);
    await I.retryUntilExists(() => I.click('Continue'), '#uploadedCaseManagementOrder');
  }

  await uploadCaseManagementOrderEventPage.uploadCaseManagementOrder(config.testNonEmptyWordFile);
  await I.completeEvent('Submit');
};


