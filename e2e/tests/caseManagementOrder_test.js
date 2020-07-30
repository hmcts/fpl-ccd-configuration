const config = require('../config.js');
const standardDirectionOrder = require('../fixtures/standardDirectionOrder.json');
const dateFormat = require('dateformat');

const changeRequestReason = 'Timetable for the proceedings is incomplete';

let caseId;
let today;

Feature('Case Management Order Journey');

BeforeSuite(async (I) => {
  caseId = await I.submitNewCaseWithData(standardDirectionOrder);
  today = new Date();
});

Scenario('Local authority sends agreed CMOs to judge', async (I, caseViewPage, uploadCaseManagementOrderEventPage) => {
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  await localAuthoritySendsAgreedCmo(I, caseViewPage, uploadCaseManagementOrderEventPage, '1 January 2020', true);
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadCMO);
  await localAuthoritySendsAgreedCmo(I, caseViewPage, uploadCaseManagementOrderEventPage);
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadCMO);
  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  assertDraftCMO1(I);
  assertDraftCMO2(I);
});

Scenario('Judge makes changes to draft CMO and seals', async (I, caseViewPage, uploadCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage) => {
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.reviewAgreedCmo);
  reviewAgreedCaseManagementOrderEventPage.selectCMOToReview('1 March 2020');
  await I.retryUntilExists(() => I.click('Continue'), '#reviewCMODecision_decision');
  I.see('mockFile.docx');
  reviewAgreedCaseManagementOrderEventPage.selectMakeChangesToCmo();
  reviewAgreedCaseManagementOrderEventPage.uploadAmendedCmo(config.testNonEmptyWordFile);
  await I.completeEvent('Save and continue', {summary: 'Summary', description: 'Description'});
  I.seeEventSubmissionConfirmation(config.applicationActions.reviewAgreedCmo);
  caseViewPage.selectTab(caseViewPage.tabs.orders);
  assertSealedCMO1(I);
});

Scenario('Judge sends agreed CMO back to the local authority', async (I, caseViewPage, reviewAgreedCaseManagementOrderEventPage) => {
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.reviewAgreedCmo);
  I.see('mockFile.docx');
  reviewAgreedCaseManagementOrderEventPage.selectReturnCmoForChanges();
  reviewAgreedCaseManagementOrderEventPage.enterChangesRequested(changeRequestReason);
  await I.completeEvent('Save and continue', {summary: 'Summary', description: 'Description'});
  I.seeEventSubmissionConfirmation(config.applicationActions.reviewAgreedCmo);
  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  assertReturnedCMO(I);
});

Scenario('Local authority makes changes requested by the judge', async (I, caseViewPage, uploadCaseManagementOrderEventPage) => {
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  assertReturnedCMO(I);
  await localAuthoritySendsAgreedCmo(I, caseViewPage, uploadCaseManagementOrderEventPage);
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadCMO);
  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  assertDraftCMO1(I);
});

Scenario('Judge seals and sends the agreed CMO to parties', async (I, caseViewPage, uploadCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage) => {
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.reviewAgreedCmo);
  reviewAgreedCaseManagementOrderEventPage.selectSealCmo();
  await I.completeEvent('Save and continue', {summary: 'Summary', description: 'Description'});
  I.seeEventSubmissionConfirmation(config.applicationActions.reviewAgreedCmo);
  caseViewPage.selectTab(caseViewPage.tabs.orders);
  assertSealedCMO1(I);
  assertSealedCMO2(I);
});

const localAuthoritySendsAgreedCmo = async function (I, caseViewPage, uploadCaseManagementOrderEventPage, hearing, multiHearings) {
  await caseViewPage.goToNewActions(config.applicationActions.uploadCMO);
  if (multiHearings) {
    await uploadCaseManagementOrderEventPage.associateHearing(hearing);
    await I.retryUntilExists(() => I.click('Continue'), '#uploadedCaseManagementOrder');
  }
  await uploadCaseManagementOrderEventPage.uploadCaseManagementOrder(config.testNonEmptyWordFile);
  await I.completeEvent('Submit');
};

const assertDraftCMO1 = function (I) {
  I.seeInTab(['Draft Case Management Order 1', 'Order'], 'mockFile.docx');
  I.seeInTab(['Draft Case Management Order 1', 'Hearing'], 'Case management hearing, 1 January 2020');
  I.seeInTab(['Draft Case Management Order 1', 'Date sent'], dateFormat(today, 'dd mmm yyyy'));
  I.seeInTab(['Draft Case Management Order 1', 'Status'], 'With judge for approval');
  I.seeInTab(['Draft Case Management Order 1', 'Judge'], 'Her Honour Judge Reed');
};

const assertDraftCMO2 = function (I) {
  I.seeInTab(['Draft Case Management Order 2', 'Order'], 'mockFile.docx');
  I.seeInTab(['Draft Case Management Order 2', 'Hearing'], 'Case management hearing, 1 March 2020');
  I.seeInTab(['Draft Case Management Order 2', 'Date sent'], dateFormat(today, 'dd mmm yyyy'));
  I.seeInTab(['Draft Case Management Order 2', 'Status'], 'With judge for approval');
  I.seeInTab(['Draft Case Management Order 2', 'Judge'], 'Her Honour Judge Reed');
};

const assertReturnedCMO = function (I) {
  I.seeInTab(['Draft Case Management Order 1', 'Order'], 'mockFile.docx');
  I.seeInTab(['Draft Case Management Order 1', 'Hearing'], 'Case management hearing, 1 January 2020');
  I.seeInTab(['Draft Case Management Order 1', 'Date sent'], dateFormat(today, 'dd mmm yyyy'));
  I.seeInTab(['Draft Case Management Order 1', 'Status'], 'Returned');
  I.seeInTab(['Draft Case Management Order 1', 'Judge'], 'Her Honour Judge Reed');
  I.seeInTab(['Draft Case Management Order 1', 'Changes requested by judge'], changeRequestReason);
};

const assertSealedCMO1 = function (I) {
  I.seeInTab(['Sealed Case Management Orders 1', 'Order'], 'mockFile.pdf');
  I.seeInTab(['Sealed Case Management Orders 1', 'Hearing'], 'Case management hearing, 1 March 2020');
  I.seeInTab(['Sealed Case Management Orders 1', 'Date issued'], dateFormat(today, 'dd mmm yyyy'));
  I.seeInTab(['Sealed Case Management Orders 1', 'Judge'], 'Her Honour Judge Reed');
};

const assertSealedCMO2 = function (I) {
  I.seeInTab(['Sealed Case Management Orders 2', 'Order'], 'mockFile.pdf');
  I.seeInTab(['Sealed Case Management Orders 2', 'Hearing'], 'Case management hearing, 1 January 2020');
  I.seeInTab(['Sealed Case Management Orders 2', 'Date issued'], dateFormat(today, 'dd mmm yyyy'));
  I.seeInTab(['Sealed Case Management Orders 2', 'Judge'], 'Her Honour Judge Reed');
};
