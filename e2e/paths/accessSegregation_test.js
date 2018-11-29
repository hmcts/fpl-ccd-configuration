/* global xScenario */

const config = require('../config.js');

let caseId;

Feature('Cases visible only to respective local authority and admin').retry(2);

Before(async (I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseId = await I.grabTextFrom('.heading-medium');
  caseId = caseId.toString().replace('#', '');
  caseViewPage.goToNewActions(config.applicationActions.submitCase);
  I.click('Submit');
  I.click('Sign Out');
});

xScenario('Different user in the same local authority can see case created', (I, loginPage, caseListPage) => {
  loginPage.signIn(config.swanseaLocalAuthorityEmailUserTwo, config.localAuthorityPassword);
  caseListPage.changeStateFilter('Submitted');
  I.see(caseId);
});

xScenario('Different user in a different local authority cannot see case created', (I, loginPage, caseListPage) => {
  loginPage.signIn(config.hillingdonLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseListPage.changeStateFilter('Submitted');
  I.dontSee(caseId);
});

xScenario('HMCTS admin user can see the case', (I, loginPage) => {
  loginPage.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
  I.see(caseId);
});
