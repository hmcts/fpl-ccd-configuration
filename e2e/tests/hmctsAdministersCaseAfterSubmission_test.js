const config = require('../config.js');
const hearingDetails = require('../fixtures/hearingTypeDetails.js');
const orders = require('../fixtures/orders.js');
const orderFunctions = require('../helpers/generated_order_helper');
const representatives = require('../fixtures/representatives.js');
const c2Payment = require('../fixtures/c2Payment.js');
const expertReportLog = require('../fixtures/expertReportLog.js');
const dateFormat = require('dateformat');
const dateToString = require('../helpers/date_to_string_helper');

let caseId;
let submittedAt;

Feature('Case administration after submission');

Before(async (I, caseViewPage, submitApplicationEventPage) => {

  if (!caseId) {
    await I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
    await I.enterMandatoryFields({multipleChildren: true});
    await caseViewPage.goToNewActions(config.applicationActions.submitCase);
    submitApplicationEventPage.giveConsent();
    await I.completeEvent('Submit');

    // eslint-disable-next-line require-atomic-updates
    caseId = await I.grabTextFrom('.heading-h1');
    submittedAt = new Date();
    console.log(`Case ${caseId} has been submitted`);

    I.signOut();
  }
  await I.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);

  await I.navigateToCaseDetails(caseId);
});

Scenario('HMCTS admin confirms payment', async (I, caseViewPage) => {
  caseViewPage.selectTab(caseViewPage.tabs.paymentHistory);
  I.see('Processed payments'); // Test to pass AAT, to make better
});

Scenario('HMCTS admin enters FamilyMan reference number', async (I, caseViewPage, loginPage, enterFamilyManCaseNumberEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.addFamilyManCaseNumber);
  enterFamilyManCaseNumberEventPage.enterCaseID();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.addFamilyManCaseNumber);
});

Scenario('HMCTS admin amends children, respondents, others, international element, other proceedings and attending hearing', async (I, caseViewPage, loginPage, enterFamilyManCaseNumberEventPage, enterOtherProceedingsEventPage) => {
  async function I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible(event, summary, description, I_doActionsOnEditPage = () => {
  }) {
    await caseViewPage.goToNewActions(event);
    I_doActionsOnEditPage();
    await I.completeEvent('Save and continue', {summary: summary, description: description});
    I.seeEventSubmissionConfirmation(event);
    I.see(summary);
    I.see(description);
  }

  const summaryText = 'Summary of change';
  const descriptionText = 'Description of change';

  await I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible(config.administrationActions.amendChildren,
    summaryText, descriptionText);

  await I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible(config.administrationActions.amendRespondents,
    summaryText, descriptionText);

  await I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible(config.administrationActions.amendOther,
    summaryText, descriptionText);

  await I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible(config.administrationActions.amendInternationalElement,
    summaryText, descriptionText);

  await I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible(config.administrationActions.amendOtherProceedings,
    summaryText, descriptionText, () => enterOtherProceedingsEventPage.selectNoForProceeding());

  await I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible(config.administrationActions.amendAttendingHearing,
    summaryText, descriptionText);
});

Scenario('HMCTS admin uploads standard directions with other documents', async (I, caseViewPage, uploadStandardDirectionsDocumentEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
  uploadStandardDirectionsDocumentEventPage.uploadStandardDirections(config.testFile);
  uploadStandardDirectionsDocumentEventPage.uploadAdditionalDocuments(config.testFile);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.see('mockFile.txt');
  I.seeInTab(['Other documents 1', 'Document name'], 'Document 1');
  I.seeInTab(['Other documents 1', 'Upload a file'], 'mockFile.txt');
  I.seeInTab(['Other documents 2', 'Document name'], 'Document 2');
  I.seeInTab(['Other documents 2', 'Upload a file'], 'mockFile.txt');
});

Scenario('HMCTS admin uploads C2 documents to the case', async (I, caseViewPage, uploadC2DocumentsEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.uploadC2Documents);
  uploadC2DocumentsEventPage.selectApplicationType('WITH_NOTICE');
  await I.retryUntilExists(() => I.click('Continue'), '#temporaryC2Document_document');
  uploadC2DocumentsEventPage.usePbaPayment();
  uploadC2DocumentsEventPage.enterPbaPaymentDetails(c2Payment);
  uploadC2DocumentsEventPage.uploadC2Document(config.testFile, 'Rachel Zane C2');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.uploadC2Documents);
  await caseViewPage.goToNewActions(config.administrationActions.uploadC2Documents);
  uploadC2DocumentsEventPage.selectApplicationType('WITHOUT_NOTICE');
  await I.retryUntilExists(() => I.click('Continue'), '#temporaryC2Document_document');
  uploadC2DocumentsEventPage.usePbaPayment(false);
  uploadC2DocumentsEventPage.uploadC2Document(config.testFile, 'Jessica Pearson C2');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.uploadC2Documents);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.seeInTab(['C2 Application 1', 'Upload a file'], 'mockFile.txt');
  I.seeInTab(['C2 Application 1', 'Description'], 'Rachel Zane C2');
  I.seeInTab(['C2 Application 1', 'Are you using PBA to pay?'], 'Yes');
  I.seeInTab(['C2 Application 1', 'Payment by account (PBA) number'], 'PBA0082848');
  I.seeInTab(['C2 Application 1', 'Client code'], '8888');
  I.seeInTab(['C2 Application 1', 'Customer reference'], 'Example reference');
  I.seeInTab(['C2 Application 2', 'Upload a file'], 'mockFile.txt');
  I.seeInTab(['C2 Application 2', 'Description'], 'Jessica Pearson C2');
  I.seeInTab(['C2 Application 2', 'Are you using PBA to pay?'], 'No');
});

Scenario('HMCTS admin enters hearing details and submits', async (I, caseViewPage, loginPage, addHearingBookingDetailsEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.addHearingBookingDetails);
  await addHearingBookingDetailsEventPage.enterHearingDetails(hearingDetails[0]);
  await I.addAnotherElementToCollection();
  await addHearingBookingDetailsEventPage.enterHearingDetails(hearingDetails[1]);
  await I.completeEvent('Save and continue', {summary: 'summary', description: 'description'});
  I.seeEventSubmissionConfirmation(config.administrationActions.addHearingBookingDetails);
  caseViewPage.selectTab(caseViewPage.tabs.hearings);

  let startDate = dateToString(hearingDetails[0].startDate);
  let endDate = dateToString(hearingDetails[0].endDate);
  I.seeInTab(['Hearing 1', 'Type of hearing'], hearingDetails[0].caseManagement);
  I.seeInTab(['Hearing 1', 'Venue'], hearingDetails[0].venue);
  I.seeInTab(['Hearing 1', 'Start date and time'], dateFormat(startDate, 'd mmm yyyy, h:MM:ss TT'));
  I.seeInTab(['Hearing 1', 'End date and time'], dateFormat(endDate, 'd mmm yyyy, h:MM:ss TT'));
  I.seeInTab(['Hearing 1', 'Hearing needs booked'], [hearingDetails[0].type.interpreter, hearingDetails[0].type.welsh,hearingDetails[0].type.somethingElse]);
  I.seeInTab(['Hearing 1', 'Give details'], hearingDetails[0].giveDetails);
  I.seeInTab(['Hearing 1', 'Judge and Justices\' Legal Adviser', 'Judge or magistrate\'s title'], hearingDetails[0].judgeAndLegalAdvisor.judgeTitle);
  I.seeInTab(['Hearing 1', 'Judge and Justices\' Legal Adviser', 'Last name'], hearingDetails[0].judgeAndLegalAdvisor.judgeLastName);
  I.seeInTab(['Hearing 1', 'Judge and Justices\' Legal Adviser', 'Justices\' Legal Adviser\'s full name'], hearingDetails[0].judgeAndLegalAdvisor.legalAdvisorName);

  startDate = dateToString(hearingDetails[1].startDate);
  endDate = dateToString(hearingDetails[1].endDate);
  I.seeInTab(['Hearing 2', 'Type of hearing'], hearingDetails[1].caseManagement);
  I.seeInTab(['Hearing 2', 'Venue'], hearingDetails[1].venue);
  I.seeInTab(['Hearing 2', 'Venue address', 'Building and Street'], hearingDetails[1].venueCustomAddress.buildingAndStreet.lineOne);
  I.seeInTab(['Hearing 2', 'Venue address', 'Address Line 2'], hearingDetails[1].venueCustomAddress.buildingAndStreet.lineTwo);
  I.seeInTab(['Hearing 2', 'Venue address', 'Address Line 3'], hearingDetails[1].venueCustomAddress.buildingAndStreet.lineThree);
  I.seeInTab(['Hearing 2', 'Venue address', 'Town or City'], hearingDetails[1].venueCustomAddress.town);
  I.seeInTab(['Hearing 2', 'Venue address', 'Postcode/Zipcode'], hearingDetails[1].venueCustomAddress.postcode);
  I.seeInTab(['Hearing 2', 'Venue address', 'Country'], hearingDetails[1].venueCustomAddress.country);

  I.seeInTab(['Hearing 2', 'Start date and time'], dateFormat(startDate, 'd mmm yyyy, h:MM:ss TT'));
  I.seeInTab(['Hearing 2', 'End date and time'], dateFormat(endDate, 'd mmm yyyy, h:MM:ss TT'));
  I.seeInTab(['Hearing 2', 'Hearing needs booked'], [hearingDetails[1].type.interpreter, hearingDetails[1].type.welsh, hearingDetails[1].type.somethingElse]);
  I.seeInTab(['Hearing 2', 'Give details'], hearingDetails[1].giveDetails);
  I.seeInTab(['Hearing 2', 'Judge and Justices\' Legal Adviser', 'Judge or magistrate\'s title'], hearingDetails[1].judgeAndLegalAdvisor.judgeTitle);
  I.seeInTab(['Hearing 2', 'Judge and Justices\' Legal Adviser', 'Title'], hearingDetails[1].judgeAndLegalAdvisor.otherTitle);
  I.seeInTab(['Hearing 2', 'Judge and Justices\' Legal Adviser', 'Last name'], hearingDetails[1].judgeAndLegalAdvisor.judgeLastName);
  I.seeInTab(['Hearing 2', 'Judge and Justices\' Legal Adviser', 'Justices\' Legal Adviser\'s full name'], hearingDetails[1].judgeAndLegalAdvisor.legalAdvisorName);
});

Scenario('HMCTS admin share case with representatives', async (I, caseViewPage, enterRepresentativesEventPage) => {
  await I.navigateToCaseDetails(caseId);
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

  I.signOut();
  await I.signIn(representative1.email, config.localAuthorityPassword);
  await I.navigateToCaseDetails(caseId);
  I.see(caseId);
  I.signOut();
  await I.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
});

Scenario('HMCTS admin revoke case access from representative', async (I, caseViewPage) => {
  await I.navigateToCaseDetails(caseId);
  await caseViewPage.goToNewActions(config.administrationActions.amendRepresentatives);

  await I.removeElementFromCollection('Representatives');

  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.amendRepresentatives);

  I.signOut();
  await I.signIn(representatives.servedByDigitalService.email, config.localAuthorityPassword);
  await I.navigateToCaseDetails(caseId);
  I.seeInCurrentUrl('error');

  I.signOut();
  await I.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
});

Scenario('HMCTS admin creates multiple orders for the case', async (I, caseViewPage, createOrderEventPage) => {
  for (let i = 0; i < orders.length; i++) {
    const order = orders[i];
    await caseViewPage.goToNewActions(config.administrationActions.createOrder);
    const defaultIssuedDate = new Date();
    await orderFunctions.createOrder(I, createOrderEventPage, order);
    I.seeEventSubmissionConfirmation(config.administrationActions.createOrder);
    await orderFunctions.assertOrder(I, caseViewPage, order, i + 1, defaultIssuedDate);
    await orderFunctions.assertOrderSentToParty(I, caseViewPage, representatives.servedByPost.fullName, order, i + 1);
  }
});

Scenario('HMCTS admin creates notice of proceedings documents', async (I, caseViewPage, createNoticeOfProceedingsEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.createNoticeOfProceedings);
  createNoticeOfProceedingsEventPage.checkC6();
  createNoticeOfProceedingsEventPage.checkC6A();
  createNoticeOfProceedingsEventPage.selectJudgeTitle();
  createNoticeOfProceedingsEventPage.enterJudgeLastName('Sarah Simpson');
  createNoticeOfProceedingsEventPage.enterLegalAdvisorName('Ian Watson');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.createNoticeOfProceedings);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.seeInTab(['Notice of proceedings 1', 'File name'], 'Notice_of_proceedings_c6.pdf');
  I.seeInTab(['Notice of proceedings 2', 'File name'], 'Notice_of_proceedings_c6a.pdf');
});

Scenario('HMCTS admin handles supplementary evidence', async (I, caseListPage, caseViewPage, handleSupplementaryEvidenceEventPage) => {
  await I.navigateToCaseList();
  await caseListPage.searchForCasesWithHandledEvidences(submittedAt);
  await I.dontSeeCaseInSearchResult(caseId);

  await I.navigateToCaseDetails(caseId);
  await caseViewPage.goToNewActions(config.administrationActions.handleSupplementaryEvidence);
  handleSupplementaryEvidenceEventPage.handleSupplementaryEvidence();
  await I.completeEvent('Save and continue');
  await I.seeEventSubmissionConfirmation(config.administrationActions.handleSupplementaryEvidence);

  await I.navigateToCaseList();
  await caseListPage.searchForCasesWithHandledEvidences(submittedAt);
  await I.seeCaseInSearchResult(caseId);
});

Scenario('HMCTS admin sends email to gatekeeper with a link to the case', async (I, caseViewPage, sendCaseToGatekeeperEventPage, enterFamilyManCaseNumberEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.addFamilyManCaseNumber);
  enterFamilyManCaseNumberEventPage.enterCaseID();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.addFamilyManCaseNumber);

  await caseViewPage.goToNewActions(config.administrationActions.sendToGatekeeper);
  await sendCaseToGatekeeperEventPage.enterEmail();
  await I.addAnotherElementToCollection();
  await sendCaseToGatekeeperEventPage.enterEmail("cafcass+gatekeeping@gmail.com");
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.sendToGatekeeper);
});

Scenario('HMCTS admin adds a note to the case', async (I, caseViewPage, addNoteEventPage) => {
  const note = 'Example note';
  await caseViewPage.goToNewActions(config.administrationActions.addNote);
  addNoteEventPage.addNote(note);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.addNote);
  caseViewPage.selectTab(caseViewPage.tabs.notes);
  I.seeInTab(['Note 1', 'Note'], note);
});

Scenario('HMCTS admin update FamilyMan reference number after sending case to gatekeeper', async (I, caseViewPage, loginPage, enterFamilyManCaseNumberEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.addFamilyManCaseNumber);
  enterFamilyManCaseNumberEventPage.enterCaseID('updatedmockcaseID');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.addFamilyManCaseNumber);
});

Scenario('HMCTS admin adds expert report log', async (I, caseViewPage, loginPage, addExpertReportEventPage) => {
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
