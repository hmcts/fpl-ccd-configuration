const config = require('../config.js');
let caseId;

Feature('Login as hmcts admin');

Before(async (I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
  caseId = await I.grabTextFrom('h2');
  caseViewPage.goToNewActions(config.applicationActions.submitCase);
  I.click('.button[type=submit]');
  I.waitForElement('.tabs');
  I.signOut();
});

Scenario('HMCTS admin can login and add a FamilyMan case number to a submitted case', (I, caseViewPage, loginPage, caseListPage, enterFamilyManPage) => {
  loginPage.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
  caseListPage.openExistingCase(caseId);
  I.see(caseId);
  caseViewPage.goToNewActions(config.addFamilyManCaseNumber);
  enterFamilyManPage.enterCaseID();
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.addFamilyManCaseNumber);
});
