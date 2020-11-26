const config = require('../config.js');
const hearingDetails = require('../fixtures/hearingTypeDetails.js');
const directions = require('../fixtures/directions.js');
const dateFormat = require('dateformat');
const dateToString = require('../helpers/date_to_string_helper');
const gatekeepingNoHearingDetails = require('../fixtures/caseData/gatekeepingNoHearingDetails.json');

let caseId;
let submittedAt;

Feature('Gatekeeper Case administration after gatekeeping @failure');

BeforeSuite(async ({I}) => {
  caseId = await I.submitNewCaseWithData(gatekeepingNoHearingDetails);
  submittedAt = new Date();
  await I.signIn(config.gateKeeperUser);
});

Before(async ({I}) => await I.navigateToCaseDetails(caseId));

Scenario('Gatekeeper notifies another gatekeeper with a link to the case', async ({I, caseViewPage, notifyGatekeeperEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.notifyGatekeeper);
  await notifyGatekeeperEventPage.enterEmail('gatekeeper@mailnesia.com');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.notifyGatekeeper);
});

Scenario('Gatekeeper adds allocated judge', async ({I, caseViewPage, allocatedJudgeEventPage}) => {
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

Scenario('Gatekeeper make allocation decision based on proposal', async ({I, caseViewPage, enterAllocationDecisionEventPage}) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterAllocationDecision);
  enterAllocationDecisionEventPage.selectCorrectLevelOfJudge('Yes');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterAllocationDecision);
});

Scenario('Gatekeeper enters allocation decision', async ({I, caseViewPage, enterAllocationDecisionEventPage}) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterAllocationDecision);
  enterAllocationDecisionEventPage.selectCorrectLevelOfJudge('No');
  enterAllocationDecisionEventPage.selectAllocationDecision('Lay justices');
  enterAllocationDecisionEventPage.enterProposalReason('test');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterAllocationDecision);
});

Scenario('Gatekeeper manages hearings', async ({I, caseViewPage, manageHearingsEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.manageHearings);
  await manageHearingsEventPage.enterHearingDetails(hearingDetails[0]);
  await manageHearingsEventPage.enterVenue(hearingDetails[0]);
  await I.goToNextPage();
  await manageHearingsEventPage.enterJudgeDetails(hearingDetails[0]);
  await manageHearingsEventPage.enterLegalAdvisorName(hearingDetails[0].judgeAndLegalAdvisor.legalAdvisorName);
  await I.goToNextPage();
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
  I.seeInTab(['Hearing 1', 'Allocated judge or magistrate'], 'Her Honour Judge Moley');
  I.seeInTab(['Hearing 1', 'Hearing judge or magistrate'], 'Her Honour Judge Reed');
  I.seeInTab(['Hearing 1', 'Justices\' Legal Adviser\'s full name'], hearingDetails[0].judgeAndLegalAdvisor.legalAdvisorName);
  I.seeInTab(['Hearing 1', 'Additional notes'], hearingDetails[0].additionalNotes);
  I.seeInTab(['Hearing 1', 'Notice of hearing'], `Notice_of_hearing_${dateFormat(submittedAt, 'ddmmmm')}.pdf`);
});

Scenario('Gatekeeper drafts standard directions', async ({I, caseViewPage, draftStandardDirectionsEventPage}) => {
  const today = new Date();
  await caseViewPage.goToNewActions(config.administrationActions.draftStandardDirections);
  await draftStandardDirectionsEventPage.createSDOThroughService();
  await draftStandardDirectionsEventPage.skipDateOfIssue();
  await draftStandardDirectionsEventPage.useAllocatedJudge('Bob Ross');
  await I.goToNextPage();
  await draftStandardDirectionsEventPage.enterDatesForDirections(directions[0]);
  await draftStandardDirectionsEventPage.markAsDraft();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.draftStandardDirections);

  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  I.seeInTab(['Gatekeeping order', 'File'], 'draft-standard-directions-order.pdf');
  I.seeInTab(['Gatekeeping order', 'Date of issue'], dateFormat(today, 'd mmmm yyyy'));
}).retry(1); // Send letter is async for the hearing details event, if things are running slow then a data altered outside of transaction error may be raised at the end of this scenario.

Scenario('Gatekeeper submits final version of standard directions', async ({I, caseViewPage, draftStandardDirectionsEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.draftStandardDirections);
  await draftStandardDirectionsEventPage.enterDateOfIssue({day: 11, month: 1, year: 2020});
  await draftStandardDirectionsEventPage.useAllocatedJudge('Bob Ross');
  await I.goToNextPage();
  await draftStandardDirectionsEventPage.enterDatesForDirections(directions[0]);
  await draftStandardDirectionsEventPage.markAsFinal();
  draftStandardDirectionsEventPage.checkC6();
  draftStandardDirectionsEventPage.checkC6A();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.draftStandardDirections);

  caseViewPage.selectTab(caseViewPage.tabs.hearings);
  I.seeInTab(['Notice of proceedings 1', 'File name'], 'Notice_of_proceedings_c6.pdf');
  I.seeInTab(['Notice of proceedings 2', 'File name'], 'Notice_of_proceedings_c6a.pdf');

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
