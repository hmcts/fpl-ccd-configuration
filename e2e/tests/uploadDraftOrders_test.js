const config = require('../config.js');
const standardDirectionOrder = require('../fixtures/caseData/prepareForHearing.json');
const cmoHelper = require('../helpers/cmo_helper');
const dateFormat = require('dateformat');

const changeRequestReason = 'Timetable for the proceedings is incomplete';
const returnedStatus = 'Returned';
const noHearing = 'No hearing';
const withJudgeStatus = 'With judge for approval';
const draftStatus = 'Draft order, to review before hearing';
const linkLabel = 'Review agreed CMO';
const agreedCMO = 'Agreed CMO discussed at hearing';
const draftCMO = 'Draft CMO from advocates\' meeting';
const hearing1 = 'Case management hearing, 1 January 2020';
const hearing2 = 'Case management hearing, 1 March 2020';
const hearing3 = 'Case management hearing, 1 January 2050';
const hearing4 = noHearing;

const draftOrder1 = {
  title: 'draft order 1',
  file: config.testWordFile,
};

const draftOrder1Updated = {
  title: 'draft order 1 Updated',
  file: config.testWordFile,
  number: 2,
};
const draftOrder2 = {
  title: 'draft order 2',
  file: config.testWordFile,
  number: 2,
};
const draftOrder3 = {
  title: 'draft order 3',
  file: config.testWordFile,
};
const supportingDoc = {name: 'case summary', notes: 'this is the case summary', file: config.testFile, fileName: 'mockFile.txt'};


let caseId;
let today;

Feature('Upload Draft Orders Journey');

BeforeSuite(async ({I}) => {
  caseId = await I.submitNewCaseWithData(standardDirectionOrder);
  today = new Date();
});

Scenario('Local authority uploads draft orders', async ({I, caseViewPage, uploadCaseManagementOrderEventPage}) => {

  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);

  await cmoHelper.localAuthoritySendsAgreedCmo(I, caseViewPage, uploadCaseManagementOrderEventPage, hearing1,null, draftOrder1);
  await cmoHelper.localAuthoritySendsAgreedCmo(I, caseViewPage, uploadCaseManagementOrderEventPage, hearing2, supportingDoc);
  await cmoHelper.localAuthorityUploadsDraftCmo(I, caseViewPage, uploadCaseManagementOrderEventPage, hearing3, supportingDoc);
  await cmoHelper.localAuthorityUploadsC21(I, caseViewPage, uploadCaseManagementOrderEventPage, draftOrder2, hearing1);
  await cmoHelper.localAuthorityUploadsC21(I, caseViewPage, uploadCaseManagementOrderEventPage, draftOrder3);

  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);

  assertDraftCMO(I, 1, hearing1, [
    {title: agreedCMO, status: withJudgeStatus},
    {title: draftOrder1.title, status: withJudgeStatus},
    {title: draftOrder2.title, status: withJudgeStatus},
  ]);
  assertDraftCMO(I, 2, hearing2, [
    {title: agreedCMO, status: withJudgeStatus, supportingDocs: supportingDoc},
  ]);
  assertDraftCMO(I, 3, hearing3, [
    {title: draftCMO, status: draftStatus, supportingDocs: supportingDoc},
  ]);
  assertDraftCMO(I, 4, hearing4, [
    {title: draftOrder3.title, status: withJudgeStatus},
  ]);

  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.seeInTab(['Further evidence documents for hearings 1', 'Hearing'], hearing2);
  I.seeInTab(['Further evidence documents for hearings 1', 'Documents 1', 'Document name'], supportingDoc.name);
  I.seeInTab(['Further evidence documents for hearings 1', 'Documents 1', 'Notes'], supportingDoc.notes);
  I.seeInTab(['Further evidence documents for hearings 1', 'Documents 1', 'File'], supportingDoc.fileName);
});

Scenario('Judge makes changes to agreed CMO and seals', async ({I, caseViewPage, reviewAgreedCaseManagementOrderEventPage}) => {
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);

  await caseViewPage.goToNewActions(config.applicationActions.approveOrders);
  reviewAgreedCaseManagementOrderEventPage.selectCMOToReview(hearing2);
  await I.goToNextPage();
  I.see('mockFile.docx');
  reviewAgreedCaseManagementOrderEventPage.selectMakeChangesToCmo();
  reviewAgreedCaseManagementOrderEventPage.uploadAmendedCmo(config.testWordFile);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.approveOrders);

  caseViewPage.selectTab(caseViewPage.tabs.orders);
  assertSealedCMO(I, 1, hearing2);

  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  I.dontSeeInTab(hearing2);
});

Scenario('Judge sends agreed CMO back to the local authority', async ({I, caseViewPage, reviewAgreedCaseManagementOrderEventPage}) => {
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);

  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  await I.startEventViaHyperlink(linkLabel);
  reviewAgreedCaseManagementOrderEventPage.selectCMOToReview(hearing1);
  await I.goToNextPage();
  I.see('mockFile.docx');

  reviewAgreedCaseManagementOrderEventPage.selectReturnCmoForChanges();
  reviewAgreedCaseManagementOrderEventPage.enterChangesRequested(changeRequestReason);
  reviewAgreedCaseManagementOrderEventPage.selectReturnC21ForChanges(1);
  reviewAgreedCaseManagementOrderEventPage.enterChangesRequestedC21(1,'note2');

  await I.completeEvent('Save and continue',null, false, '.alert-warning');
  //TODO replace wait with:  I.completeEvent('Save and continue') once submitted callback is fixed

  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);

  assertDraftCMO(I, 1, hearing1, [
    {title: draftOrder2.title, status: withJudgeStatus},
  ]);
});

Scenario('Local authority makes changes requested by the judge', async ({I, caseViewPage, uploadCaseManagementOrderEventPage}) => {
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);

  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  assertDraftCMO(I, 1, hearing1, [
    {title: draftOrder2.title, status: withJudgeStatus},
  ]);

  await cmoHelper.localAuthoritySendsAgreedCmo(I, caseViewPage, uploadCaseManagementOrderEventPage, hearing1,null, draftOrder1Updated);

  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);

  assertDraftCMO(I, 1, hearing1, [
    {title: agreedCMO, status: withJudgeStatus},
    {title: draftOrder2.title, status: withJudgeStatus},
    {title: draftOrder1Updated.title, status: withJudgeStatus},
  ]);

  I.dontSee(linkLabel);
});

xScenario('Judge seals and sends the agreed CMO to parties', async ({I, caseViewPage, reviewAgreedCaseManagementOrderEventPage}) => {
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);

  await caseViewPage.goToNewActions(config.applicationActions.approveOrders);
  reviewAgreedCaseManagementOrderEventPage.selectSealCmo();
  await I.completeEvent('Save and continue', {summary: 'Summary', description: 'Description'});
  I.seeEventSubmissionConfirmation(config.applicationActions.approveOrders);

  caseViewPage.selectTab(caseViewPage.tabs.orders);
  assertSealedCMO(I, 1, hearing2);
  assertSealedCMO(I, 2, hearing1);

  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  assertDraftCMO(I, 1, hearing1, [
    {title: draftOrder1.title, status: withJudgeStatus},
    {title: draftOrder2.title, status: withJudgeStatus},
  ]);

  caseViewPage.selectTab(caseViewPage.tabs.documentsSentToParties);
  assertDocumentSentToParties(I);
});

const assertDraftCMO = function (I, collectionId, hearingName, orders, title, status, supportingDocs) {
  const hearing = `Hearing ${collectionId}`;

  I.seeInTab([hearing, 'Hearing'], hearingName);

  if(hearingName !== noHearing) {
    I.seeInTab([hearing, 'Judge'], 'Her Honour Judge Reed');
  }

  orders.forEach((order, index) => {

    const draft = `Draft ${index + 1}`;

    I.seeInTab([hearing, draft, 'Title'], order.title);
    I.seeInTab([hearing, draft, 'Order'], 'mockFile.docx');
    I.seeInTab([hearing, draft, 'Status'], order.status);
    I.seeInTab([hearing, draft, 'Date sent'], dateFormat(today, 'd mmm yyyy'));

    if (order.status === returnedStatus) {
      I.seeInTab([hearing, draft, 'Changes requested by judge'], changeRequestReason);
    }

    if (supportingDocs) {
      I.seeInTab([hearing, draft, 'Case summary or supporting documents 1', 'Document name'], supportingDocs.name);
      I.seeInTab([hearing, draft, 'Case summary or supporting documents 1', 'Notes'], supportingDocs.notes);
      I.seeInTab([hearing, draft, 'Case summary or supporting documents 1', 'File'], supportingDocs.fileName);
    }
  });
};

const assertSealedCMO = (I, collectionId, hearingName) => {
  const sealedCMO = `Sealed Case Management Order ${collectionId}`;

  I.seeInTab([sealedCMO, 'Order'], 'mockFile.pdf');
  I.seeInTab([sealedCMO, 'Hearing'], hearingName);
  I.seeInTab([sealedCMO, 'Date issued'], dateFormat(today, 'd mmm yyyy'));
  I.seeInTab([sealedCMO, 'Judge'], 'Her Honour Judge Reed');
};

const assertDocumentSentToParties = I => {
  I.seeInTab(['Party 1', 'Representative name'], 'Marie Kelly');
  I.seeInTab(['Party 1', 'Document 1', 'File'], 'mockFile.pdf');
};
