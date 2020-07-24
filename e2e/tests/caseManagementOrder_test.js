const config = require('../config.js');
const standardDirectionOrder = require('../fixtures/standardDirectionOrder.json');
const dateFormat = require('dateformat');

let caseId;

Feature('Case Management Order Journey');

BeforeSuite(async (I) => {
  caseId = await I.submitNewCaseWithData(standardDirectionOrder);
});

Scenario('local authority sends agreed CMO to judge', async (I, caseViewPage, uploadCaseManagementOrderEventPage) => {
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.uploadCMO);
  await uploadCaseManagementOrderEventPage.associateHearing('1 January 2020');
  await I.retryUntilExists(() => I.click('Continue'), '#uploadedCaseManagementOrder');
  await uploadCaseManagementOrderEventPage.uploadCaseManagementOrder(config.testNonEmptyPdfFile);
  await I.completeEvent('Submit');
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadCMO);
  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  I.seeInTab(['Draft Case Management Order 1', 'Order'], 'mockFile.pdf');
  I.seeInTab(['Draft Case Management Order 1', 'Hearing'], 'Case management hearing, 1 January 2020');
  I.seeInTab(['Draft Case Management Order 1', 'Date sent'], dateFormat(new Date(), 'dd mmm yyyy'));
  I.seeInTab(['Draft Case Management Order 1', 'Status'], 'With judge for approval');
});
