const config = require('../config.js');
const standardDirectionOrder = require('../fixtures/standardDirectionOrder.json');
const dateFormat = require('dateformat');

let caseId;

Feature('Case Management Order Journey');

BeforeSuite(async (I) => {
  caseId = await I.submitNewCaseWithData(standardDirectionOrder);
});

Scenario('local sends agreed CMO to judge', async (I, caseViewPage, sendAgreedCaseManagementOrderEventPage) => {
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.sendAgreedCmoToJudge);
  await sendAgreedCaseManagementOrderEventPage.associateHearing('1 January 2020');
  await I.retryUntilExists(() => I.click('Continue'), '#uploadedCaseManagementOrder');
  await sendAgreedCaseManagementOrderEventPage.uploadCaseManagementOrder(config.testNonEmptyPdfFile);
  await I.completeEvent('Submit');
  I.seeEventSubmissionConfirmation(config.applicationActions.sendAgreedCmoToJudge);
  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  I.seeInTab(['Draft Case Management Order 1', 'Order'], 'mockFile.pdf');
  I.seeInTab(['Draft Case Management Order 1', 'Hearing'], 'Case management hearing, 1 January 2020');
  I.seeInTab(['Draft Case Management Order 1', 'Date sent'], dateFormat(new Date(), 'dd mmm yyyy'));
  I.seeInTab(['Draft Case Management Order 1', 'Status'], 'With judge for approval');
});

Scenario('Judge sees Action CMO placeholder when CMO is not in Judge Review', async (I, caseViewPage) => {
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);

  await caseViewPage.goToNewActions(config.applicationActions.actionCaseManagementOrder);
  await I.see('You cannot edit this order');
  await I.see('You can only review the draft order after it has been submitted');
});
