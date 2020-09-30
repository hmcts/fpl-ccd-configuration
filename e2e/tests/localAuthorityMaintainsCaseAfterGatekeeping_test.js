const config = require('../config.js');
const uploadDocumentsHelper = require('../helpers/upload_case_documents_helper.js');
const gatekeeping = require('../fixtures/caseData/gatekeeping.json');

let caseId;

Feature('Case maintenance after gatekeeping');

BeforeSuite(async I => {
  caseId = await I.submitNewCaseWithData(gatekeeping);
  await I.signIn(config.swanseaLocalAuthorityUserOne);
});

Before(async I => await I.navigateToCaseDetails(caseId));

Scenario('local authority uploads documents', async (I, caseViewPage, uploadDocumentsEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
  uploadDocumentsHelper.uploadCaseDocuments(uploadDocumentsEventPage);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  uploadDocumentsHelper.assertCaseDocuments(I);
});

Scenario('local authority uploads court bundle', async (I, caseViewPage, uploadDocumentsEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
  I.seeElement(uploadDocumentsEventPage.documents.courtBundle);
  uploadDocumentsEventPage.uploadCourtBundle(config.testFile);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.seeDocument('Court bundle', 'mockFile.txt', '', '', 'Date and time uploaded', 'Uploaded by');
});
