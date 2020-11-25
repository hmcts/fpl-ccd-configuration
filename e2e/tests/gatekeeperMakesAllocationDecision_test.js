const config = require('../config.js');
const gatekeeping = require('../fixtures/caseData/gatekeeping.json');

let caseId;

Feature('Gatekeeper makes allocation decision');

BeforeSuite(async ({I}) => {
  caseId = await I.submitNewCaseWithData(gatekeeping);

  await I.signIn(config.gateKeeperUser);
});

Before(async ({I}) => await I.navigateToCaseDetails(caseId));

Scenario('gatekeeper enters allocation decision with incorrect allocation proposal', async ({I, caseViewPage, enterAllocationDecisionEventPage}) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterAllocationDecision);
  enterAllocationDecisionEventPage.selectCorrectLevelOfJudge('No');
  enterAllocationDecisionEventPage.selectAllocationDecision('District judge');
  enterAllocationDecisionEventPage.enterProposalReason('test');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterAllocationDecision);
  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeInTab(['Allocation decision', 'Which level of judge is needed for this case?'], 'District Judge');
  I.seeInTab(['Allocation decision', 'Give reason'], 'test');
});

Scenario('gatekeeper enters allocation decision with correct allocation proposal', async ({I, caseViewPage, enterAllocationDecisionEventPage}) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterAllocationDecision);
  enterAllocationDecisionEventPage.selectCorrectLevelOfJudge('Yes');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterAllocationDecision);
  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
});
