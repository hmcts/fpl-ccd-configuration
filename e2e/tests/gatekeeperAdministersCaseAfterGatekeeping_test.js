const config = require('../config.js');
const hearingDetails = require('../fixtures/hearingTypeDetails.js');
const directions = require('../fixtures/directions.js');
const dateFormat = require('dateformat');
const dateToString = require('../helpers/date_to_string_helper');

let caseId;

Feature('Gatekeeper Case administration after gatekeeping');

Before(async (I, caseViewPage, submitApplicationEventPage, enterFamilyManCaseNumberEventPage, sendCaseToGatekeeperEventPage, allocatedJudgeEventPage) => {
  if (!caseId) {
    // eslint-disable-next-line require-atomic-updates
    caseId = await I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
    await I.populateCaseWithMandatoryFields(caseId);
    await I.signIn(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
    await I.submitCase(caseId);

    console.log(`Case ${caseId} has been submitted`);
    I.signOut();

    //hmcts login, enter case number and send to gatekeeper
    await I.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
    await I.navigateToCaseDetails(caseId);
    await caseViewPage.goToNewActions(config.administrationActions.addFamilyManCaseNumber);
    enterFamilyManCaseNumberEventPage.enterCaseID();
    await I.completeEvent('Save and continue');
    await caseViewPage.goToNewActions(config.applicationActions.allocatedJudge);
    await allocatedJudgeEventPage.enterAllocatedJudge('Moley');
    await I.completeEvent('Save and continue');
    await caseViewPage.goToNewActions(config.administrationActions.sendToGatekeeper);
    sendCaseToGatekeeperEventPage.enterEmail();
    await I.completeEvent('Save and continue');
    I.signOut();

    await I.signIn(config.gateKeeperEmail, config.gateKeeperPassword);
  }
  await I.navigateToCaseDetails(caseId);
});

Scenario('Gatekeeper notifies another gatekeeper with a link to the case', async (I, caseViewPage, notifyGatekeeperEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.notifyGatekeeper);
  await notifyGatekeeperEventPage.enterEmail('gatekeeper@mailnesia.com');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.notifyGatekeeper);
});

Scenario('gatekeeper make allocation decision based on proposal', async (I, caseViewPage, enterAllocationDecisionEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterAllocationDecision);
  enterAllocationDecisionEventPage.selectCorrectLevelOfJudge('Yes');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterAllocationDecision);
});

Scenario('gatekeeper enters allocation decision', async (I, caseViewPage, enterAllocationDecisionEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterAllocationDecision);
  enterAllocationDecisionEventPage.selectCorrectLevelOfJudge('No');
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
  I.seeInTab(['Hearing 1', 'Type of hearing'], hearingDetails[0].caseManagement);
  I.seeInTab(['Hearing 1', 'Venue'], hearingDetails[0].venue);
  I.seeInTab(['Hearing 1', 'Start date and time'], dateFormat(startDate, 'd mmm yyyy, h:MM:ss TT'));
  I.seeInTab(['Hearing 1', 'End date and time'], dateFormat(endDate, 'd mmm yyyy, h:MM:ss TT'));
  I.seeInTab(['Hearing 1', 'Hearing needs booked'], [hearingDetails[0].type.interpreter, hearingDetails[0].type.welsh, hearingDetails[0].type.somethingElse]);
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

Scenario('Gatekeeper drafts standard directions', async (I, caseViewPage, draftStandardDirectionsEventPage) => {
  const today = new Date();
  await caseViewPage.goToNewActions(config.administrationActions.draftStandardDirections);
  await draftStandardDirectionsEventPage.skipDateOfIssue();
  await draftStandardDirectionsEventPage.useAllocatedJudge('Bob Ross');
  await draftStandardDirectionsEventPage.enterDatesForDirections(directions[0]);
  await draftStandardDirectionsEventPage.markAsDraft();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.draftStandardDirections);

  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  I.seeInTab(['Standard directions order', 'File'], 'draft-standard-directions-order.pdf');
  I.seeInTab(['Standard directions order', 'Date of issue'], dateFormat(today, 'd mmmm yyyy'));
  I.seeInTab(['Directions 1', 'Title'], 'Request permission for expert evidence');
  I.seeInTab(['Directions 1', 'Description'], 'Your request must be in line with Family Procedure Rules part 25 and Practice Direction 25C. Give other parties a list of names of suitable experts.');
  I.seeInTab(['Directions 1', 'For'], 'All parties');
  I.seeInTab(['Directions 1', 'Due date and time'], '1 Jan 2050, 12:00:00 PM');
});

Scenario('Gatekeeper submits final version of standard directions', async (I, caseViewPage, draftStandardDirectionsEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.draftStandardDirections);
  await draftStandardDirectionsEventPage.enterDateOfIssue({day: 11, month: 1, year: 2020});
  await draftStandardDirectionsEventPage.useAllocatedJudge('Bob Ross');
  await draftStandardDirectionsEventPage.enterDatesForDirections(directions[0]);
  await draftStandardDirectionsEventPage.markAsFinal();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.draftStandardDirections);

  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeInTab(['Standard directions order', 'File'], 'standard-directions-order.pdf');
  I.seeInTab(['Standard directions order', 'Date of issue'], '11 January 2020');

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
