/* global xScenario */

const config = require('../config.js');
let caseId;

Feature('Login as hmcts admin').retry(2);

Before(async (I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseId = await I.grabTextFrom('h2');
  caseViewPage.goToNewActions(config.applicationActions.submitCase);
  I.click('.button[type=submit]');
  I.waitForElement('.tabs');
  I.signOut();
});

xScenario('HMCTS admin can login and add a FamilyMan case number to a submitted case', (I, caseViewPage, loginPage, caseListPage, enterFamilyManPage) => {
  loginPage.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
  caseListPage.openExistingCase(caseId);
  I.see(caseId);
  caseViewPage.goToNewActions(config.addFamilyManCaseNumber);
  enterFamilyManPage.enterCaseID();
  I.continueAndSubmit();
  I.seeEventSubmissionConfirmation(config.addFamilyManCaseNumber);
});
