const config = require('../config.js');

let caseId;
let hillingdonCaseId;

Feature('Cases visible only to respective local authority and admin').retry(2);

Before(async (I, caseViewPage, submitApplicationPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseId = await I.grabTextFrom('.heading-h1');
  caseViewPage.goToNewActions(config.applicationActions.submitCase);
  submitApplicationPage.giveConsent();
  I.continueAndSubmit();
  I.signOut();
  I.logInAndCreateCase(config.hillingdonLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  hillingdonCaseId = await I.grabTextFrom('.heading-h1');
  caseViewPage.goToNewActions(config.applicationActions.submitCase);
  submitApplicationPage.giveConsent();
  I.continueAndSubmit();
  I.signOut();
});

Scenario('Different user in the same local authority can see case created', async (I, loginPage, caseViewPage, submitApplicationPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  let swanseaCaseId = await I.grabTextFrom('.heading-h1');
  caseViewPage.goToNewActions(config.applicationActions.submitCase);
  submitApplicationPage.giveConsent();
  I.continueAndSubmit();
  I.signOut();
  loginPage.signIn(config.swanseaLocalAuthorityEmailUserTwo, config.localAuthorityPassword);
  I.navigateToCaseDetails(swanseaCaseId);
  I.see(swanseaCaseId);
});

Scenario('Different user in a different local authority cannot see case created', (I, loginPage) => {
  loginPage.signIn(config.hillingdonLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  I.navigateToCaseDetails(caseId);
  I.seeInCurrentUrl('error');
});

Scenario('HMCTS admin user can see the case', (I, loginPage) => {
  loginPage.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
  I.navigateToCaseDetails(caseId);
  I.see(caseId);
});

Scenario('CAFCASS user can see the case', (I, loginPage) => {
  loginPage.signIn(config.cafcassEmail, config.cafcassPassword);
  I.navigateToCaseDetails(caseId);
  I.see(caseId);
});

Scenario('gatekeeper user can see submitted cases', (I, loginPage) => {
  loginPage.signIn(config.gateKeeperEmail, config.gateKeeperPassword);
  I.navigateToCaseDetails(caseId);
  I.see(caseId);
});

Scenario('Gatekeeper can login and see all cases across all courts', (I, loginPage) => {
  loginPage.signIn(config.gateKeeperEmail, config.gateKeeperPassword);
  I.navigateToCaseDetails(caseId);
  I.see(caseId);
  I.navigateToCaseDetails(hillingdonCaseId);
  I.see(hillingdonCaseId);
});
