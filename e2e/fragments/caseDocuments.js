const config = require('../config.js');

module.exports = {
  uploadDocuments() {
    return async (I, caseViewPage, uploadDocumentsEventPage) => {
      await caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
      uploadDocumentsEventPage.selectSocialWorkChronologyToFollow(config.testFile);
      uploadDocumentsEventPage.uploadSocialWorkStatement(config.testFile);
      uploadDocumentsEventPage.uploadSocialWorkAssessment(config.testFile);
      uploadDocumentsEventPage.uploadCarePlan(config.testFile);
      uploadDocumentsEventPage.uploadSWET(config.testFile);
      uploadDocumentsEventPage.uploadThresholdDocument(config.testFile);
      uploadDocumentsEventPage.uploadChecklistDocument(config.testFile);
      uploadDocumentsEventPage.uploadAdditionalDocuments(config.testFile);
      await I.completeEvent('Save and continue');
      I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);
      caseViewPage.selectTab(caseViewPage.tabs.documents);
      I.seeDocument('Social work chronology', '', 'To follow', 'mock reason');
      I.seeDocument('Social work statement and genogram', 'mockFile.txt', 'Attached');
      I.seeDocument('Social work assessment', 'mockFile.txt', 'Attached');
      I.seeDocument('Care plan', 'mockFile.txt', 'Attached');
      I.seeDocument('Social work evidence template (SWET)', 'mockFile.txt', 'Attached');
      I.seeDocument('Threshold document', 'mockFile.txt', 'Attached');
      I.seeDocument('Checklist document', 'mockFile.txt', 'Attached');
    };
  },

  uploadCourtBundle() {
    return async (I, uploadDocumentsEventPage, submitApplicationEventPage, caseViewPage) => {
      await caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
      uploadDocumentsEventPage.uploadCourtBundle(config.testFile);
      await I.completeEvent('Save and continue');
      I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);
      caseViewPage.selectTab(caseViewPage.tabs.documents);
      I.seeDocument('Court bundle', 'mockFile.txt');
    };
  },
};
