const config = require('../config.js');
const hearingDetails = require('../fixtures/hearingTypeDetails.js');

let caseId;

Feature('Gatekeeper Case administration after submission');

Before(async (I, caseViewPage, submitApplicationEventPage) => {
  if (!caseId) {
    await I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
    await I.enterMandatoryFields();
    caseViewPage.goToNewActions(config.applicationActions.submitCase);
    submitApplicationEventPage.giveConsent();
    await I.completeEvent('Submit');

    // eslint-disable-next-line require-atomic-updates
    caseId = await I.grabTextFrom('.heading-h1');
    console.log(`Case ${caseId} has been submitted`);

    I.signOut();
    await I.signIn(config.gateKeeperEmail, config.gateKeeperPassword);
  }
  await I.navigateToCaseDetails(caseId);
});

Scenario('gatekeeper enters allocation decision', async (I, caseViewPage, enterAllocationDecisionEventPage) => {
  caseViewPage.goToNewActions(config.applicationActions.enterAllocationDecision);
  enterAllocationDecisionEventPage.selectAllocationDecision('Lay justices');
  enterAllocationDecisionEventPage.enterProposalReason('test');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterAllocationDecision);
});

Scenario('Gatekeeper enters hearing details and submits', async (I, caseViewPage, loginPage, addHearingBookingDetailsEventPage) => {
  caseViewPage.goToNewActions(config.administrationActions.addHearingBookingDetails);
  await addHearingBookingDetailsEventPage.enterHearingDetails(hearingDetails[0]);
  await I.addAnotherElementToCollection();
  await addHearingBookingDetailsEventPage.enterHearingDetails(hearingDetails[1]);
  await I.completeEvent('Save and continue', { summary: 'summary', description: 'description' });
  I.seeEventSubmissionConfirmation(config.administrationActions.addHearingBookingDetails);
  caseViewPage.selectTab(caseViewPage.tabs.hearings);
  I.seeAnswerInTab(1, 'Hearing 1', 'Type of hearing', hearingDetails[0].caseManagement);
  I.seeAnswerInTab(2, 'Hearing 1', 'Venue', hearingDetails[0].venue);
  I.seeAnswerInTab(3, 'Hearing 1', 'Date', '1 Jan 2050');
  I.seeAnswerInTab(4, 'Hearing 1', 'Pre-hearing attendance', hearingDetails[0].preHearingAttendance);
  I.seeAnswerInTab(5, 'Hearing 1', 'Hearing time', hearingDetails[0].time);
  I.seeAnswerInTab(6, 'Hearing 1', 'Hearing needs booked', hearingDetails[0].type.interpreter);
  I.seeAnswerInTab(6, 'Hearing 1', '', hearingDetails[0].type.welsh);
  I.seeAnswerInTab(6, 'Hearing 1', '', hearingDetails[0].type.somethingElse);
  I.seeAnswerInTab(7, 'Hearing 1', 'Give details', hearingDetails[0].giveDetails);
  I.seeAnswerInTab(8, 'Hearing 1', 'Judge or magistrate\'s title', hearingDetails[0].judgeTitle);
  I.seeAnswerInTab(9, 'Hearing 1', 'Judge or magistrate\'s last name', hearingDetails[0].lastName);

  I.seeAnswerInTab(1, 'Hearing 2', 'Type of hearing', hearingDetails[1].caseManagement);
  I.seeAnswerInTab(2, 'Hearing 2', 'Venue', hearingDetails[1].venue);
  I.seeAnswerInTab(3, 'Hearing 2', 'Date', '2 Feb 2060');
  I.seeAnswerInTab(4, 'Hearing 2', 'Pre-hearing attendance', hearingDetails[1].preHearingAttendance);
  I.seeAnswerInTab(5, 'Hearing 2', 'Hearing time', hearingDetails[1].time);
  I.seeAnswerInTab(6, 'Hearing 2', 'Hearing needs booked', hearingDetails[1].type.interpreter);
  I.seeAnswerInTab(6, 'Hearing 2', '', hearingDetails[1].type.welsh);
  I.seeAnswerInTab(6, 'Hearing 2', '', hearingDetails[1].type.somethingElse);
  I.seeAnswerInTab(7, 'Hearing 2', 'Give details', hearingDetails[1].giveDetails);
  I.seeAnswerInTab(8, 'Hearing 2', 'Judge or magistrate\'s title', hearingDetails[1].judgeTitle);
  I.seeAnswerInTab(9, 'Hearing 2', 'Judge or magistrate\'s last name', hearingDetails[1].lastName);
});
