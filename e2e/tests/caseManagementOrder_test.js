const config = require('../config.js');
const standardDirectionOrder = require('../fixtures/standardDirectionOrder.json');
const dateFormat = require('dateformat');

let caseId;
let today;

Feature('Case Management Order Journey');

BeforeSuite(async (I) => {
  caseId = await I.submitNewCaseWithData(standardDirectionOrder);
  today = new Date();
});

Scenario('local authority sends agreed CMO to judge', async (I, caseViewPage, sendAgreedCaseManagementOrderEventPage) => {
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  await localAuthoritySendsAgreedCmo(I, caseViewPage, sendAgreedCaseManagementOrderEventPage);
  I.seeEventSubmissionConfirmation(config.applicationActions.sendAgreedCmoToJudge);
  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  assertDraftCaseManagementOrder(I);
});

Scenario('Judge sends agreed CMO back to the local authority', async(I, caseViewPage, reviewAgreedCaseManagementOrderEventPage) => {
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.reviewAgreedCmo);
  I.see('mockFile.pdf');
  reviewAgreedCaseManagementOrderEventPage.selectReturnCmoForChanges();
  reviewAgreedCaseManagementOrderEventPage.enterChangesRequested();
  await I.completeEvent('Save and continue', {summary: 'Summary', description: 'Description'});
  I.seeEventSubmissionConfirmation(config.applicationActions.reviewAgreedCmo);
  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  assertDraftCaseManagementOrder(I, 'Returned', 'PBA number is incorrect');
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  assertDraftCaseManagementOrder(I, 'Returned', 'PBA number is incorrect');
});

Scenario('Judge seals and sends the agreed CMO to parties', async(I, caseViewPage, sendAgreedCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage) => {
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  await localAuthoritySendsAgreedCmo(I, caseViewPage, sendAgreedCaseManagementOrderEventPage);
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.reviewAgreedCmo);
  reviewAgreedCaseManagementOrderEventPage.selectSealCmo();
  await I.completeEvent('Save and continue', {summary: 'Summary', description: 'Description'});
  I.seeEventSubmissionConfirmation(config.applicationActions.reviewAgreedCmo);
  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeInTab(['Sealed Case Management Orders 1', 'Order'], 'mockFile.pdf');
  I.seeInTab(['Sealed Case Management Orders 1', 'Hearing'], 'Case management hearing, 1 January 2020');
  I.seeInTab(['Sealed Case Management Orders 1', 'Date issued'], dateFormat(today, 'dd mmm yyyy'));
  I.seeInTab(['Sealed Case Management Orders 1', 'Judge'], 'Her Honour Judge Reed');
});

const localAuthoritySendsAgreedCmo = async function(I, caseViewPage, sendAgreedCaseManagementOrderEventPage) {
  await caseViewPage.goToNewActions(config.applicationActions.sendAgreedCmoToJudge);
  await sendAgreedCaseManagementOrderEventPage.associateHearing('1 January 2020');
  await I.retryUntilExists(() => I.click('Continue'), '#uploadedCaseManagementOrder');
  await sendAgreedCaseManagementOrderEventPage.uploadCaseManagementOrder(config.testNonEmptyPdfFile);
  await I.completeEvent('Submit');
};

const assertDraftCaseManagementOrder = function(I, status='With judge for approval', changesRequested) {
  I.seeInTab(['Draft Case Management Order 1', 'Order'], 'mockFile.pdf');
  I.seeInTab(['Draft Case Management Order 1', 'Hearing'], 'Case management hearing, 1 January 2020');
  I.seeInTab(['Draft Case Management Order 1', 'Date sent'], dateFormat(today, 'dd mmm yyyy'));
  I.seeInTab(['Draft Case Management Order 1', 'Status'], status);

  if (changesRequested) {
    I.seeInTab(['Draft Case Management Order 1', 'Changes requested by judge'], changesRequested);
  }
};
