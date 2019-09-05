const config = require('../config.js');

let caseId;

Feature('Application draft (populated draft)');

Before(async (I, caseViewPage, submitApplicationEventPage) => {
  if (!caseId) {
    await I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
    await I.enterMandatoryFields();
    caseViewPage.goToNewActions(config.applicationActions.submitCase);
    submitApplicationEventPage.giveConsent();
    I.continueAndSubmit();

    // eslint-disable-next-line require-atomic-updates
    caseId = await I.grabTextFrom('.heading-h1');
    console.log(`Case ${caseId} has been submitted`);

    I.signOut();
    await I.signIn(config.gateKeeperEmail, config.gateKeeperPassword);
  }
  await I.navigateToCaseDetails(caseId);
});

Scenario('gatekeeper enters allocation decision', (I, caseViewPage, enterAllocationDecisionEventPage) => {
  caseViewPage.goToNewActions(config.applicationActions.enterAllocationDecision);
  enterAllocationDecisionEventPage.selectAllocationDecision('Lay justices');
  enterAllocationDecisionEventPage.enterProposalReason('test');
  I.seeCheckAnswers('Give reason');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterAllocationDecision);
});
