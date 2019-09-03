const config = require('../config.js');

let caseId;
caseId = '1567521062611916';

Feature('Application draft (populated draft)');

Before(async (I) => {
  if (!caseId) {
    await I.logInAndCreateCase(config.gateKeeperEmail, config.gateKeeperPassword);

    // eslint-disable-next-line require-atomic-updates
    caseId = await I.grabTextFrom('.heading-h1');
    console.log(`Application draft ${caseId} has been created`);
  } else {
    await I.signIn(config.gateKeeperEmail, config.gateKeeperPassword);
    await I.navigateToCaseDetails(caseId);
  }
});

Scenario('gatekeeper enters allocation decision', (I, caseViewPage, enterAllocationDecisionEventPage) => {
  caseViewPage.goToNewActions(config.applicationActions.enterAllocationDecision);
  enterAllocationDecisionEventPage.selectAllocationDecision('Lay justices');
  enterAllocationDecisionEventPage.enterProposalReason('test');
  I.seeCheckAnswers('Give reason');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterAllocationDecision);
});
