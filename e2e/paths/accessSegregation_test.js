const config = require('../config.js');

let caseId;

Feature('Cases visible only to respective local authority and admin').retry(2);

Before(async (I, caseViewPage, submitApplicationPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseId = await I.grabTextFrom('.heading-medium');
  caseViewPage.goToNewActions(config.applicationActions.submitCase);
  submitApplicationPage.giveConsent();
  I.continueAndSubmit();
  I.signOut();
});

Scenario('Different user in the same local authority can see case created', async (I, loginPage) => {
  loginPage.signIn(config.swanseaLocalAuthorityEmailUserTwo, config.localAuthorityPassword);
  I.navigateToCaseDetails(caseId);
  I.see(caseId);
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
