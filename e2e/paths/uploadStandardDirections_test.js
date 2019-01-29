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

Scenario('HMCTS admin upload standard directions with other documents and see them in documents tab', (I, caseViewPage, standardDirectionsPage) => {
  caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
  standardDirectionsPage.uploadStandardDirections(config.testFile);
  standardDirectionsPage.uploadAdditionalDocuments(config.testFile);
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.see('mockFile.txt');
  I.seeAnswerInTab('1', 'Other documents 1', 'Document title', 'Document 1');
  I.seeAnswerInTab('2', 'Other documents 1', 'Upload a file', 'mockFile.txt');
  I.seeAnswerInTab('1', 'Other documents 2', 'Document title', 'Document 2');
  I.seeAnswerInTab('2', 'Other documents 2', 'Upload a file', 'mockFile.txt');
});

Scenario('Local authority can see standard directions in documents tab', (I, caseViewPage, standardDirectionsPage, loginPage) => {
  caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
  standardDirectionsPage.uploadStandardDirections(config.testFile);
  standardDirectionsPage.uploadAdditionalDocuments(config.testFile);
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);
  I.signOut();
  loginPage.signIn(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  I.navigateToCaseDetails(caseId);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.see( 'mockFile.txt');
  I.seeAnswerInTab('1', 'Other documents 1', 'Document title', 'Document 1');
  I.seeAnswerInTab('2', 'Other documents 1', 'Upload a file', 'mockFile.txt');
  I.seeAnswerInTab('1', 'Other documents 2', 'Document title', 'Document 2');
  I.seeAnswerInTab('2', 'Other documents 2', 'Upload a file', 'mockFile.txt');
});
