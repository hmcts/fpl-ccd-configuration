const config = require('../config.js');
const gatekeepingCaseData = require('../fixtures/caseData/gatekeepingNoAllocatedJudge.json');

let caseId;

Feature('Gatekeeper Case administration after gatekeeping');

async function setupScenario(I) {
  if (!caseId) { caseId = await I.submitNewCaseWithData(gatekeepingCaseData); }
  await I.navigateToCaseDetailsAs(config.gateKeeperUser, caseId);
}

Scenario('Gatekeeper notifies another gatekeeper with a link to the case', async ({I, caseViewPage, notifyGatekeeperEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.administrationActions.notifyGatekeeper);
  await notifyGatekeeperEventPage.enterEmail('gatekeeper@mailnesia.com');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.notifyGatekeeper);
});

Scenario('Gatekeeper adds allocated judge', async ({I, caseViewPage, allocatedJudgeEventPage}) => {
  await setupScenario(I);
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
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.applicationActions.enterAllocationDecision);
  enterAllocationDecisionEventPage.selectCorrectLevelOfJudge('Yes');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterAllocationDecision);

  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeInTab(['Allocation decision', 'Which level of judge is needed for this case?'], 'District Judge');
});

Scenario('Gatekeeper enters allocation decision', async ({I, caseViewPage, enterAllocationDecisionEventPage}) => {
  await setupScenario(I);
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
