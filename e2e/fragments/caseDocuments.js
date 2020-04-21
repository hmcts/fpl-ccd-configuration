const { I } = inject();
const config = require('../config.js');

module.exports = {
  uploadMandatoryDocuments(uploadDocumentsEventPage) {
    uploadDocumentsEventPage.selectSocialWorkChronologyToFollow(config.testFile);
    uploadDocumentsEventPage.selectSocialWorkStatementIncludedInSWET();
    uploadDocumentsEventPage.uploadSocialWorkAssessment(config.testFile);
    uploadDocumentsEventPage.uploadCarePlan(config.testFile);
    uploadDocumentsEventPage.uploadSWET(config.testFile);
    uploadDocumentsEventPage.uploadThresholdDocument(config.testFile);
    uploadDocumentsEventPage.uploadChecklistDocument(config.testFile);
    uploadDocumentsEventPage.uploadAdditionalDocuments(config.testFile);
  },

  assertMandatoryDocuments() {
    I.seeDocument('Social work chronology', '', 'To follow', 'mock reason');
    I.seeDocument('Social work statement and genogram', 'mockFile.txt', 'Attached');
    I.seeDocument('Social work assessment', 'mockFile.txt', 'Attached');
    I.seeDocument('Care plan', 'mockFile.txt', 'Attached');
    I.seeDocument('Social work evidence template (SWET)', 'mockFile.txt', 'Attached');
    I.seeDocument('Threshold document', 'mockFile.txt', 'Attached');
    I.seeDocument('Checklist document', 'mockFile.txt', 'Attached');
  },
};
