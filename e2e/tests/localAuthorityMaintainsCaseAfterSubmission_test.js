const config = require('../config.js');

let caseId;

Feature('Case maintenance after submission');

Before(async (I, caseViewPage, submitApplicationEventPage) => {
  if (!caseId) {
    await I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
    await I.enterMandatoryFields();
    caseViewPage.goToNewActions(config.applicationActions.submitCase);
    submitApplicationEventPage.giveConsent();
    I.continueAndSubmit();

    // eslint-disable-next-line require-atomic-updates
    caseId = await I.grabTextFrom('.heading-h1');
    console.log(`Case ${caseId} has been submitted`);
  } else {
    await I.navigateToCaseDetails(caseId);
  }
});

Scenario('local authority uploads documents', (I, caseViewPage, uploadDocumentsEventPage) => {
  caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
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

Scenario('local authority uploads court bundle', (I, uploadDocumentsEventPage, submitApplicationEventPage, caseViewPage) => {
  caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
  uploadDocumentsEventPage.uploadCourtBundle(config.testFile);
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.seeDocument('Court bundle', 'mockFile.txt');
});
