const config = require('../config.js');
const representatives = require('../fixtures/representatives.js');
const c2Payment = require('../fixtures/c2Payment.js');
const expertReportLog = require('../fixtures/expertReportLog.js');
const supportingDocuments = require('../fixtures/c2SupportingDocuments.js');
const supplements = require('../fixtures/supplements.js');

const dateFormat = require('dateformat');
const mandatoryWithMultipleChildren = require('../fixtures/caseData/mandatoryWithMultipleChildren.json');
const api = require('../helpers/api_helper');
//const caseListPage = require('../pages/caseList.page');

let caseId;

Feature('Case administration after submission');

async function setupScenario(I) {
  if (!caseId) { caseId = await I.submitNewCaseWithData(mandatoryWithMultipleChildren); }
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
}

Scenario('HMCTS admin enters FamilyMan reference number', async ({I, caseViewPage, enterFamilyManCaseNumberEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.administrationActions.addFamilyManCaseNumber);
  await enterFamilyManCaseNumberEventPage.enterCaseID('mockCaseID');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.addFamilyManCaseNumber);
  I.seeFamilyManNumber('mockCaseID');
});

Scenario('HMCTS admin updates language requirement @nightlyOnly', async ({I, caseViewPage, enterLanguageRequirementsEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.administrationActions.languageRequirement);
  await enterLanguageRequirementsEventPage.enterLanguageRequirement();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.languageRequirement);
  caseViewPage.selectTab(caseViewPage.tabs.summary);
  I.seeTagInTab('WELSH CASE');
});

Scenario('HMCTS admin amends children, respondents, others, international element, other proceedings and attending hearing @nightlyOnly', async ({I, caseViewPage, enterOtherProceedingsEventPage, enterChildrenEventPage}) => {
  await setupScenario(I);
  const I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible = async (event, summary, description, I_doActionsOnEditPage = () => {}) => {
    await caseViewPage.goToNewActions(event);
    await I_doActionsOnEditPage();
    await I.completeEvent('Save and continue', {summary: summary, description: description});
    I.seeEventSubmissionConfirmation(event);
    I.see('Case information');
  };

  const summaryText = 'Summary of change';
  const descriptionText = 'Description of change';

  await I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible(config.administrationActions.amendChildren, summaryText, descriptionText, async () => {
    await I.goToNextPage();
    enterChildrenEventPage.selectAnyChildHasLegalRepresentation(enterChildrenEventPage.fields().mainSolicitor.childrenHaveLegalRepresentation.options.no);
  });

  await I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible(config.administrationActions.amendRespondents, summaryText, descriptionText);

  await I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible(config.administrationActions.amendOther, summaryText, descriptionText);

  await I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible(config.administrationActions.amendInternationalElement, summaryText, descriptionText);

  await I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible(config.administrationActions.amendOtherProceedings, summaryText, descriptionText, () => enterOtherProceedingsEventPage.selectNoForProceeding());

  await I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible(config.administrationActions.amendAttendingHearing, summaryText, descriptionText);
});

Scenario('HMCTS admin uploads additional applications to the case @nightlyOnly', async ({I, caseViewPage, uploadAdditionalApplicationsEventPage, paymentHistoryPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.administrationActions.uploadAdditionalApplications);
  uploadAdditionalApplicationsEventPage.selectAdditionalApplicationType('OTHER_ORDER');
  uploadAdditionalApplicationsEventPage.selectAdditionalApplicationType('C2_ORDER');
  uploadAdditionalApplicationsEventPage.selectC2Type('WITH_NOTICE');
  uploadAdditionalApplicationsEventPage.selectApplicantList('Someone else');
  uploadAdditionalApplicationsEventPage.enterOtherApplicantName('Jonathon Walker');
  await I.goToNextPage();
  uploadAdditionalApplicationsEventPage.uploadC2Document(config.testWordFile);
  uploadAdditionalApplicationsEventPage.selectC2AdditionalOrdersRequested('PARENTAL_RESPONSIBILITY');
  uploadAdditionalApplicationsEventPage.selectC2ParentalResponsibilityType('PR_BY_FATHER');
  await uploadAdditionalApplicationsEventPage.uploadC2Supplement(supplements);
  await uploadAdditionalApplicationsEventPage.uploadC2SupportingDocument(supportingDocuments);
  await I.goToNextPage();
  uploadAdditionalApplicationsEventPage.selectOtherApplication('C1 - Parental responsibility');
  uploadAdditionalApplicationsEventPage.selectOtherParentalResponsibilityType('PR_BY_FATHER');
  uploadAdditionalApplicationsEventPage.uploadDocument(config.testWordFile);
  await uploadAdditionalApplicationsEventPage.uploadOtherSupplement(supplements);
  await uploadAdditionalApplicationsEventPage.uploadOtherSupportingDocument(supportingDocuments);
  await I.goToNextPage();
  uploadAdditionalApplicationsEventPage.selectPeople(uploadAdditionalApplicationsEventPage.fields.allOthers.options.select, [0, 2]);
  await I.goToNextPage();
  const feeToPay = await uploadAdditionalApplicationsEventPage.getFeeToPay();
  uploadAdditionalApplicationsEventPage.usePbaPayment();
  uploadAdditionalApplicationsEventPage.enterPbaPaymentDetails(c2Payment);

  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.uploadAdditionalApplications);

  caseViewPage.selectTab(caseViewPage.tabs.paymentHistory);
  await paymentHistoryPage.checkPayment(feeToPay, c2Payment.pbaNumber);

  caseViewPage.selectTab(caseViewPage.tabs.otherApplications);

  I.seeInTab(['Additional applications 1', 'C2 application', 'File'], 'mockFile.pdf');
  I.seeInTab(['Additional applications 1', 'C2 application', 'Applicant'], 'Jonathon Walker');
  I.seeInTab(['Additional applications 1', 'C2 application', 'People notified'], 'Joe Bloggs, Noah King');
  I.seeInTab(['Additional applications 1', 'C2 application', 'Application type'], 'Application with notice.');
  I.seeTextInTab(['Additional applications 1', 'C2 application', 'Date and time of upload']);
  I.seeInTab(['Additional applications 1', 'C2 application', 'Uploaded by'], 'HMCTS');
  I.seeInTab(['Additional applications 1', 'C2 application', 'Supplements 1', 'Document name'], 'C20 - Secure accommodation');
  I.seeInTab(['Additional applications 1', 'C2 application', 'Supplements 1', 'Which jurisdiction?'], 'England');
  I.seeInTab(['Additional applications 1', 'C2 application', 'Supplements 1', 'Notes'], 'This is a note about supplement');
  I.seeTextInTab(['Additional applications 1', 'C2 application', 'Supplements 1', 'Date and time uploaded']);
  I.seeInTab(['Additional applications 1', 'C2 application', 'Supplements 1', 'Uploaded by'], 'HMCTS');
  I.seeInTab(['Additional applications 1', 'C2 application', 'Supplements 1', 'File'], 'mockFile.pdf');
  I.seeInTab(['Additional applications 1', 'C2 application', 'Supporting documents 1', 'Document name'], 'Supporting document');
  I.seeInTab(['Additional applications 1', 'C2 application', 'Supporting documents 1', 'Notes'], 'This is a note about supporting doc');
  I.seeTextInTab(['Additional applications 1', 'C2 application', 'Supporting documents 1', 'Date and time uploaded']);
  I.seeInTab(['Additional applications 1', 'C2 application', 'Supporting documents 1', 'Uploaded by'], 'HMCTS');
  I.seeInTab(['Additional applications 1', 'C2 application', 'Supporting documents 1', 'File'], 'mockFile.pdf');
  I.seeInTab(['Additional applications 1', 'C2 application', 'Additional orders requested'], 'Parental responsibility');
  I.seeInTab(['Additional applications 1', 'C2 application', 'Who\'s seeking parental responsibility?'], 'Parental responsibility by the father');

  I.seeInTab(['Additional applications 1', 'Other applications', 'File'], 'mockFile.pdf');
  I.seeInTab(['Additional applications 1', 'Other applications', 'Applicant'], 'Jonathon Walker');
  I.seeInTab(['Additional applications 1', 'Other applications', 'People notified'], 'Joe Bloggs, Noah King');
  I.seeInTab(['Additional applications 1', 'Other applications', 'Application type'], 'C1 - Parental responsibility');
  I.seeInTab(['Additional applications 1', 'Other applications', 'Who\'s seeking parental responsibility?'], 'Parental responsibility by the father');
  I.seeTextInTab(['Additional applications 1', 'Other applications', 'Date and time of upload']);
  I.seeInTab(['Additional applications 1', 'Other applications', 'Uploaded by'], 'HMCTS');
  I.seeInTab(['Additional applications 1', 'Other applications', 'Supplements 1', 'Document name'], 'C20 - Secure accommodation');
  I.seeInTab(['Additional applications 1', 'Other applications', 'Supplements 1', 'Which jurisdiction?'], 'England');
  I.seeInTab(['Additional applications 1', 'Other applications', 'Supplements 1', 'Notes'], 'This is a note about supplement');
  I.seeTextInTab(['Additional applications 1', 'Other applications', 'Supplements 1', 'Date and time uploaded']);
  I.seeInTab(['Additional applications 1', 'Other applications', 'Supplements 1', 'File'], 'mockFile.pdf');
  I.seeInTab(['Additional applications 1', 'Other applications', 'Supplements 1', 'Uploaded by'], 'HMCTS');
  I.seeInTab(['Additional applications 1', 'Other applications', 'Supporting documents 1', 'Document name'], 'Supporting document');
  I.seeInTab(['Additional applications 1', 'Other applications', 'Supporting documents 1', 'Notes'], 'This is a note about supporting doc');
  I.seeTextInTab(['Additional applications 1', 'Other applications', 'Supporting documents 1', 'Date and time uploaded']);
  I.seeInTab(['Additional applications 1', 'Other applications', 'Supporting documents 1', 'Uploaded by'], 'HMCTS');
  I.seeInTab(['Additional applications 1', 'Other applications', 'Supporting documents 1', 'File'], 'mockFile.pdf');

  I.seeInTab(['Additional applications 1', 'PBA Payment', 'Paid with PBA'], 'Yes');
  I.seeInTab(['Additional applications 1', 'PBA Payment', 'Payment by account (PBA) number'], c2Payment.pbaNumber);
  I.seeInTab(['Additional applications 1', 'PBA Payment', 'Client code'], c2Payment.clientCode);
  I.seeInTab(['Additional applications 1', 'PBA Payment', 'Customer reference'], c2Payment.customerReference);

  await api.pollLastEvent(caseId, config.internalActions.updateCase);
});

Scenario('HMCTS admin edits supporting evidence document on C2 application @nightlyOnly', async({I, caseViewPage, manageDocumentsEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.administrationActions.manageDocuments);
  await manageDocumentsEventPage.selectAdditionalApplicationsSupportingDocuments();
  await manageDocumentsEventPage.selectApplicationBundleFromDropdown(3);
  await I.goToNextPage();
  manageDocumentsEventPage.enterDocumentName('Updated document name');
  await I.completeEvent('Save and continue', {summary: 'Summary', description: 'Description'});
  I.seeEventSubmissionConfirmation(config.administrationActions.manageDocuments);
  caseViewPage.selectTab(caseViewPage.tabs.otherApplications);
  I.seeInTab(['Additional applications 1', 'C2 application', 'Supporting documents 1', 'Document name'], 'Updated document name');
  I.seeInTab(['Additional applications 1', 'C2 application', 'Supporting documents 1', 'Notes'], 'This is a note about supporting doc');
  I.seeTextInTab(['Additional applications 1', 'C2 application', 'Supporting documents 1', 'Date and time uploaded']);
  I.seeInTab(['Additional applications 1', 'C2 application', 'Supporting documents 1', 'Uploaded by'], 'HMCTS');
  I.seeInTab(['Additional applications 1', 'C2 application', 'Supporting documents 1', 'File'], 'mockFile.pdf');
});

Scenario('HMCTS admin edits supporting evidence document on Other application @nightlyOnly', async({I, caseViewPage, manageDocumentsEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.administrationActions.manageDocuments);
  await manageDocumentsEventPage.selectAdditionalApplicationsSupportingDocuments();
  await manageDocumentsEventPage.selectApplicationBundleFromDropdown(2);
  await I.goToNextPage();
  manageDocumentsEventPage.enterDocumentName('Updated document name');
  await I.completeEvent('Save and continue', {summary: 'Summary', description: 'Description'});
  I.seeEventSubmissionConfirmation(config.administrationActions.manageDocuments);
  caseViewPage.selectTab(caseViewPage.tabs.otherApplications);
  I.seeInTab(['Additional applications 1', 'Other applications', 'Supporting documents 1', 'Document name'], 'Updated document name');
  I.seeInTab(['Additional applications 1', 'Other applications', 'Supporting documents 1', 'Notes'], 'This is a note about supporting doc');
  I.seeTextInTab(['Additional applications 1', 'Other applications', 'Supporting documents 1', 'Date and time uploaded']);
  I.seeInTab(['Additional applications 1', 'Other applications', 'Supporting documents 1', 'Uploaded by'], 'HMCTS');
  I.seeInTab(['Additional applications 1', 'Other applications', 'Supporting documents 1', 'File'], 'mockFile.pdf');
});

Scenario('HMCTS admin share case with representatives @nightlyOnly', async ({I, caseViewPage, enterRepresentativesEventPage}) => {
  const representative1 = representatives.servedByDigitalService;
  const representative2 = representatives.servedByPost;

  await setupScenario(I);
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

Scenario('HMCTS admin revoke case access from representative @nightlyOnly', async ({I, caseViewPage, caseListPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.administrationActions.amendRepresentatives);

  await I.removeElementFromCollection('Representatives');

  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.amendRepresentatives);

  await I.signIn({email: representatives.servedByDigitalService.email, password: config.localAuthorityPassword});

  caseListPage.verifyCaseIsNotAccessible(caseId);
});

// Disabled as part of FPLA-1754 - TBD if super user will have access to notice of proceedings event
xScenario('HMCTS admin creates notice of proceedings documents', async (I, caseViewPage, createNoticeOfProceedingsEventPage) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.administrationActions.createNoticeOfProceedings);
  await createNoticeOfProceedingsEventPage.checkC6();
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
  await setupScenario(I);
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

Scenario('HMCTS admin handles supplementary evidence @nightlyOnly', async ({I, caseListPage, caseViewPage, handleSupplementaryEvidenceEventPage}) => {
  await setupScenario(I);
  caseListPage.navigate();
  await caseListPage.searchForCasesWithHandledEvidences(caseId);
  I.dontSeeCaseInSearchResult(caseId);

  await I.navigateToCaseDetails(caseId);
  await caseViewPage.goToNewActions(config.administrationActions.handleSupplementaryEvidence);
  await handleSupplementaryEvidenceEventPage.handleSupplementaryEvidence();
  await I.completeEvent('Save and continue');
  await I.seeEventSubmissionConfirmation(config.administrationActions.handleSupplementaryEvidence);

  caseListPage.navigate();
  await I.retryUntilExists(() => caseListPage.searchForCasesWithHandledEvidences(caseId), `a[href$='${caseId}']`, false);
  I.seeCaseInSearchResult(caseId);
}).retry(1); //Async case update in prev test

Scenario('HMCTS admin sends email to gatekeeper with a link to the case @nightlyOnly', async ({I, caseViewPage, sendCaseToGatekeeperEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.administrationActions.sendToGatekeeper);
  await sendCaseToGatekeeperEventPage.enterEmail();
  await I.addAnotherElementToCollection();
  await sendCaseToGatekeeperEventPage.enterEmail('cafcass+gatekeeping@gmail.com');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.sendToGatekeeper);
});

Scenario('HMCTS admin adds a note to the case @nightlyOnly', async ({I, caseViewPage, addNoteEventPage}) => {
  const note = 'Example note';
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.administrationActions.addNote);
  await addNoteEventPage.addNote(note);
  await I.completeEvent('Save and continue');
  await I.seeEventSubmissionConfirmation(config.administrationActions.addNote);
  caseViewPage.selectTab(caseViewPage.tabs.notes);
  I.seeInTab(['Note 1', 'Note'], note);
}).retry(1); // async processing in previous test

Scenario('HMCTS admin adds expert report log @nightlyOnly', async ({I, caseViewPage, addExpertReportEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.administrationActions.addExpertReportLog);
  await addExpertReportEventPage.addExpertReportLog(expertReportLog[0]);
  await I.completeEvent('Save and continue');
  await I.seeEventSubmissionConfirmation(config.administrationActions.addExpertReportLog);
  caseViewPage.selectTab(caseViewPage.tabs.expertReports);
  I.seeInTab(['Report 1', 'What type of report have you requested?'], 'Pediatric');
  I.seeInTab(['Report 1', 'Date requested'], '1 Mar 2020');
  I.seeInTab(['Report 1', 'Has it been approved?'], 'Yes');
  I.seeInTab(['Report 1', 'Date approved'], '2 Apr 2020');
}).retry(1);

Scenario('HMCTS admin makes 26-week case extension @nightlyOnly', async ({I, caseViewPage, addExtend26WeekTimelineEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.applicationActions.extend26WeekTimeline);
  await addExtend26WeekTimelineEventPage.selectExtendsForAllChildren();
  await I.goToNextPage();
  await addExtend26WeekTimelineEventPage.selectSameExtensionForAllChildren();
  await addExtend26WeekTimelineEventPage.selectEightWeekExtensionTime();
  addExtend26WeekTimelineEventPage.selectTimetableForChildExtensionReason();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.extend26WeekTimeline);
  caseViewPage.selectTab(caseViewPage.tabs.summary);
  I.seeInTab('Date of issue', dateFormat(Date.now(), 'd mmm yyyy'));
  I.seeInTab('26-week timeline date', dateFormat(new Date().setDate(new Date().getDate() + 26 * 7), 'd mmm yyyy'));

  let extendedDate = dateFormat(new Date().setDate(new Date().getDate() + 26 * 7 + 8 * 7), 'd mmm yyyy');
  I.seeInTab('Extended timeline date', extendedDate);
  I.assertValueInTab('Extended timeline:',
    'Timothy Jones - ' + extendedDate + ' - Timetable for child\n' +
    'John Black - ' + extendedDate + ' - Timetable for child\n' +
    'William Black - ' + extendedDate + ' - Timetable for child\n' +
    'Sarah Black - ' + extendedDate + ' - Timetable for child');
}).retry(0);

Scenario('HMCTS admin closes the case @nightlyOnly', async ({I, caseViewPage, recordFinalDecisionsEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.administrationActions.recordFinalDecisions);
  await recordFinalDecisionsEventPage.selectChildren(recordFinalDecisionsEventPage.section1.allChildren.options.all, [0]);
  await I.goToNextPage();
  await recordFinalDecisionsEventPage.addDate({day: 12, month: 3, year: 2020});
  recordFinalDecisionsEventPage.selectReason(recordFinalDecisionsEventPage.section2.reason.withdrawn, [0,1]);
  recordFinalDecisionsEventPage.selectReason(recordFinalDecisionsEventPage.section2.reason.refusal, [2]);
  recordFinalDecisionsEventPage.selectReason(recordFinalDecisionsEventPage.section2.reason.noOrder, [3]);
  await I.completeEvent('Submit');
  I.seeEventSubmissionConfirmation(config.administrationActions.recordFinalDecisions);
  caseViewPage.selectTab(caseViewPage.tabs.summary);
  I.seeInTab(['Close the case', 'Date'], '12 Mar 2020');
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeInTab(['Child 1', 'Resolution Reason'], 'Application withdrawn');
  I.seeInTab(['Child 1', 'Resolution Date'], '12 March 2020');
  I.seeInTab(['Child 2', 'Resolution Reason'], 'Application withdrawn');
  I.seeInTab(['Child 2', 'Resolution Date'], '12 March 2020');
  I.seeInTab(['Child 3', 'Resolution Reason'], 'Application refused');
  I.seeInTab(['Child 3', 'Resolution Date'], '12 March 2020');
  I.seeInTab(['Child 4', 'Resolution Reason'], 'No order made');
  I.seeInTab(['Child 4', 'Resolution Date'], '12 March 2020');
}).retry(1);
