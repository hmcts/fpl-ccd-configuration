const config = require('../config.js');
const hearingDetails = require('../fixtures/hearingTypeDetails.js');

let caseId;

Feature('Add hearing booking details');

Before(async (I, caseViewPage, submitApplicationEventPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseId = await I.grabTextFrom('.heading-h1');
  await I.enterMandatoryFields();
  caseViewPage.goToNewActions(config.applicationActions.submitCase);
  submitApplicationEventPage.giveConsent();
  I.continueAndSubmit();
  I.signOut();
});

Scenario('Enter hearing details and submitting as HMCTS admin', async (I, caseViewPage, loginPage, addHearingBookingDetailsEventPage) => {
  loginPage.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
  I.navigateToCaseDetails(caseId);
  I.see(caseId);
  caseViewPage.goToNewActions(config.administrationActions.addHearingBookingDetails);
  addHearingBookingDetailsEventPage.addHearing();
  await addHearingBookingDetailsEventPage.enterHearingDetails(hearingDetails[0]);
  addHearingBookingDetailsEventPage.addHearing();
  await addHearingBookingDetailsEventPage.enterHearingDetails(hearingDetails[1]);
  I.continueAndProvideSummary('summary', 'description');
  I.seeEventSubmissionConfirmation(config.administrationActions.addHearingBookingDetails);
  caseViewPage.selectTab(caseViewPage.tabs.hearings);
  seeAnswersInHearingTab(I);
});

Scenario('Enter hearing details and submitting as Judiciary', async (I, caseViewPage, loginPage, addHearingBookingDetailsEventPage) => {
  loginPage.signIn(config.judiciaryEmail, config.judiciaryPassword);
  I.navigateToCaseDetails(caseId);
  I.see(caseId);
  caseViewPage.goToNewActions(config.administrationActions.addHearingBookingDetails);
  addHearingBookingDetailsEventPage.addHearing();
  await addHearingBookingDetailsEventPage.enterHearingDetails(hearingDetails[0]);
  addHearingBookingDetailsEventPage.addHearing();
  await addHearingBookingDetailsEventPage.enterHearingDetails(hearingDetails[1]);
  I.continueAndProvideSummary('summary', 'description');
  I.seeEventSubmissionConfirmation(config.administrationActions.addHearingBookingDetails);
  caseViewPage.selectTab(caseViewPage.tabs.hearings);
  seeAnswersInHearingTab(I);
});


Scenario('Enter hearing details and submitting as Gatekeeper', async (I, caseViewPage, loginPage, addHearingBookingDetailsEventPage) => {
  loginPage.signIn(config.gateKeeperEmail, config.gateKeeperPassword);
  I.navigateToCaseDetails(caseId);
  I.see(caseId);
  caseViewPage.goToNewActions(config.administrationActions.addHearingBookingDetails);
  addHearingBookingDetailsEventPage.addHearing();
  await addHearingBookingDetailsEventPage.enterHearingDetails(hearingDetails[0]);
  addHearingBookingDetailsEventPage.addHearing();
  await addHearingBookingDetailsEventPage.enterHearingDetails(hearingDetails[1]);
  I.continueAndProvideSummary('summary', 'description');
  I.seeEventSubmissionConfirmation(config.administrationActions.addHearingBookingDetails);
  caseViewPage.selectTab(caseViewPage.tabs.hearings);
  seeAnswersInHearingTab(I);
});

function seeAnswersInHearingTab(I) {
  I.seeAnswerInTab(1, 'Hearing 1', 'Type of hearing', hearingDetails[0].caseManagement);
  I.seeAnswerInTab(2, 'Hearing 1', 'Venue', hearingDetails[0].hearingVenue);
  I.seeAnswerInTab(3, 'Hearing 1', 'Date', '1 Jan 2050');
  I.seeAnswerInTab(4, 'Hearing 1', 'Pre-hearing attendance', hearingDetails[0].preHearingAttendance);
  I.seeAnswerInTab(5, 'Hearing 1', 'Hearing time', hearingDetails[0].hearingTime);
  I.seeAnswerInTab(6, 'Hearing 1', 'Hearing needs booked', hearingDetails[0].hearingType.interpreter);
  I.seeAnswerInTab(6, 'Hearing 1', '', hearingDetails[0].hearingType.welsh);
  I.seeAnswerInTab(6, 'Hearing 1', '', hearingDetails[0].hearingType.somethingElse);
  I.seeAnswerInTab(7, 'Hearing 1', 'Give details', hearingDetails[0].giveDetails);
  I.seeAnswerInTab(8, 'Hearing 1', 'Title', hearingDetails[0].judgeTitle);
  I.seeAnswerInTab(9, 'Hearing 1', 'Full name', hearingDetails[0].fullName);

  I.seeAnswerInTab(1, 'Hearing 2', 'Type of hearing', hearingDetails[1].caseManagement);
  I.seeAnswerInTab(2, 'Hearing 2', 'Venue', hearingDetails[1].hearingVenue);
  I.seeAnswerInTab(3, 'Hearing 2', 'Date', '2 Feb 2060');
  I.seeAnswerInTab(4, 'Hearing 2', 'Pre-hearing attendance', hearingDetails[1].preHearingAttendance);
  I.seeAnswerInTab(5, 'Hearing 2', 'Hearing time', hearingDetails[1].hearingTime);
  I.seeAnswerInTab(6, 'Hearing 2', 'Hearing needs booked', hearingDetails[1].hearingType.interpreter);
  I.seeAnswerInTab(6, 'Hearing 2', '', hearingDetails[1].hearingType.welsh);
  I.seeAnswerInTab(6, 'Hearing 2', '', hearingDetails[1].hearingType.somethingElse);
  I.seeAnswerInTab(7, 'Hearing 2', 'Give details', hearingDetails[1].giveDetails);
  I.seeAnswerInTab(8, 'Hearing 2', 'Title', hearingDetails[1].judgeTitle);
  I.seeAnswerInTab(9, 'Hearing 2', 'Full name', hearingDetails[1].fullName);
}
