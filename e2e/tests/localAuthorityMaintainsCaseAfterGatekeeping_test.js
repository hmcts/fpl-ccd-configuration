const config = require('../config.js');
const uploadDocumentsHelper = require('../helpers/upload_case_documents_helper.js');

let caseId;

Feature('Case maintenance after gatekeeping');

Before(async (I, caseViewPage, submitApplicationEventPage, enterFamilyManCaseNumberEventPage, sendCaseToGatekeeperEventPage) => {
  if (!caseId) {
    // eslint-disable-next-line require-atomic-updates
    caseId = await I.submitNewCaseWithData();

    //hmcts login, enter case number and send to gatekeeper
    await I.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
    await I.navigateToCaseDetails(caseId);
    await caseViewPage.goToNewActions(config.administrationActions.addFamilyManCaseNumber);
    enterFamilyManCaseNumberEventPage.enterCaseID();
    await I.completeEvent('Save and continue');
    await caseViewPage.goToNewActions(config.administrationActions.sendToGatekeeper);
    sendCaseToGatekeeperEventPage.enterEmail();
    await I.completeEvent('Save and continue');
    I.seeEventSubmissionConfirmation(config.administrationActions.sendToGatekeeper);
    I.signOut();

    await I.signIn(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  }
  await I.navigateToCaseDetails(caseId);
});

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
  I.seeDocument('Court bundle', 'mockFile.txt');
});
