/*global process*/

const config = require('../config.js');
let caseId;
let caseUrl;
let baseUrl = process.env.URL || 'http://localhost:3451';

Feature('Login as hmcts admin').retry(2);

Before(async (I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseId = await I.grabTextFrom('.heading-medium');
  caseUrl = caseId.toString().replace('#', '').replace(/-/g, '');
  caseViewPage.goToNewActions(config.applicationActions.submitCase);
  I.click('.button[type=submit]');
  I.waitForElement('.tabs');
  I.signOut();
});

Scenario('HMCTS admin can login and add a FamilyMan case number to a submitted case', (I, caseViewPage, loginPage, caseListPage, enterFamilyManPage) => {
  loginPage.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
  I.navigateToUrl(baseUrl + '/case/PUBLICLAW/Shared_Storage_DRAFTType/' + caseUrl);
  I.see(caseId);
  caseViewPage.goToNewActions(config.addFamilyManCaseNumber);
  enterFamilyManPage.enterCaseID();
  I.continueAndSubmit();
  I.seeEventSubmissionConfirmation(config.addFamilyManCaseNumber);
});
