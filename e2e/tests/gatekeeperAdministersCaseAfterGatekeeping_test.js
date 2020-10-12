const config = require('../config.js');
const hearingDetails = require('../fixtures/hearingTypeDetails.js');
const directions = require('../fixtures/directions.js');
const dateFormat = require('dateformat');
const dateToString = require('../helpers/date_to_string_helper');
const gatekeepingNoHearingDetails = require('../fixtures/caseData/gatekeepingNoHearingDetails.json');

let caseId;
let submittedAt;

Feature('Gatekeeper Case administration after gatekeeping');

BeforeSuite(async (I) => {
  caseId = await I.submitNewCaseWithData(gatekeepingNoHearingDetails);
  submittedAt = new Date();
  await I.signIn(config.gateKeeperUser);
});

Before(async I => await I.navigateToCaseDetails(caseId));

Scenario('Gatekeeper notifies another gatekeeper with a link to the case', async (I, caseViewPage, notifyGatekeeperEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.notifyGatekeeper);
  await notifyGatekeeperEventPage.enterEmail('gatekeeper@mailnesia.com');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.notifyGatekeeper);
});

Scenario('Gatekeeper adds allocated judge', async (I, caseViewPage, allocatedJudgeEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.allocatedJudge);
  await allocatedJudgeEventPage.enterAllocatedJudge('Moley', 'moley@example.com');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.allocatedJudge);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeInTab(['Allocated Judge', 'Judge or magistrate\'s title'], 'Her Honour Judge');
  I.seeInTab(['Allocated Judge', 'Last name'], 'Moley');
  I.seeInTab(['Allocated Judge', 'Email Address'], 'moley@example.com');

  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);

  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeInTab(['Allocated Judge', 'Judge or magistrate\'s title'], 'Her Honour Judge');
  I.seeInTab(['Allocated Judge', 'Last name'], 'Moley');
  I.dontSeeInTab(['Allocated Judge', 'Email Address']);

  await I.navigateToCaseDetailsAs(config.gateKeeperUser, caseId);
});

Scenario('Gatekeeper make allocation decision based on proposal', async (I, caseViewPage, enterAllocationDecisionEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterAllocationDecision);
  enterAllocationDecisionEventPage.selectCorrectLevelOfJudge('Yes');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterAllocationDecision);
});

Scenario('Gatekeeper enters allocation decision', async (I, caseViewPage, enterAllocationDecisionEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterAllocationDecision);
  enterAllocationDecisionEventPage.selectCorrectLevelOfJudge('No');
  enterAllocationDecisionEventPage.selectAllocationDecision('Lay justices');
  enterAllocationDecisionEventPage.enterProposalReason('test');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterAllocationDecision);
});

Scenario('Gatekeeper manages hearings', async (I, caseViewPage, manageHearingsEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.manageHearings);
  await manageHearingsEventPage.enterHearingDetails(hearingDetails[0]);
  await manageHearingsEventPage.enterVenue(hearingDetails[0]);
  await I.retryUntilExists(() => I.click('Continue'), '#judgeAndLegalAdvisor_judgeTitle');
  await manageHearingsEventPage.enterJudgeAndLegalAdvisorDetails(hearingDetails[0]);
  await I.retryUntilExists(() => I.click('Continue'), '#sendNoticeOfHearing');
  await manageHearingsEventPage.sendNoticeOfHearingWithNotes(hearingDetails[0].additionalNotes);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageHearings);
  caseViewPage.selectTab(caseViewPage.tabs.hearings);

  let startDate = dateToString(hearingDetails[0].startDate);
  let endDate = dateToString(hearingDetails[0].endDate);
  I.seeInTab(['Hearing 1', 'Type of hearing'], hearingDetails[0].caseManagement);
  I.seeInTab(['Hearing 1', 'Venue'], hearingDetails[0].venue);
  I.seeInTab(['Hearing 1', 'Start date and time'], dateFormat(startDate, 'd mmm yyyy, h:MM:ss TT'));
  I.seeInTab(['Hearing 1', 'End date and time'], dateFormat(endDate, 'd mmm yyyy, h:MM:ss TT'));
  I.seeInTab(['Hearing 1', 'Judge and Justices\' Legal Adviser', 'Judge or magistrate\'s title'], hearingDetails[0].judgeAndLegalAdvisor.judgeTitle);
  I.seeInTab(['Hearing 1', 'Judge and Justices\' Legal Adviser', 'Last name'], hearingDetails[0].judgeAndLegalAdvisor.judgeLastName);
  I.seeInTab(['Hearing 1', 'Judge and Justices\' Legal Adviser', 'Justices\' Legal Adviser\'s full name'], hearingDetails[0].judgeAndLegalAdvisor.legalAdvisorName);
  I.seeInTab(['Hearing 1', 'Additional notes'], hearingDetails[0].additionalNotes);
  I.seeInTab(['Hearing 1', 'Notice of hearing'], `Notice_of_hearing_${dateFormat(submittedAt, 'ddmmmm')}.pdf`);

});

xScenario('Gatekeeper enters hearing details and submits', async (I, caseViewPage, addHearingBookingDetailsEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.addHearingBookingDetails);
  await addHearingBookingDetailsEventPage.enterHearingDetails(hearingDetails[0]);
  await addHearingBookingDetailsEventPage.useAllocatedJudge();
  await addHearingBookingDetailsEventPage.enterLegalAdvisor(hearingDetails[0].judgeAndLegalAdvisor.legalAdvisorName);
  await I.addAnotherElementToCollection();
  await addHearingBookingDetailsEventPage.enterHearingDetails(hearingDetails[1]);
  await addHearingBookingDetailsEventPage.enterJudge(hearingDetails[1].judgeAndLegalAdvisor);
  await addHearingBookingDetailsEventPage.enterLegalAdvisor(hearingDetails[1].judgeAndLegalAdvisor.legalAdvisorName);
  await I.retryUntilExists(() => I.click('Continue'), '#newHearingSelector_newHearingSelector');
  addHearingBookingDetailsEventPage.sendNoticeOfHearing('No');
  addHearingBookingDetailsEventPage.sendNoticeOfHearing('No', 1);
  await I.completeEvent('Save and continue');
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
  I.seeInTab(['Hearing 1', 'Judge and Justices\' Legal Adviser', 'Judge or magistrate\'s title'], 'Her Honour Judge');
  I.seeInTab(['Hearing 1', 'Judge and Justices\' Legal Adviser', 'Last name'], 'Moley');
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
  await draftStandardDirectionsEventPage.createSDOThroughService();
  await draftStandardDirectionsEventPage.skipDateOfIssue();
  await draftStandardDirectionsEventPage.useAllocatedJudge('Bob Ross');
  await draftStandardDirectionsEventPage.enterDatesForDirections(directions[0]);
  await draftStandardDirectionsEventPage.markAsDraft();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.draftStandardDirections);

  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  I.seeInTab(['Gatekeeping order', 'File'], 'draft-standard-directions-order.pdf');
  I.seeInTab(['Gatekeeping order', 'Date of issue'], dateFormat(today, 'd mmmm yyyy'));
}).retry(1); // Send letter is async for the hearing details event, if things are running slow then a data altered outside of transaction error may be raised at the end of this scenario.

Scenario('Gatekeeper submits final version of standard directions', async (I, caseViewPage, draftStandardDirectionsEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.draftStandardDirections);
  await draftStandardDirectionsEventPage.enterDateOfIssue({day: 11, month: 1, year: 2020});
  await draftStandardDirectionsEventPage.useAllocatedJudge('Bob Ross');
  await draftStandardDirectionsEventPage.enterDatesForDirections(directions[0]);
  await draftStandardDirectionsEventPage.markAsFinal();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.draftStandardDirections);

  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeInTab(['Gatekeeping order', 'File'], 'standard-directions-order.pdf');
  I.seeInTab(['Gatekeeping order', 'Date of issue'], '11 January 2020');

  caseViewPage.checkActionsAreAvailable([
    config.administrationActions.manageHearings,
    config.administrationActions.amendChildren,
    config.administrationActions.amendRespondents,
    config.administrationActions.amendOther,
    config.administrationActions.amendInternationalElement,
    config.administrationActions.amendAttendingHearing,
  ]);
  caseViewPage.checkActionsAreNotAvailable([
    config.applicationActions.enterAllocationDecision,
    config.administrationActions.draftStandardDirections,
  ]);
});
