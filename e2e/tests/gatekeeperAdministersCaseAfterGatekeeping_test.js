const config = require('../config.js');
const hearingDetails = require('../fixtures/hearingTypeDetails.js');
const directions = require('../fixtures/directions.js');
const dateFormat = require('dateformat');
const dateToString = require('../helpers/date_to_string_helper');

let caseId;

Feature('Gatekeeper Case administration after gatekeeping');

Before(async (I, caseViewPage, submitApplicationEventPage, enterFamilyManCaseNumberEventPage, sendCaseToGatekeeperEventPage) => {
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

    //hmcts login, enter case number and send to gatekeeper
    await I.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
    await I.navigateToCaseDetails(caseId);
    caseViewPage.goToNewActions(config.administrationActions.addFamilyManCaseNumber);
    enterFamilyManCaseNumberEventPage.enterCaseID();
    await I.completeEvent('Save and continue');
    caseViewPage.goToNewActions(config.administrationActions.sendToGatekeeper);
    sendCaseToGatekeeperEventPage.enterEmail();
    await I.completeEvent('Save and continue');
    I.signOut();

    await I.signIn(config.gateKeeperEmail, config.gateKeeperPassword);
  }
  await I.navigateToCaseDetails(caseId);
});

Scenario('gatekeeper enters allocation decision without proposal', async (I, caseViewPage, enterAllocationDecisionEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterAllocationDecision);
  enterAllocationDecisionEventPage.selectAllocationDecision('Lay justices');
  enterAllocationDecisionEventPage.enterProposalReason('test');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterAllocationDecision);
});

Scenario('gatekeeper enters allocation decision', async (I, caseViewPage, enterAllocationDecisionEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterAllocationDecision);
  enterAllocationDecisionEventPage.selectAllocationDecision('Lay justices');
  enterAllocationDecisionEventPage.enterProposalReason('test');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterAllocationDecision);
});

Scenario('Gatekeeper enters hearing details and submits', async (I, caseViewPage, loginPage, addHearingBookingDetailsEventPage) => {
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
  I.seeAnswerInTab(2, 'Judge and legal advisor', 'Last name', hearingDetails[1].judgeAndLegalAdvisor.judgeLastName);
  I.seeAnswerInTab(3, 'Judge and legal advisor', 'Legal advisor\'s full name', hearingDetails[1].judgeAndLegalAdvisor.legalAdvisorName);
});

Scenario('Gatekeeper drafts standard directions', async (I, caseViewPage, draftStandardDirectionsEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.draftStandardDirections);
  await draftStandardDirectionsEventPage.enterJudgeAndLegalAdvisor('Smith', 'Bob Ross');
  await draftStandardDirectionsEventPage.enterDatesForDirections(directions[0]);
  await draftStandardDirectionsEventPage.markAsDraft();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.draftStandardDirections);
  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  I.see('draft-standard-directions-order.pdf');
  I.seeAnswerInTab(1, 'Directions 1', 'Direction title', 'Request permission for expert evidence');
  I.seeAnswerInTab(4, 'Directions 1', 'Description', 'Your request must be in line with Family Procedure Rules part 25 and Practice Direction 25C. Give other parties a list of names of suitable experts.');
  I.seeAnswerInTab(5, 'Directions 1', 'For', 'All parties');
  I.seeAnswerInTab(6, 'Directions 1', 'Due date and time', '1 Jan 2050, 12:00:00 PM');
});

Scenario('Gatekeeper submits final version of standard directions', async (I, caseViewPage, draftStandardDirectionsEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.draftStandardDirections);
  await draftStandardDirectionsEventPage.enterJudgeAndLegalAdvisor('Smith', 'Bob Ross');
  await draftStandardDirectionsEventPage.enterDatesForDirections(directions[0]);
  await draftStandardDirectionsEventPage.markAsFinal();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.draftStandardDirections);

  caseViewPage.checkActionsAreAvailable([
    config.administrationActions.addHearingBookingDetails,
    config.administrationActions.amendChildren,
    config.administrationActions.amendRespondents,
    config.administrationActions.amendOther,
    config.administrationActions.amendInternationalElement,
    config.administrationActions.amendOtherProceedings,
    config.administrationActions.amendAttendingHearing,
    config.applicationActions.uploadDocuments,
  ]);
  caseViewPage.checkActionsAreNotAvailable([
    config.applicationActions.enterAllocationDecision,
    config.administrationActions.draftStandardDirections,
  ]);
});
