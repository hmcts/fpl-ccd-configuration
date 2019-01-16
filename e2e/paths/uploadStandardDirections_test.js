const config = require('../config.js');

let caseId;

Feature('HMCTS admin upload standard directions');

Before(async (I, caseViewPage, loginPage, submitApplicationPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseId = await I.grabTextFrom('.heading-medium');
  caseViewPage.goToNewActions(config.applicationActions.submitCase);
  submitApplicationPage.giveConsent();
  I.continueAndSubmit();
  I.signOut();
  loginPage.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
  I.navigateToCaseDetails(caseId);
});

Scenario('HMCTS admin upload standard directions and see them in documents tab', (I, caseViewPage, uploadDocumentsPage) => {
  caseViewPage.goToNewActions(config.standardDirections);
  uploadDocumentsPage.uploadStandardDirections(config.testFile);
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.standardDirections);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.see('mockFile.txt');
});

Scenario('Local authority can see standard directions in documents tab', (I, caseViewPage, uploadDocumentsPage, loginPage) => {
  caseViewPage.goToNewActions(config.standardDirections);
  uploadDocumentsPage.uploadStandardDirections(config.testFile);
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.standardDirections);
  I.signOut();
  loginPage.signIn(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  I.navigateToCaseDetails(caseId);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.see('mockFile.txt');
});
