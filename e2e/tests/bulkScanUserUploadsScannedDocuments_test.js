const config = require('../config.js');
const scannedDocument = require('../fixtures/scannedDocument.js');

let caseId;

Feature('Uploading bulk scan document');

Before(async (I, caseViewPage, submitApplicationEventPage, handleSupplementaryEvidenceEventPage, attachScannedDocsEventPage) => {
  if (!caseId) {
    await I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
    await I.enterMandatoryFields({multipleChildren: false});
    await caseViewPage.goToNewActions(config.applicationActions.submitCase);
    submitApplicationEventPage.giveConsent();
    await I.completeEvent('Submit');

    // eslint-disable-next-line require-atomic-updates
    caseId = await I.grabTextFrom('.heading-h1');
    console.log(`Case ${caseId} has been submitted`);

    I.signOut();
    await I.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
    await I.navigateToCaseDetails(caseId);

    await caseViewPage.goToNewActions(config.administrationActions.bulkScan);
    attachScannedDocsEventPage.enterScannedDocument(scannedDocument, config.testFile);
    await I.click('Continue');
    handleSupplementaryEvidenceEventPage.handleSupplementaryEvidence();
    await I.completeEvent('Submit');
    I.seeEventSubmissionConfirmation(config.administrationActions.bulkScan);
  }
});

Scenario('HMCTS admin can see Documents scanned in with Bulk Scan', async (I, caseViewPage) => {
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.seeAnswerInTab(1, 'Scanned Documents 1', 'Document type', scannedDocument.type);
  I.seeAnswerInTab(2, 'Scanned Documents 1', 'Document subtype', scannedDocument.subtype);
  I.seeAnswerInTab(3, 'Scanned Documents 1', 'Document url', 'mockFile.txt');
  I.seeAnswerInTab(4, 'Scanned Documents 1', 'Document control number', scannedDocument.controlNumber);
  I.seeAnswerInTab(5, 'Scanned Documents 1', 'File name', scannedDocument.fileName);
  I.seeAnswerInTab(6, 'Scanned Documents 1', 'Scanned date', '1 Jan 2050, 12:00:00 PM');
  I.seeAnswerInTab(7, 'Scanned Documents 1', 'Delivery date', '1 Jan 2050, 12:00:00 PM');
  I.seeAnswerInTab(8, 'Scanned Documents 1', 'Exception record reference', scannedDocument.exceptionRecordReference);
});
