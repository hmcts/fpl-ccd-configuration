const config = require('../config.js');
const blankOrder = require('../fixtures/orders/blankOrder.js');
const interimSuperVisionOrder = require('../fixtures/orders/interimSupervision.js');
const finalSuperVisionOrder = require('../fixtures/orders/finalSupervisionOrder.js');
const emergencyProtectionOrder = require('../fixtures/orders/emergencyProtectionOrder.js');
const uploadedOrder = require('../fixtures/orders/uploadedOrder.js');
const interimCareOrder = require('../fixtures/orders/interimCareOrder.js');
const finalCareOrder = require('../fixtures/orders/finalCareOrder.js');
const dischargeOfCareOrder = require('../fixtures/orders/dischargeOfCareOrder.js');
const orderFunctions = require('../helpers/generated_order_helper');
const representatives = require('../fixtures/representatives.js');
const c2Payment = require('../fixtures/c2Payment.js');
const expertReportLog = require('../fixtures/expertReportLog.js');
const dateFormat = require('dateformat');
const mandatoryWithMultipleChildren = require('../fixtures/caseData/mandatoryWithMultipleChildren.json');
const supportingEvidenceDocuments = require('../fixtures/supportingEvidenceDocuments.js');

let caseId;
let submittedAt;

Feature('Case administration after submission');

BeforeSuite(async ({I}) => {
  caseId = await I.submitNewCaseWithData(mandatoryWithMultipleChildren);
  submittedAt = new Date();

  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
});

Before(async ({I}) => await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId));

Scenario('HMCTS admin enters FamilyMan reference number', async ({I, caseViewPage, enterFamilyManCaseNumberEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.addFamilyManCaseNumber);
  enterFamilyManCaseNumberEventPage.enterCaseID('mockCaseID');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.addFamilyManCaseNumber);
  I.seeFamilyManNumber('mockCaseID');
});

Scenario('HMCTS admin amends children, respondents, others, international element, other proceedings and attending hearing', async ({I, caseViewPage, enterOtherProceedingsEventPage}) => {
  const I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible = async (event, summary, description, I_doActionsOnEditPage = () => {}) => {
    await caseViewPage.goToNewActions(event);
    I_doActionsOnEditPage();
    await I.completeEvent('Save and continue', {summary: summary, description: description});
    I.seeEventSubmissionConfirmation(event);
    I.see(summary);
    I.see(description);
  };

  const summaryText = 'Summary of change';
  const descriptionText = 'Description of change';

  await I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible(config.administrationActions.amendChildren, summaryText, descriptionText);

  await I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible(config.administrationActions.amendRespondents, summaryText, descriptionText);

  await I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible(config.administrationActions.amendOther, summaryText, descriptionText);

  await I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible(config.administrationActions.amendInternationalElement, summaryText, descriptionText);

  await I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible(config.administrationActions.amendOtherProceedings, summaryText, descriptionText, () => enterOtherProceedingsEventPage.selectNoForProceeding());

  await I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible(config.administrationActions.amendAttendingHearing, summaryText, descriptionText);
});

Scenario('HMCTS admin uploads correspondence documents', async ({I, caseViewPage, manageDocumentsEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.manageDocuments);
  manageDocumentsEventPage.selectCorrespondence();
  await I.goToNextPage();
  await manageDocumentsEventPage.uploadSupportingEvidenceDocument(supportingEvidenceDocuments[0]);
  await I.addAnotherElementToCollection();
  await manageDocumentsEventPage.uploadSupportingEvidenceDocument(supportingEvidenceDocuments[1]);
  await I.completeEvent('Save and continue', {summary: 'Summary', description: 'Description'});
  I.seeEventSubmissionConfirmation(config.administrationActions.manageDocuments);
  caseViewPage.selectTab(caseViewPage.tabs.correspondence);
  I.seeInTab(['Correspondence uploaded by HMCTS 1', 'Document name'], 'Email to say evidence will be late');
  I.seeInTab(['Correspondence uploaded by HMCTS 1', 'Notes'], 'Evidence will be late');
  I.seeInTab(['Correspondence uploaded by HMCTS 1', 'Date and time received'], '1 Jan 2020, 11:00:00 AM');
  I.seeInTab(['Correspondence uploaded by HMCTS 1', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeTextInTab(['Correspondence uploaded by HMCTS 1', 'Uploaded by']);
  I.seeInTab(['Correspondence uploaded by HMCTS 1', 'File'], 'mockFile.txt');
  I.seeInTab(['Correspondence uploaded by HMCTS 2', 'Document name'], 'Email with evidence attached');
  I.seeInTab(['Correspondence uploaded by HMCTS 2', 'Notes'], 'Case evidence included');
  I.seeInTab(['Correspondence uploaded by HMCTS 2', 'Date and time received'], '1 Jan 2020, 11:00:00 AM');
  I.seeInTab(['Correspondence uploaded by HMCTS 2', 'Date and time uploaded'], dateFormat(submittedAt, 'd mmm yyyy'));
  I.seeTextInTab(['Correspondence uploaded by HMCTS 2', 'Uploaded by']);
  I.seeInTab(['Correspondence uploaded by HMCTS 2', 'File'], 'mockFile.txt');
});

Scenario('HMCTS admin uploads C2 documents to the case', async ({I, caseViewPage, uploadC2DocumentsEventPage, paymentHistoryPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.uploadC2Documents);
  uploadC2DocumentsEventPage.selectApplicationType('WITH_NOTICE');
  await I.goToNextPage();
  const feeToPay = await uploadC2DocumentsEventPage.getFeeToPay();
  uploadC2DocumentsEventPage.usePbaPayment();
  uploadC2DocumentsEventPage.enterPbaPaymentDetails(c2Payment);
  uploadC2DocumentsEventPage.uploadC2Document(config.testFile, 'Rachel Zane C2');
  await uploadC2DocumentsEventPage.uploadC2SupportingDocument();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.uploadC2Documents);

  caseViewPage.selectTab(caseViewPage.tabs.paymentHistory);
  paymentHistoryPage.checkPayment(feeToPay, c2Payment.pbaNumber);

  caseViewPage.selectTab(caseViewPage.tabs.c2);
  I.seeInTab(['C2 Application 1', 'File'], 'mockFile.txt');
  I.seeInTab(['C2 Application 1', 'Notes'], 'Rachel Zane C2');
  I.seeInTab(['C2 Application 1', 'Paid with PBA'], 'Yes');
  I.seeInTab(['C2 Application 1', 'Payment by account (PBA) number'], c2Payment.pbaNumber);
  I.seeInTab(['C2 Application 1', 'Client code'], c2Payment.clientCode);
  I.seeInTab(['C2 Application 1', 'Customer reference'], c2Payment.customerReference);
  I.seeInTab(['C2 Application 1', 'Document name'], 'C2 supporting document');
  I.seeInTab(['C2 Application 1', 'Notes'], 'C2 supporting document');
  I.seeInTab(['C2 Application 1', 'Date and time received'], '1 Jan 2020, 11:00:00 AM');
  I.seeTextInTab(['C2 Application 1', 'Date and time uploaded']);
  I.seeTextInTab(['C2 Application 1', 'Uploaded by']);
  I.seeInTab(['C2 Application 1', 'Document name'], 'This is a note about supporting doc');
  I.seeInTab(['C2 Application 1', 'File'], 'mockFile.txt');

  await I.startEventViaHyperlink('Upload a new C2 application');

  uploadC2DocumentsEventPage.selectApplicationType('WITHOUT_NOTICE');
  await I.goToNextPage();
  uploadC2DocumentsEventPage.usePbaPayment(false);
  uploadC2DocumentsEventPage.uploadC2Document(config.testFile, 'Jessica Pearson C2');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.uploadC2Documents);
  caseViewPage.selectTab(caseViewPage.tabs.c2);
  I.seeInTab(['C2 Application 2', 'File'], 'mockFile.txt');
  I.seeInTab(['C2 Application 2', 'Notes'], 'Jessica Pearson C2');
  I.seeInTab(['C2 Application 2', 'Paid with PBA'], 'No');
});

Scenario('HMCTS admin edits supporting evidence document on C2 application', async({I, caseViewPage, manageDocumentsEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.manageDocuments);
  manageDocumentsEventPage.selectC2SupportingDocuments();
  await manageDocumentsEventPage.selectC2FromDropdown();
  await I.goToNextPage();
  manageDocumentsEventPage.enterDocumentName('Updated document name');
  await I.completeEvent('Save and continue', {summary: 'Summary', description: 'Description'});
  I.seeEventSubmissionConfirmation(config.administrationActions.manageDocuments);
  caseViewPage.selectTab(caseViewPage.tabs.c2);
  I.seeInTab(['C2 Application 1', 'Document name'], 'Updated document name');
  I.seeInTab(['C2 Application 1', 'Notes'], 'C2 supporting document');
  I.seeInTab(['C2 Application 1', 'Date and time received'], '1 Jan 2020, 11:00:00 AM');
  I.seeInTab(['C2 Application 1', 'Document name'], 'This is a note about supporting doc');
  I.seeInTab(['C2 Application 1', 'File'], 'mockFile.txt');
  I.seeTextInTab(['C2 Application 1', 'Date and time uploaded']);
  I.seeTextInTab(['C2 Application 1', 'Uploaded by']);
});

Scenario('HMCTS admin share case with representatives', async ({I, caseViewPage, enterRepresentativesEventPage}) => {
  const representative1 = representatives.servedByDigitalService;
  const representative2 = representatives.servedByPost;

  await caseViewPage.goToNewActions(config.administrationActions.amendRepresentatives);

  await enterRepresentativesEventPage.enterRepresentative(representative1);
  await I.addAnotherElementToCollection('Representatives');
  await enterRepresentativesEventPage.enterRepresentative(representative2);

  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.amendRepresentatives);

  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeInTab(['Representatives 1', 'Full name'], representative1.fullName);
  I.seeInTab(['Representatives 1', 'Position in a case'], representative1.positionInACase);
  I.seeInTab(['Representatives 1', 'Email address'], representative1.email);
  I.seeInTab(['Representatives 1', 'Phone number'], representative1.telephone);
  I.seeInTab(['Representatives 1', 'How do they want to get case information?'], representative1.servingPreferences);
  I.seeInTab(['Representatives 1', 'Who are they?'], representative1.role);
  I.seeInTab(['Representatives 1', 'Email address'], representative1.email);
  I.seeInTab(['Representatives 2', 'Phone number'], representative2.telephone);

  I.seeInTab(['Representatives 2', 'Full name'], representative2.fullName);
  I.seeInTab(['Representatives 2', 'Position in a case'], representative2.positionInACase);
  I.seeInTab(['Representatives 2', 'How do they want to get case information?'], representative2.servingPreferences);
  I.seeInTab(['Representatives 2', 'Who are they?'], representative2.role);

  await I.navigateToCaseDetailsAs({email: representative1.email, password: config.localAuthorityPassword}, caseId);
  I.see(caseId);
});

Scenario('HMCTS admin revoke case access from representative', async ({I, caseViewPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.amendRepresentatives);

  await I.removeElementFromCollection('Representatives');

  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.amendRepresentatives);

  await I.navigateToCaseDetailsAs({email: representatives.servedByDigitalService.email, password: config.localAuthorityPassword}, caseId);

  I.see('No cases found.');
});

Scenario('HMCTS admin creates blank order', async ({I, caseViewPage, createOrderEventPage}) => {
  await verifyOrderCreation(I, caseViewPage, createOrderEventPage, blankOrder);
});

Scenario('HMCTS admin creates interim supervision order', async ({I, caseViewPage, createOrderEventPage}) => {
  await verifyOrderCreation(I, caseViewPage, createOrderEventPage, interimSuperVisionOrder);
});

Scenario('HMCTS admin creates final supervision order', async ({I, caseViewPage, createOrderEventPage}) => {
  await verifyOrderCreation(I, caseViewPage, createOrderEventPage, finalSuperVisionOrder);
});

Scenario('HMCTS admin creates emergency protection order', async ({I, caseViewPage, createOrderEventPage}) => {
  await verifyOrderCreation(I, caseViewPage, createOrderEventPage, emergencyProtectionOrder);
});

Scenario('HMCTS admin creates interim care order', async ({I, caseViewPage, createOrderEventPage}) => {
  await verifyOrderCreation(I, caseViewPage, createOrderEventPage, interimCareOrder);
});

Scenario('HMCTS admin uploads order', async ({I, caseViewPage, createOrderEventPage}) => {
  await verifyOrderCreation(I, caseViewPage, createOrderEventPage, uploadedOrder);
});

Scenario('HMCTS admin creates final care order', async ({I, caseViewPage, createOrderEventPage}) => {
  await verifyOrderCreation(I, caseViewPage, createOrderEventPage, finalCareOrder);
});

Scenario('HMCTS admin creates discharge of care order', async ({I, caseViewPage, createOrderEventPage}) => {
  await verifyOrderCreation(I, caseViewPage, createOrderEventPage, dischargeOfCareOrder);
});

// Disabled as part of FPLA-1754 - TBD if super user will have access to notice of proceedings event
xScenario('HMCTS admin creates notice of proceedings documents', async (I, caseViewPage, createNoticeOfProceedingsEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.createNoticeOfProceedings);
  createNoticeOfProceedingsEventPage.checkC6();
  createNoticeOfProceedingsEventPage.checkC6A();
  createNoticeOfProceedingsEventPage.useAlternateJudge();
  createNoticeOfProceedingsEventPage.selectJudgeTitle();
  createNoticeOfProceedingsEventPage.enterJudgeLastName('Sarah Simpson');
  createNoticeOfProceedingsEventPage.enterJudgeEmailAddress('test@test.com');
  createNoticeOfProceedingsEventPage.enterLegalAdvisorName('Ian Watson');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.createNoticeOfProceedings);
  caseViewPage.selectTab(caseViewPage.tabs.hearings);
  I.seeInTab(['Notice of proceedings 1', 'File name'], 'Notice_of_proceedings_c6.pdf');
  I.seeInTab(['Notice of proceedings 2', 'File name'], 'Notice_of_proceedings_c6a.pdf');
});

// Disabled as part of FPLA-1754 - TBD if super user will have access to notice of proceedings event
xScenario('HMCTS admin creates notice of proceedings documents with allocated judge', async (I, caseViewPage, createNoticeOfProceedingsEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.createNoticeOfProceedings);
  await createNoticeOfProceedingsEventPage.checkC6();
  await createNoticeOfProceedingsEventPage.useAllocatedJudge();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.createNoticeOfProceedings);
  await caseViewPage.goToNewActions(config.administrationActions.createNoticeOfProceedings);
  await createNoticeOfProceedingsEventPage.checkC6A();
  await createNoticeOfProceedingsEventPage.useAlternateJudge();
  await createNoticeOfProceedingsEventPage.selectJudgeTitle();
  await createNoticeOfProceedingsEventPage.enterJudgeLastName('Sarah Simpson');
  await createNoticeOfProceedingsEventPage.enterJudgeEmailAddress('test@test.com');
  await createNoticeOfProceedingsEventPage.enterLegalAdvisorName('Ian Watson');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.createNoticeOfProceedings);
  caseViewPage.selectTab(caseViewPage.tabs.hearings);
  I.seeInTab(['Notice of proceedings 1', 'File name'], 'Notice_of_proceedings_c6a.pdf');
  I.seeInTab(['Notice of proceedings 2', 'File name'], 'Notice_of_proceedings_c6.pdf');
});

Scenario('HMCTS admin handles supplementary evidence', async ({I, caseListPage, caseViewPage, handleSupplementaryEvidenceEventPage}) => {
  I.navigateToCaseList();
  await caseListPage.searchForCasesWithHandledEvidences(submittedAt);
  I.dontSeeCaseInSearchResult(caseId);

  await I.navigateToCaseDetails(caseId);
  await caseViewPage.goToNewActions(config.administrationActions.handleSupplementaryEvidence);
  handleSupplementaryEvidenceEventPage.handleSupplementaryEvidence();
  await I.completeEvent('Save and continue');
  await I.seeEventSubmissionConfirmation(config.administrationActions.handleSupplementaryEvidence);

  I.navigateToCaseList();
  await I.retryUntilExists(async () => await caseListPage.searchForCasesWithHandledEvidences(submittedAt), caseListPage.locateCase(caseId));
  I.seeCaseInSearchResult(caseId);
});

Scenario('HMCTS admin sends email to gatekeeper with a link to the case', async ({I, caseViewPage, sendCaseToGatekeeperEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.sendToGatekeeper);
  await sendCaseToGatekeeperEventPage.enterEmail();
  await I.addAnotherElementToCollection();
  await sendCaseToGatekeeperEventPage.enterEmail('cafcass+gatekeeping@gmail.com');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.sendToGatekeeper);
});

Scenario('HMCTS admin adds a note to the case', async ({I, caseViewPage, addNoteEventPage}) => {
  const note = 'Example note';
  await caseViewPage.goToNewActions(config.administrationActions.addNote);
  addNoteEventPage.addNote(note);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.addNote);
  caseViewPage.selectTab(caseViewPage.tabs.notes);
  I.seeInTab(['Note 1', 'Note'], note);
});

Scenario('HMCTS admin adds expert report log', async ({I, caseViewPage, addExpertReportEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.addExpertReportLog);
  addExpertReportEventPage.addExpertReportLog(expertReportLog[0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.addExpertReportLog);
  caseViewPage.selectTab(caseViewPage.tabs.expertReports);
  I.seeInTab(['Report 1', 'What type of report have you requested?'], 'Pediatric');
  I.seeInTab(['Report 1', 'Date requested'], '1 Mar 2020');
  I.seeInTab(['Report 1', 'Has it been approved?'], 'Yes');
  I.seeInTab(['Report 1', 'Date approved'], '2 Apr 2020');
});

Scenario('HMCTS admin makes 26-week case extension', async ({I, caseViewPage, addExtend26WeekTimelineEventPage}) => {
  await caseViewPage.goToNewActions(config.applicationActions.extend26WeekTimeline);
  addExtend26WeekTimelineEventPage.selectEightWeekExtensionTime();
  addExtend26WeekTimelineEventPage.selectTimetableForChildExtensionReason();
  addExtend26WeekTimelineEventPage.addExtensionComment('Comment');
  await I.goToNextPage();
  addExtend26WeekTimelineEventPage.addCaseExtensionTimeConfirmation();
  addExtend26WeekTimelineEventPage.addCaseExtensionDate();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.extend26WeekTimeline);
  caseViewPage.selectTab(caseViewPage.tabs.overview);
  I.seeInTab('26-week timeline date', '10 Oct 2030');
  I.seeInTab('Why is this case being extended?', 'Timetable for child');
  I.seeInTab('Add comments', 'Comment');
});

Scenario('HMCTS admin closes the case', async ({I, caseViewPage, closeTheCaseEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.closeTheCase);
  await closeTheCaseEventPage.closeCase({day: 12, month: 3, year: 2020}, closeTheCaseEventPage.fields.reasons.deprivation);
  await I.completeEvent('Submit');
  I.seeEventSubmissionConfirmation(config.administrationActions.closeTheCase);
  caseViewPage.selectTab(caseViewPage.tabs.overview);
  I.seeInTab(['Close the case', 'Date'], '12 Mar 2020');
  I.seeInTab(['Close the case', 'Reason'], 'Deprivation of liberty');
});

const verifyOrderCreation = async (I, caseViewPage, createOrderEventPage, order) => {
  await caseViewPage.goToNewActions(config.administrationActions.createOrder);
  const defaultIssuedDate = new Date();
  await orderFunctions.createOrder(I, createOrderEventPage, order);
  I.seeEventSubmissionConfirmation(config.administrationActions.createOrder);
  await orderFunctions.assertOrder(I, caseViewPage, order, defaultIssuedDate);
  await orderFunctions.assertOrderSentToParty(I, caseViewPage, representatives.servedByPost.fullName, order);
};
