const config = require('../config.js');
let caseId;

Feature('Login as hmcts admin').retry(2);

Before(async (I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
  caseId = await I.grabTextFrom('h2');
  caseViewPage.goToNewActions(config.applicationActions.submitCase);
  I.click('.button[type=submit]');
  I.waitForElement('.tabs');
  I.signOut();
});

Scenario('HMCTS admin can login and select a submitted case', (I, loginPage, caseListPage) => {
  loginPage.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
  caseListPage.openExistingCase(caseId);
  I.see(caseId);
});
