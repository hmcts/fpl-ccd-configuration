const config = require('../config.js');

Feature('Upload Documents');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
});

Scenario('Selecting social work chronology document to follow in the c110a application', (I, uploadDocumentsEventPage, caseViewPage) => {
  uploadDocumentsEventPage.selectSocialWorkChronologyToFollow();
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.seeDocument('Social work chronology', '', 'To follow', 'mock reason');
});

Scenario('Uploading all files in the c110a application', (I, uploadDocumentsEventPage, caseViewPage) => {
  uploadDocumentsEventPage.selectSocialWorkChronologyToFollow(config.testFile);
  uploadDocumentsEventPage.uploadSocialWorkStatement(config.testFile);
  uploadDocumentsEventPage.uploadSocialWorkAssessment(config.testFile);
  uploadDocumentsEventPage.uploadCarePlan(config.testFile);
  uploadDocumentsEventPage.uploadSWET(config.testFile);
  uploadDocumentsEventPage.uploadThresholdDocument(config.testFile);
  uploadDocumentsEventPage.uploadChecklistDocument(config.testFile);
  uploadDocumentsEventPage.uploadAdditionalDocuments(config.testFile);
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.seeDocument('Social work chronology', '', 'To follow', 'mock reason');
  I.seeDocument('Social work statement and genogram', 'mockFile.txt', 'Attached');
  I.seeDocument('Social work assessment', 'mockFile.txt', 'Attached');
  I.seeDocument('Care plan', 'mockFile.txt', 'Attached');
  I.seeDocument('Social work evidence template (SWET)', 'mockFile.txt', 'Attached');
  I.seeDocument('Threshold document', 'mockFile.txt', 'Attached');
  I.seeDocument('Checklist document', 'mockFile.txt', 'Attached');
});

Scenario('As a local authority I have the ability to upload a document after submission of a case', async (I, uploadDocumentsEventPage, submitApplicationEventPage, caseViewPage) => {
  uploadDocumentsEventPage.selectSocialWorkChronologyToFollow(config.testFile);
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);
  await I.enterMandatoryFields();
  caseViewPage.goToNewActions(config.applicationActions.submitCase);
  submitApplicationEventPage.giveConsent();
  I.continueAndSubmit();
  caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
  uploadDocumentsEventPage.uploadSocialWorkAssessment(config.testFile);
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.seeDocument('Social work assessment', 'mockFile.txt', 'Attached');
});

Scenario('Ability for a local authority to upload court bundle only after case is submitted', async (I, uploadDocumentsEventPage, submitApplicationEventPage, caseViewPage) => {
  I.dontSee('Court bundle');
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);
  await I.enterMandatoryFields();
  caseViewPage.goToNewActions(config.applicationActions.submitCase);
  submitApplicationEventPage.giveConsent();
  I.continueAndSubmit();
  caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
  uploadDocumentsEventPage.uploadCourtBundle(config.testFile);
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.see('mockFile.txt');
});
