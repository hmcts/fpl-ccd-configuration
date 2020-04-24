const config = require('../config.js');

let caseId;

Feature('Gatekeeper makes allocation decision');

BeforeSuite(async (I, caseViewPage, submitApplicationEventPage, enterFamilyManCaseNumberEventPage, sendCaseToGatekeeperEventPage) => {
  caseId = await I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  await I.enterAllocationProposal();
  await I.populateCaseWithMandatoryFields(caseId);
  await I.signIn(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  await I.submitCase(caseId);

  console.log(`Case ${caseId} has been submitted`);
  I.signOut();

  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);

  await caseViewPage.goToNewActions(config.administrationActions.addFamilyManCaseNumber);
  enterFamilyManCaseNumberEventPage.enterCaseID();
  await I.completeEvent('Save and continue');
  await caseViewPage.goToNewActions(config.administrationActions.sendToGatekeeper);
  sendCaseToGatekeeperEventPage.enterEmail();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.sendToGatekeeper);

  await I.navigateToCaseDetailsAs(config.gateKeeperUser, caseId);
});

Before(async I => await I.navigateToCaseDetails(caseId));

Scenario('gatekeeper enters allocation decision with incorrect allocation proposal', async (I, caseViewPage, enterAllocationDecisionEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterAllocationDecision);
  enterAllocationDecisionEventPage.selectCorrectLevelOfJudge('No');
  enterAllocationDecisionEventPage.selectAllocationDecision('District judge');
  enterAllocationDecisionEventPage.enterProposalReason('test');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterAllocationDecision);
  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeInTab(['Allocation decision', 'Which level of judge is needed for this case?'], 'District judge');
  I.seeInTab(['Allocation decision', 'Give reason'], 'test');
});

Scenario('gatekeeper enters allocation decision with correct allocation proposal', async (I, caseViewPage, enterAllocationDecisionEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterAllocationDecision);
  enterAllocationDecisionEventPage.selectCorrectLevelOfJudge('Yes');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterAllocationDecision);
  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
});
