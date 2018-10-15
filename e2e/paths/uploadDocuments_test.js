const config = require('../config.js');

Feature('Upload Documents');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
  caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
});

Scenario('Selecting to follow for a document produces a textarea', (I, uploadDocumentsPage) => {
  I.selectOption(uploadDocumentsPage.fields.socialWorkChronologyStatus, 'To follow');
  I.fillField(uploadDocumentsPage.fields.socialWorkChronologyReason, 'mock reason');
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);
});

Scenario('All documents are able to be uploaded', (I, uploadDocumentsPage) => {
  uploadDocumentsPage.uploadSocialWorkChronology(config.testFile);
  uploadDocumentsPage.uploadSocialWorkStatement(config.testFile);
  uploadDocumentsPage.uploadSocialWorkAssessment(config.testFile);
  uploadDocumentsPage.uploadCarePlan(config.testFile);
  uploadDocumentsPage.uploadAdditionalDocuments(config.testFile);
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);
});
