const config = require('../config.js');

let caseId;

Feature('HMCTS admin upload standard directions');

Before(async (I, caseViewPage, loginPage, submitApplicationPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseId = await I.grabTextFrom('.heading-medium');
  caseViewPage.goToNewActions(config.applicationActions.submitCase);
  submitApplicationPage.giveConsent();
  I.click('Continue');
  I.click('Submit');
  I.signOut();
  loginPage.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
  I.navigateToCaseDetails(caseId);
});

Scenario('HMCTS admin upload standard directions and see them in evidence tab', (I, caseViewPage, uploadDocumentsPage) => {
  caseViewPage.goToNewActions(config.standardDirections);
  uploadDocumentsPage.uploadStandardDirections(config.testFile);
  I.click('Continue');
  I.click('Submit');
  I.seeEventSubmissionConfirmation(config.standardDirections);
  caseViewPage.selectTab(caseViewPage.tabs.evidence);
  I.see('mockFile.txt');
});

Scenario('Local authority can see standard directions in evidence tab', (I, caseViewPage, uploadDocumentsPage, loginPage) => {
  caseViewPage.goToNewActions(config.standardDirections);
  uploadDocumentsPage.uploadStandardDirections(config.testFile);
  I.click('Continue');
  I.click('Submit');
  I.signOut();
  loginPage.signIn(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  I.navigateToCaseDetails(caseId);
  caseViewPage.selectTab(caseViewPage.tabs.evidence);
  I.see('mockFile.txt');
});
