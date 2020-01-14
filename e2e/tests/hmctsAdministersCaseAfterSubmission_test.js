const config = require('../config.js');
const hearingDetails = require('../fixtures/hearingTypeDetails.js');
const orders = require('../fixtures/orders.js');
const representatives = require('../fixtures/representatives.js');
const dateFormat = require('dateformat');
const dateToString = require('../helpers/date_to_string_helper');

let caseId;

Feature('Case administration after submission');

Before(async (I, caseViewPage, submitApplicationEventPage) => {

  if (!caseId) {
    await I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
    await I.enterMandatoryFields();
    await caseViewPage.goToNewActions(config.applicationActions.submitCase);
    submitApplicationEventPage.giveConsent();
    await I.completeEvent('Submit');

    // eslint-disable-next-line require-atomic-updates
    caseId = await I.grabTextFrom('.heading-h1');
    console.log(`Case ${caseId} has been submitted`);

    I.signOut();
    await I.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
  }

  await I.navigateToCaseDetails(caseId);
});

Scenario('HMCTS admin enters FamilyMan reference number', async (I, caseViewPage, loginPage, enterFamilyManCaseNumberEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.addFamilyManCaseNumber);
  enterFamilyManCaseNumberEventPage.enterCaseID();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.addFamilyManCaseNumber);
});

Scenario('HMCTS admin amends children, respondents, others, international element, other proceedings and attending hearing', async (I, caseViewPage, loginPage, enterFamilyManCaseNumberEventPage, enterOtherProceedingsEventPage) => {
  async function I_doEventAndCheckIfAppropriateSummaryAndDescriptionIsVisible(event, summary, description, I_doActionsOnEditPage = () => {}) {
    await caseViewPage.goToNewActions(event);
    I_doActionsOnEditPage();
    await I.completeEvent('Save and continue', { summary: summary, description: description });
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
  I.seeAnswerInTab('1', 'Other documents 1', 'Document name', 'Document 1');
  I.seeAnswerInTab('2', 'Other documents 1', 'Upload a file', 'mockFile.txt');
  I.seeAnswerInTab('1', 'Other documents 2', 'Document name', 'Document 2');
  I.seeAnswerInTab('2', 'Other documents 2', 'Upload a file', 'mockFile.txt');
});

Scenario('HMCTS admin uploads C2 documents to the case', async (I, caseViewPage, uploadC2DocumentsEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.uploadC2Documents);
  uploadC2DocumentsEventPage.uploadC2Document(config.testFile, 'Rachel Zane C2');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.uploadC2Documents);
  await caseViewPage.goToNewActions(config.administrationActions.uploadC2Documents);
  uploadC2DocumentsEventPage.uploadC2Document(config.testFile, 'Jessica Pearson C2');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.uploadC2Documents);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.seeAnswerInTab('1', 'C2 1', 'Upload a file', 'mockFile.txt');
  I.seeAnswerInTab('4', 'C2 1', 'Description', 'Rachel Zane C2');
  I.seeAnswerInTab('1', 'C2 2', 'Upload a file', 'mockFile.txt');
  I.seeAnswerInTab('4', 'C2 2', 'Description', 'Jessica Pearson C2');
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
  I.seeAnswerInTab(1, 'Hearing 1', 'Type of hearing', hearingDetails[0].caseManagement);
  I.seeAnswerInTab(2, 'Hearing 1', 'Venue', hearingDetails[0].venue);
  I.seeAnswerInTab(3, 'Hearing 1', 'Start date and time', dateFormat(startDate, 'd mmm yyyy, h:MM:ss TT'));
  I.seeAnswerInTab(4, 'Hearing 1', 'End date and time', dateFormat(endDate, 'd mmm yyyy, h:MM:ss TT'));
  I.seeAnswerInTab(5, 'Hearing 1', 'Hearing needs booked', hearingDetails[0].type.interpreter);
  I.seeAnswerInTab(5, 'Hearing 1', '', hearingDetails[0].type.welsh);
  I.seeAnswerInTab(5, 'Hearing 1', '', hearingDetails[0].type.somethingElse);
  I.seeAnswerInTab(6, 'Hearing 1', 'Give details', hearingDetails[0].giveDetails);
  I.seeAnswerInTab(1, 'Judge and legal advisor', 'Judge or magistrate\'s title', hearingDetails[0].judgeAndLegalAdvisor.judgeTitle);
  I.seeAnswerInTab(2, 'Judge and legal advisor', 'Last name', hearingDetails[0].judgeAndLegalAdvisor.judgeLastName);
  I.seeAnswerInTab(3, 'Judge and legal advisor', 'Legal advisor\'s full name', hearingDetails[0].judgeAndLegalAdvisor.legalAdvisorName);

  startDate = dateToString(hearingDetails[1].startDate);
  endDate = dateToString(hearingDetails[1].endDate);
  I.seeAnswerInTab(1, 'Hearing 2', 'Type of hearing', hearingDetails[1].caseManagement);
  I.seeAnswerInTab(2, 'Hearing 2', 'Venue', hearingDetails[1].venue);
  I.seeAnswerInTab(3, 'Hearing 2', 'Start date and time', dateFormat(startDate, 'd mmm yyyy, h:MM:ss TT'));
  I.seeAnswerInTab(4, 'Hearing 2', 'End date and time', dateFormat(endDate, 'd mmm yyyy, h:MM:ss TT'));
  I.seeAnswerInTab(5, 'Hearing 2', 'Hearing needs booked', hearingDetails[1].type.interpreter);
  I.seeAnswerInTab(5, 'Hearing 2', '', hearingDetails[1].type.welsh);
  I.seeAnswerInTab(5, 'Hearing 2', '', hearingDetails[1].type.somethingElse);
  I.seeAnswerInTab(6, 'Hearing 2', 'Give details', hearingDetails[1].giveDetails);
  I.seeAnswerInTab(1, 'Judge and legal advisor', 'Judge or magistrate\'s title', hearingDetails[1].judgeAndLegalAdvisor.judgeTitle);
  I.seeAnswerInTab(2, 'Judge and legal advisor', 'Title', hearingDetails[1].judgeAndLegalAdvisor.otherTitle);
  I.seeAnswerInTab(3, 'Judge and legal advisor', 'Last name', hearingDetails[1].judgeAndLegalAdvisor.judgeLastName);
  I.seeAnswerInTab(4, 'Judge and legal advisor', 'Legal advisor\'s full name', hearingDetails[1].judgeAndLegalAdvisor.legalAdvisorName);
});

Scenario('HMCTS admin creates multiple orders for the case', async (I, caseViewPage, createOrderEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.createOrder);
  await createOrderEventPage.selectType(orders[0].type);
  await I.retryUntilExists(() => I.click('Continue'), '#order_title');
  await createOrderEventPage.enterC21OrderDetails();
  await I.retryUntilExists(() => I.click('Continue'), '#judgeAndLegalAdvisor_judgeTitle');
  await createOrderEventPage.enterJudgeAndLegalAdvisor(orders[0].judgeAndLegalAdvisor.judgeLastName, orders[0].judgeAndLegalAdvisor.legalAdvisorName);
  await I.completeEvent('Save and continue');
  let orderTime = new Date();

  I.seeEventSubmissionConfirmation(config.administrationActions.createOrder);
  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeAnswerInTab(1, 'Order 1', 'Type of order', orders[0].type);
  I.seeAnswerInTab(2, 'Order 1', 'Order title', orders[0].title);
  I.seeAnswerInTab(4, 'Order 1', 'Order document', orders[0].document);
  I.seeAnswerInTab(5, 'Order 1', 'Date and time of upload', dateFormat(orderTime, 'd mmmm yyyy'));
  I.seeAnswerInTab(1, 'Judge and legal advisor', 'Judge or magistrate\'s title', orders[0].judgeAndLegalAdvisor.judgeTitle);
  I.seeAnswerInTab(2, 'Judge and legal advisor', 'Last name', orders[0].judgeAndLegalAdvisor.judgeLastName);
  I.seeAnswerInTab(3, 'Judge and legal advisor', 'Legal advisor\'s full name',  orders[0].judgeAndLegalAdvisor.legalAdvisorName);

  await caseViewPage.goToNewActions(config.administrationActions.createOrder);
  await createOrderEventPage.selectType(orders[1].type);
  await I.retryUntilExists(() => I.click('Continue'), '#judgeAndLegalAdvisor_judgeTitle');
  await createOrderEventPage.enterJudgeAndLegalAdvisor(orders[1].judgeAndLegalAdvisor.judgeLastName, orders[1].judgeAndLegalAdvisor.legalAdvisorName);
  await I.retryUntilExists(() => I.click('Continue'), '#orderFurtherDirections_directionsNeeded');
  await createOrderEventPage.enterDirections('example directions');
  await I.completeEvent('Save and continue');
  orderTime = new Date();

  I.seeEventSubmissionConfirmation(config.administrationActions.createOrder);
  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeAnswerInTab(1, 'Order 2', 'Type of order', orders[1].type);
  I.seeAnswerInTab(2, 'Order 2', 'Order document', orders[1].document);
  I.seeAnswerInTab(3, 'Order 2', 'Date and time of upload', dateFormat(orderTime, 'd mmmm yyyy'));
  I.seeAnswerInTab(1, 'Judge and legal advisor', 'Judge or magistrate\'s title', orders[1].judgeAndLegalAdvisor.judgeTitle);
  I.seeAnswerInTab(2, 'Judge and legal advisor', 'Last name', orders[1].judgeAndLegalAdvisor.judgeLastName);
  I.seeAnswerInTab(3, 'Judge and legal advisor', 'Legal advisor\'s full name',  orders[1].judgeAndLegalAdvisor.legalAdvisorName);

  orderTime = new Date();
  const tomorrow = new Date(Date.now() + (3600 * 1000 * 24));
  await caseViewPage.goToNewActions(config.administrationActions.createOrder);
  await createOrderEventPage.selectType(orders[2].type);
  await I.retryUntilExists(() => I.click('Continue'), '#epoChildren_descriptionNeeded');
  await createOrderEventPage.enterChildrenDescription(orders[2].childrenDescription);
  await I.retryUntilExists(() => I.click('Continue'), '#epoType');
  createOrderEventPage.selectEpoType(orders[2].epoType);
  createOrderEventPage.enterRemovalAddress(orders[2].removalAddress);
  await I.retryUntilExists(() => I.click('Continue'), '#epoPhrase_includePhrase');
  createOrderEventPage.includePhrase(orders[2].includePhrase);
  await I.retryUntilExists(() => I.click('Continue'), '#epoEndDate');
  createOrderEventPage.enterEpoEndDate(tomorrow);
  await I.retryUntilExists(() => I.click('Continue'), '#judgeAndLegalAdvisor_judgeTitle');
  await createOrderEventPage.enterJudgeAndLegalAdvisor(orders[2].judgeAndLegalAdvisor.judgeLastName, orders[2].judgeAndLegalAdvisor.legalAdvisorName);
  await I.retryUntilExists(() => I.click('Continue'), '#orderFurtherDirections_directionsNeeded');
  await createOrderEventPage.enterDirections('example directions');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.createOrder);
  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeAnswerInTab(1, 'Order 3', 'Type of order', orders[2].type);
  I.seeAnswerInTab(2, 'Order 3', 'Order document', orders[2].document);
  I.seeAnswerInTab(3, 'Order 3', 'Date and time of upload', dateFormat(orderTime, 'd mmmm yyyy'));
  I.seeAnswerInTab(1, 'Judge and legal advisor', 'Judge or magistrate\'s title', orders[2].judgeAndLegalAdvisor.judgeTitle);
  I.seeAnswerInTab(2, 'Judge and legal advisor', 'Last name', orders[2].judgeAndLegalAdvisor.judgeLastName);
  I.seeAnswerInTab(3, 'Judge and legal advisor', 'Legal advisor\'s full name',  orders[2].judgeAndLegalAdvisor.legalAdvisorName);
});

Scenario('HMCTS admin creates notice of proceedings documents', async (I, caseViewPage, createNoticeOfProceedingsEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.createNoticeOfProceedings);
  await createNoticeOfProceedingsEventPage.checkC6();
  await createNoticeOfProceedingsEventPage.checkC6A();
  await createNoticeOfProceedingsEventPage.selectJudgeTitle();
  await createNoticeOfProceedingsEventPage.enterJudgeLastName('Sarah Simpson');
  await createNoticeOfProceedingsEventPage.enterLegalAdvisorName('Ian Watson');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.createNoticeOfProceedings);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.seeAnswerInTab('1', 'Notice of proceedings 1', 'File name', 'Notice_of_proceedings_c6.pdf');
  I.seeAnswerInTab('1', 'Notice of proceedings 2', 'File name', 'Notice_of_proceedings_c6a.pdf');
});

Scenario('HMCTS admin share case with representatives', async (I, caseViewPage, enterRepresentativesEventPage) => {
  await I.navigateToCaseDetails(caseId);
  await caseViewPage.goToNewActions(config.administrationActions.amendRepresentatives);
  const representative = {...representatives[0], email: config.hillingdonLocalAuthorityEmailUserOne};
  await enterRepresentativesEventPage.enterRepresentative(representative);

  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.amendRepresentatives);

  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeAnswerInTab(1, 'Representatives 1', 'Full name', representative.fullName);
  I.seeAnswerInTab(2, 'Representatives 1', 'Position in a case', representative.positionInACase);
  I.seeAnswerInTab(3, 'Representatives 1', 'Email address', representative.email);
  I.seeAnswerInTab(4, 'Representatives 1', 'Phone number', representative.telephone);
  I.seeAnswerInTab(6, 'Representatives 1', 'How do they want to get case information?', representative.servingPreferences);
  I.seeAnswerInTab(7, 'Representatives 1', 'Who are they?', representative.role);

  I.signOut();
  await I.signIn(config.hillingdonLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  await I.navigateToCaseDetails(caseId);
  I.see(caseId);
  I.signOut();
  await I.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
});

Scenario('HMCTS admin sends email to gatekeeper with a link to the case', async (I, caseViewPage, sendCaseToGatekeeperEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.sendToGatekeeper);
  sendCaseToGatekeeperEventPage.enterEmail();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.sendToGatekeeper);
});
