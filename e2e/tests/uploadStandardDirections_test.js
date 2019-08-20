const config = require('../config.js');

let caseId;

Feature('HMCTS admin upload standard directions');

Before(async (I, caseViewPage, loginPage, submitApplicationEventPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseId = await I.grabTextFrom('.heading-h1');
  caseViewPage.goToNewActions(config.applicationActions.submitCase);
  submitApplicationEventPage.giveConsent();
  I.continueAndSubmit();
  I.signOut();
  loginPage.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
  I.navigateToCaseDetails(caseId);
});

Scenario('HMCTS admin upload standard directions with other documents and see them in documents tab', (I, caseViewPage, uploadStandardDirectionsDocumentEventPage) => {
  caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
  uploadStandardDirectionsDocumentEventPage.uploadStandardDirections(config.testFile);
  uploadStandardDirectionsDocumentEventPage.uploadAdditionalDocuments(config.testFile);
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.see('mockFile.txt');
  I.seeAnswerInTab('1', 'Other documents 1', 'Document name', 'Document 1');
  I.seeAnswerInTab('2', 'Other documents 1', 'Upload a file', 'mockFile.txt');
  I.seeAnswerInTab('1', 'Other documents 2', 'Document name', 'Document 2');
  I.seeAnswerInTab('2', 'Other documents 2', 'Upload a file', 'mockFile.txt');
});

Scenario('Local authority can see standard directions in documents tab', (I, caseViewPage, uploadStandardDirectionsDocumentEventPage, loginPage) => {
  caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
  uploadStandardDirectionsDocumentEventPage.uploadStandardDirections(config.testFile);
  uploadStandardDirectionsDocumentEventPage.uploadAdditionalDocuments(config.testFile);
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);
  I.signOut();
  loginPage.signIn(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  I.navigateToCaseDetails(caseId);
  I.waitForElement(caseViewPage.tabs.documents, 5);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.see( 'mockFile.txt');
  I.seeAnswerInTab('1', 'Other documents 1', 'Document name', 'Document 1');
  I.seeAnswerInTab('2', 'Other documents 1', 'Upload a file', 'mockFile.txt');
  I.seeAnswerInTab('1', 'Other documents 2', 'Document name', 'Document 2');
  I.seeAnswerInTab('2', 'Other documents 2', 'Upload a file', 'mockFile.txt');
});
