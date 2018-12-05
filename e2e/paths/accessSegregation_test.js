const config = require('../config.js');

let caseId;

Feature('Cases visible only to respective local authority and admin');

Before(async (I) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseId = await I.grabTextFrom('.heading-medium');
});

Scenario('Different user in the same local authority can see case created', async (I, loginPage, caseViewPage) => {
  caseViewPage.goToNewActions(config.applicationActions.submitCase);
  I.click('Submit');
  I.signOut();

  loginPage.signIn(config.swanseaLocalAuthorityEmailUserTwo, config.localAuthorityPassword);
  I.navigateToCaseDetails(caseId);
  I.see(caseId);
  I.signOut();

  loginPage.signIn(config.hillingdonLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  I.navigateToCaseDetails(caseId);
  I.seeInCurrentUrl('error');
  I.signOut();

  loginPage.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
  I.navigateToCaseDetails(caseId);
  I.see(caseId);
  I.signOut();

  loginPage.signIn(config.cafcassEmail, config.cafcassPassword);
  I.navigateToCaseDetails(caseId);
  I.see(caseId);
});
