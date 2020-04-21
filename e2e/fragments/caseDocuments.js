const { I } = inject();
const config = require('../config.js');

module.exports = {
  uploadMandatoryDocuments(uploadDocumentsEventPage, uploadSWET=true) {
    uploadDocumentsEventPage.selectSocialWorkChronologyToFollow(config.testFile);

    if (uploadSWET) {
      uploadDocumentsEventPage.selectSocialWorkStatementIncludedInSWET();
    } else {
      uploadDocumentsEventPage.uploadSocialWorkStatementAndGenogram(config.testFile);
    }

    uploadDocumentsEventPage.uploadSocialWorkAssessment(config.testFile);
    uploadDocumentsEventPage.uploadCarePlan(config.testFile);

    if (uploadSWET) {
      uploadDocumentsEventPage.uploadSWET(config.testFile);
    } else {
      uploadDocumentsEventPage.selectSWETAsNotRequired();
    }

    uploadDocumentsEventPage.uploadThresholdDocument(config.testFile);
    uploadDocumentsEventPage.uploadChecklistDocument(config.testFile);
  },

  assertMandatoryDocuments(hasUploadedSWET=true) {
    I.seeDocument('Social work chronology', '', 'To follow', 'mock reason');
    I.seeDocument('Social work statement and genogram', 'mockFile.txt', 'Attached');
    I.seeDocument('Social work assessment', 'mockFile.txt', 'Attached');
    I.seeDocument('Care plan', 'mockFile.txt', 'Attached');

    if (hasUploadedSWET) {
      I.seeDocument('Social work evidence template (SWET)', 'mockFile.txt', 'Attached');
    } else {
      I.seeDocument('Social work evidence template (SWET)', '', 'Not Required');
    }

    I.seeDocument('Threshold document', 'mockFile.txt', 'Attached');
    I.seeDocument('Checklist document', 'mockFile.txt', 'Attached');
  },
};
