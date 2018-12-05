const config = require('../config.js');

Feature('Upload Documents').retry(2);

Before((I) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
});

Scenario('Selecting social work chronology document to follow in the c110a application', (I, uploadDocumentsPage, caseViewPage) => {
  caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
  uploadDocumentsPage.selectSocialWorkChronologyToFollow();
  I.continueAndSubmit();
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);

  caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
  uploadDocumentsPage.selectSocialWorkChronologyToFollow(config.testFile);
  uploadDocumentsPage.uploadSocialWorkStatement(config.testFile);
  uploadDocumentsPage.uploadSocialWorkAssessment(config.testFile);
  uploadDocumentsPage.uploadCarePlan(config.testFile);
  uploadDocumentsPage.uploadAdditionalDocuments(config.testFile);
  I.continueAndSubmit();
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);

  caseViewPage.selectTab(caseViewPage.tabs.evidence);
  I.seeDocument('Social work chronology', '', 'To follow', 'mock reason');
  I.seeDocument('Social work statement', 'mockFile.txt', 'Attached');
  I.seeDocument('Social work assessment', 'mockFile.txt', 'Attached');
  I.seeDocument('Care plan', 'mockFile.txt', 'Attached');
});

