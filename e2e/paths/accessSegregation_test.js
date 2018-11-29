const config = require('../config.js');

let caseId;

Feature('Cases visible only to respective local authority and admin').retry(2);

Before(async (I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailKurt, config.localAuthorityPassword);
  caseId = await I.grabTextFrom('.heading-medium');
  caseId = caseId.toString().replace('#', '');
  caseViewPage.goToNewActions(config.applicationActions.submitCase);
  I.click('Submit');
  I.click('Sign Out');
});

Scenario('Different user in the same local authority can see case created', (I, loginPage, caseListPage) => {
  loginPage.signIn(config.swanseaLocalAuthorityEmailDamian, config.localAuthorityPassword);
  caseListPage.changeStateFilter('Submitted');
  I.see(caseId);
});

Scenario('Different user in a different local authority cannot see case created', (I, loginPage, caseListPage) => {
  loginPage.signIn(config.hillingdonLocalAuthorityEmailSam, config.localAuthorityPassword);
  caseListPage.changeStateFilter('Submitted');
  I.dontSee(caseId);
});

Scenario('HMCTS admin user can see the case', (I, loginPage) => {
  loginPage.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
  I.see(caseId);
});
