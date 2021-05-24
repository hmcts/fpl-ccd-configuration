const config = require('../config.js');
const directions = require('../fixtures/directions.js');
const dateFormat = require('dateformat');
const gatekeepingCaseData = require('../fixtures/caseData/gatekeepingNoAllocatedJudge.json');

let caseId;

Feature('Gatekeeper Case administration after gatekeeping');

BeforeSuite(async ({I}) => {
  caseId = await I.submitNewCaseWithData(gatekeepingCaseData);
  await I.navigateToCaseDetailsAs(config.gateKeeperUser, caseId);
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
});

Scenario('Gatekeeper make allocation decision based on proposal', async ({I, caseViewPage, enterAllocationDecisionEventPage}) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterAllocationDecision);
  enterAllocationDecisionEventPage.selectCorrectLevelOfJudge('Yes');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterAllocationDecision);

  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeInTab(['Allocation decision', 'Which level of judge is needed for this case?'], 'District Judge');
});

Scenario('Gatekeeper enters allocation decision', async ({I, caseViewPage, enterAllocationDecisionEventPage}) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterAllocationDecision);
  enterAllocationDecisionEventPage.selectCorrectLevelOfJudge('No');
  await enterAllocationDecisionEventPage.selectAllocationDecision('Magistrate');
  await enterAllocationDecisionEventPage.enterProposalReason('new information was acquired');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterAllocationDecision);

  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeInTab(['Allocation decision', 'Which level of judge is needed for this case?'], 'Magistrate');
  I.seeInTab(['Allocation decision', 'Give reason'], 'new information was acquired');
});

Scenario('Gatekeeper drafts standard directions', async ({I, caseViewPage, draftStandardDirectionsEventPage}) => {
  const today = new Date();
  await caseViewPage.goToNewActions(config.administrationActions.draftStandardDirections);
  await draftStandardDirectionsEventPage.createSDOThroughService();
  await draftStandardDirectionsEventPage.skipDateOfIssue();
  draftStandardDirectionsEventPage.useAllocatedJudge('Bob Ross');
  await I.goToNextPage();
  await draftStandardDirectionsEventPage.enterDatesForDirections(directions[0]);
  draftStandardDirectionsEventPage.markAsDraft();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.draftStandardDirections);

  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  I.seeInTab(['Gatekeeping order', 'File'], 'draft-standard-directions-order.pdf');
  I.seeInTab(['Gatekeeping order', 'Date of issue'], dateFormat(today, 'd mmmm yyyy'));
});

Scenario('Gatekeeper submits final version of standard directions', async ({I, caseViewPage, draftStandardDirectionsEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.draftStandardDirections);
  await draftStandardDirectionsEventPage.enterDateOfIssue({day: 11, month: 1, year: 2020});
  draftStandardDirectionsEventPage.useAllocatedJudge('Bob Ross');
  await I.goToNextPage();
  await draftStandardDirectionsEventPage.enterDatesForDirections(directions[0]);
  await draftStandardDirectionsEventPage.markAsFinal();
  await draftStandardDirectionsEventPage.checkC6();
  draftStandardDirectionsEventPage.checkC6A();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.draftStandardDirections);

  caseViewPage.selectTab(caseViewPage.tabs.hearings);
  I.seeInTab(['Notice of proceedings 1', 'File name'], 'Notice_of_proceedings_c6.pdf');
  I.seeInTab(['Notice of proceedings 2', 'File name'], 'Notice_of_proceedings_c6a.pdf');

  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeInTab(['Gatekeeping order', 'File'], 'standard-directions-order.pdf');
  I.seeInTab(['Gatekeeping order', 'Date of issue'], '11 January 2020');

  await caseViewPage.checkActionsAreAvailable([
    config.administrationActions.manageHearings,
  ]);
  await caseViewPage.checkActionsAreNotAvailable([
    config.applicationActions.enterAllocationDecision,
    config.administrationActions.amendChildren,
    config.administrationActions.amendRespondents,
    config.administrationActions.amendOther,
    config.administrationActions.amendInternationalElement,
    config.administrationActions.amendAttendingHearing,
    config.administrationActions.draftStandardDirections,
  ]);
}).retry(1); //async processing in prev test
