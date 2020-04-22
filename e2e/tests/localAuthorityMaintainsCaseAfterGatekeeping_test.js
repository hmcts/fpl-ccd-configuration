const config = require('../config.js');

let caseId;

Feature('Case maintenance after gatekeeping');

Before(async (I, caseViewPage, submitApplicationEventPage, enterFamilyManCaseNumberEventPage, sendCaseToGatekeeperEventPage) => {
  if (!caseId) {
    await I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
    await I.enterMandatoryFields();
    await caseViewPage.goToNewActions(config.applicationActions.submitCase);
    submitApplicationEventPage.giveConsent();
    await I.completeEvent('Submit');

    // eslint-disable-next-line require-atomic-updates
    caseId = await I.grabTextFrom('.heading-h1');
    console.log(`Case ${caseId} has been submitted`);

    I.signOut();

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
  uploadDocumentsEventPage.uploadCaseDocuments(config);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  uploadDocumentsEventPage.assertCaseDocuments();
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
