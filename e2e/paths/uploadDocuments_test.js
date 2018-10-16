const config = require('../config.js');

Feature('Upload Documents');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
  caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
});

Scenario('Completing half the Annex documents form by selecting social work chronology document to follow in the c110a application', (I, uploadDocumentsPage, caseViewPage) => {
  uploadDocumentsPage.socialWorkChronologyToFollow();
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);
  caseViewPage.selectTab('evidence');
  I.seeDocument('Social work chronology', '', 'To follow', 'mock reason');
});

Scenario('Complete the Annex documents part of the form by uploading all files in the c110a application', (I, uploadDocumentsPage, caseViewPage) => {
  uploadDocumentsPage.socialWorkChronologyToFollow(config.testFile);
  uploadDocumentsPage.uploadSocialWorkStatement(config.testFile);
  uploadDocumentsPage.uploadSocialWorkAssessment(config.testFile);
  uploadDocumentsPage.uploadCarePlan(config.testFile);
  uploadDocumentsPage.uploadAdditionalDocuments(config.testFile);
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);
  caseViewPage.selectTab('evidence');
  I.seeDocument('Social work chronology', '', 'To follow', 'mock reason');
  I.seeDocument('Social work statement', 'mockFile.txt', 'Attached');
  I.seeDocument('Social work assessment', 'mockFile.txt', 'Attached');
  I.seeDocument('Care plan', 'mockFile.txt', 'Attached');
});
