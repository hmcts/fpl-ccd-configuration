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
  const uploadedByAndDateTimeText = '\'Date and time uploaded\', \'Uploaded by\'';
  I.seeDocument('Social work chronology', '', 'To follow', 'mock reason', uploadedByAndDateTimeText);
  I.seeDocument('Social work statement and genogram', 'mockFile.txt', 'Attached', uploadedByAndDateTimeText);
  I.seeDocument('Social work assessment', 'mockFile.txt', 'Attached', uploadedByAndDateTimeText);
  I.seeDocument('Care plan', 'mockFile.txt', 'Attached', uploadedByAndDateTimeText);

  if (hasUploadedSWET) {
    I.seeDocument('Social work evidence template (SWET)', 'mockFile.txt', 'Attached', uploadedByAndDateTimeText);
  } else {
    I.seeDocument('Social work evidence template (SWET)', '', 'Not Required', uploadedByAndDateTimeText);
  }

  I.seeDocument('Threshold document', 'mockFile.txt', 'Attached', uploadedByAndDateTimeText);
  I.seeDocument('Checklist document', 'mockFile.txt', 'Attached', uploadedByAndDateTimeText);
};

module.exports = {
  uploadCaseDocuments, assertCaseDocuments,
};
