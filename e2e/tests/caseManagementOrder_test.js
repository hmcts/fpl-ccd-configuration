const config = require('../config.js');
const standardDirectionOrder = require('../fixtures/caseData/prepareForHearing.json');
const cmoHelper = require('../helpers/cmo_helper');
const dateFormat = require('dateformat');

const changeRequestReason = 'Timetable for the proceedings is incomplete';
const returnedStatus = 'Returned';
const withJudgeStatus = 'With judge for approval';
const draftStatus = 'Draft order, to review before hearing';
const linkLabel = 'Review agreed CMO';

let caseId;
let today;

Feature('Case Management Order Journey');

BeforeSuite(async ({I}) => {
  caseId = await I.submitNewCaseWithData(standardDirectionOrder);
  today = new Date();
});

Scenario('Local authority sends agreed CMOs to judge @failure', async ({I, caseViewPage, uploadCaseManagementOrderEventPage}) => {
  const supportingDocs = {name: 'case summary', notes: 'this is the case summary', file: config.testFile, fileName: 'mockFile.txt'};

  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);

  await cmoHelper.localAuthoritySendsAgreedCmo(I, caseViewPage, uploadCaseManagementOrderEventPage, 'Case management hearing, 1 January 2020');
  await cmoHelper.localAuthoritySendsAgreedCmo(I, caseViewPage, uploadCaseManagementOrderEventPage, 'Case management hearing, 1 March 2020', supportingDocs);
  await cmoHelper.localAuthorityUploadsDraftCmo(I, caseViewPage, uploadCaseManagementOrderEventPage, 'Case management hearing, 1 January 2050', supportingDocs);

  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  assertDraftCMO(I, '1', '1 January 2020', withJudgeStatus);
  assertDraftCMO(I, '2', '1 March 2020', withJudgeStatus);
  assertDraftCMO(I, '3', '1 January 2050', draftStatus, supportingDocs);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.seeInTab(['Further evidence documents 1', 'Hearing'], 'Case management hearing, 1 March 2020');
  I.seeInTab(['Further evidence documents 1', 'Documents 1', 'Document name'], supportingDocs.name);
  I.seeInTab(['Further evidence documents 1', 'Documents 1', 'Notes'], supportingDocs.notes);
  I.seeInTab(['Further evidence documents 1', 'Documents 1', 'File'], supportingDocs.fileName);
});

Scenario('Judge makes changes to agreed CMO and seals @failure', async ({I, caseViewPage, reviewAgreedCaseManagementOrderEventPage}) => {
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.reviewAgreedCmo);
  reviewAgreedCaseManagementOrderEventPage.selectCMOToReview('1 March 2020');
  await I.goToNextPage();
  I.see('mockFile.docx');
  reviewAgreedCaseManagementOrderEventPage.selectMakeChangesToCmo();
  reviewAgreedCaseManagementOrderEventPage.uploadAmendedCmo(config.testWordFile);
  await I.completeEvent('Save and continue', {summary: 'Summary', description: 'Description'});
  I.seeEventSubmissionConfirmation(config.applicationActions.reviewAgreedCmo);
  caseViewPage.selectTab(caseViewPage.tabs.orders);
  assertSealedCMO(I, '1', '1 March 2020');
});

Scenario('Judge sends agreed CMO back to the local authority @failure', async ({I, caseViewPage, reviewAgreedCaseManagementOrderEventPage}) => {
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);
  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  await I.startEventViaHyperlink(linkLabel);
  I.see('mockFile.docx');
  reviewAgreedCaseManagementOrderEventPage.selectReturnCmoForChanges();
  reviewAgreedCaseManagementOrderEventPage.enterChangesRequested(changeRequestReason);
  await I.completeEvent('Save and continue', {summary: 'Summary', description: 'Description'});
  I.seeEventSubmissionConfirmation(config.applicationActions.reviewAgreedCmo);
  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  assertDraftCMO(I, '1', '1 January 2020', returnedStatus);
});

Scenario('Local authority makes changes requested by the judge @failure', async ({I, caseViewPage, uploadCaseManagementOrderEventPage}) => {
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  assertDraftCMO(I, '1', '1 January 2020', returnedStatus);
  await cmoHelper.localAuthoritySendsAgreedCmo(I, caseViewPage, uploadCaseManagementOrderEventPage, 'Case management hearing, 1 January 2020');
  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  assertDraftCMO(I, '1', '1 January 2020', withJudgeStatus);
  I.dontSee(linkLabel);
});

Scenario('Judge seals and sends the agreed CMO to parties @failure', async ({I, caseViewPage, reviewAgreedCaseManagementOrderEventPage}) => {
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.reviewAgreedCmo);
  reviewAgreedCaseManagementOrderEventPage.selectSealCmo();
  await I.completeEvent('Save and continue', {summary: 'Summary', description: 'Description'});
  I.seeEventSubmissionConfirmation(config.applicationActions.reviewAgreedCmo);
  caseViewPage.selectTab(caseViewPage.tabs.orders);
  assertSealedCMO(I, '1', '1 March 2020');
  assertSealedCMO(I, '2', '1 January 2020');
  caseViewPage.selectTab(caseViewPage.tabs.documentsSentToParties);
  assertDocumentSentToParties(I);
});

const assertDraftCMO = function (I, collectionId, hearingDate, status, supportingDocs) {
  const draftCMO = `Draft case management order ${collectionId}`;

  I.seeInTab([draftCMO, 'Order'], 'mockFile.docx');
  I.seeInTab([draftCMO, 'Hearing'], `Case management hearing, ${hearingDate}`);
  I.seeInTab([draftCMO, 'Date sent'], dateFormat(today, 'd mmm yyyy'));
  I.seeInTab([draftCMO, 'Judge'], 'Her Honour Judge Reed');
  I.seeInTab([draftCMO, 'Status'], status);

  if (status === returnedStatus) {
    I.seeInTab([draftCMO, 'Changes requested by judge'], changeRequestReason);
  }

  if (supportingDocs) {
    I.seeInTab([draftCMO, 'Case summary or supporting documents 1', 'Document name'], supportingDocs.name);
    I.seeInTab([draftCMO, 'Case summary or supporting documents 1', 'Notes'], supportingDocs.notes);
    I.seeInTab([draftCMO, 'Case summary or supporting documents 1', 'File'], supportingDocs.fileName);
  }
};

const assertSealedCMO = function (I, collectionId, hearingDate) {
  const sealedCMO = `Sealed Case Management Order ${collectionId}`;

  I.seeInTab([sealedCMO, 'Order'], 'mockFile.pdf');
  I.seeInTab([sealedCMO, 'Hearing'], `Case management hearing, ${hearingDate}`);
  I.seeInTab([sealedCMO, 'Date issued'], dateFormat(today, 'd mmm yyyy'));
  I.seeInTab([sealedCMO, 'Judge'], 'Her Honour Judge Reed');
};

const assertDocumentSentToParties = function (I) {
  I.seeInTab(['Party 1', 'Representative name'], 'Marie Kelly');
  I.seeInTab(['Party 1', 'Document 1', 'File'], 'mockFile.pdf');
};
