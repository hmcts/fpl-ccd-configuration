const config = require('../config.js');

let caseId;

Feature('Send notification to gatekeeper');

Before(async (I, caseViewPage, loginPage, submitApplicationPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseId = await I.grabTextFrom('.heading-h1');
  I.enterMandatoryFields();
  caseViewPage.goToNewActions(config.applicationActions.submitCase);
  submitApplicationPage.giveConsent();
  I.continueAndSubmit();
  I.signOut();
  loginPage.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
  I.navigateToCaseDetails(caseId);
});

Scenario('HMCTS admin can enter and send email to gatekeeper', (I, caseViewPage, sendToGatekeeperPage) => {
  caseViewPage.goToNewActions(config.sendToGatekeeper);
  sendToGatekeeperPage.enterEmail();
  I.click('Continue');
  I.click('Save and continue');
  I.seeEventSubmissionConfirmation(config.sendToGatekeeper);
});
