const config = require('../config.js');

const uploadCaseDocuments = (uploadDocumentsEventPage, uploadSWET=false) => {
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
};

const assertCaseDocuments = (I, hasUploadedSWET=true) => {
  I.seeDocument('Social work chronology', '', 'To follow', 'mock reason', 'Date and time uploaded', 'Uploaded by');
  I.seeDocument('Social work statement and genogram', 'mockFile.txt', 'Attached', '', 'Date and time uploaded', 'Uploaded by');
  I.seeDocument('Social work assessment', 'mockFile.txt', 'Attached', '', 'Date and time uploaded', 'Uploaded by');
  I.seeDocument('Care plan', 'mockFile.txt', 'Attached', '', 'Date and time uploaded', 'Uploaded by');

  if (hasUploadedSWET) {
    I.seeDocument('Social work evidence template (SWET)', 'mockFile.txt', 'Attached', '', 'Date and time uploaded', 'Uploaded by');
  } else {
    I.seeDocument('Social work evidence template (SWET)', '', 'Not Required');
  }

  I.seeDocument('Threshold document', 'mockFile.txt', 'Attached', '', 'Date and time uploaded', 'Uploaded by');
  I.seeDocument('Checklist document', 'mockFile.txt', 'Attached', '', 'Date and time uploaded', 'Uploaded by');
};

module.exports = {
  uploadCaseDocuments, assertCaseDocuments,
};
