const config = require('../config.js');
const standardDirectionOrder = require('../fixtures/standardDirectionOrder.json');
const dateFormat = require('dateformat');

const changeRequestReason = 'Timetable for the proceedings is incomplete';
const returnedStatus = 'Returned';
const withJudgeStatus = 'With judge for approval';
const linkName = 'Review agreed CMO';

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
  assertDraftCMO(I, '1', '1 January 2020', withJudgeStatus);
  assertDraftCMO(I, '2', '1 March 2020', withJudgeStatus);
});

Scenario('Judge makes changes to agreed CMO and seals', async (I, caseViewPage, uploadCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage) => {
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
  assertSealedCMO(I, '1', '1 March 2020');
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
  assertDraftCMO(I, '1', '1 January 2020', returnedStatus);
  I.clickHyperlink(linkName, 'reviewCMO');
});

Scenario('Local authority makes changes requested by the judge', async (I, caseViewPage, uploadCaseManagementOrderEventPage) => {
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  assertDraftCMO(I, '1', '1 January 2020', returnedStatus);
  await localAuthoritySendsAgreedCmo(I, caseViewPage, uploadCaseManagementOrderEventPage);
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadCMO);
  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  assertDraftCMO(I, '1', '1 January 2020', withJudgeStatus);
  I.dontSeeElement(locate(`//p/a[text()="${linkName}"]`));
});

Scenario('Judge seals and sends the agreed CMO to parties', async (I, caseViewPage, uploadCaseManagementOrderEventPage, reviewAgreedCaseManagementOrderEventPage) => {
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.reviewAgreedCmo);
  reviewAgreedCaseManagementOrderEventPage.selectSealCmo();
  await I.completeEvent('Save and continue', {summary: 'Summary', description: 'Description'});
  I.seeEventSubmissionConfirmation(config.applicationActions.reviewAgreedCmo);
  caseViewPage.selectTab(caseViewPage.tabs.orders);
  assertSealedCMO(I, '1', '1 March 2020');
  assertSealedCMO(I, '2', '1 January 2020');
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

const assertDraftCMO = function (I, collectionId, hearingDate, status) {
  I.seeInTab([`Draft Case Management Order ${collectionId}`, 'Order'], 'mockFile.docx');
  I.seeInTab([`Draft Case Management Order ${collectionId}`, 'Hearing'], `Case management hearing, ${hearingDate}`);
  I.seeInTab([`Draft Case Management Order ${collectionId}`, 'Date sent'], dateFormat(today, 'dd mmm yyyy'));
  I.seeInTab([`Draft Case Management Order ${collectionId}`, 'Judge'], 'Her Honour Judge Reed');
  I.seeInTab([`Draft Case Management Order ${collectionId}`, 'Status'], status);

  if (status == returnedStatus) {
    I.seeInTab([`Draft Case Management Order ${collectionId}`, 'Changes requested by judge'], changeRequestReason);
  }
};

const assertSealedCMO = function(I, collectionId, hearingDate) {
  I.seeInTab([`Sealed Case Management Order ${collectionId}`, 'Order'], 'mockFile.pdf');
  I.seeInTab([`Sealed Case Management Order ${collectionId}`, 'Hearing'], `Case management hearing, ${hearingDate}`);
  I.seeInTab([`Sealed Case Management Order ${collectionId}`, 'Date issued'], dateFormat(today, 'dd mmm yyyy'));
  I.seeInTab([`Sealed Case Management Order ${collectionId}`, 'Judge'], 'Her Honour Judge Reed');
};
