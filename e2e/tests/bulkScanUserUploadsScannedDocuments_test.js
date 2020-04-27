const config = require('../config.js');
const scannedDocument = require('../fixtures/scannedDocument.js');

let caseId;

Feature('Uploading bulk scan document');

BeforeSuite(async (I, caseViewPage, submitApplicationEventPage, handleSupplementaryEvidenceEventPage, attachScannedDocsEventPage) => {
  caseId = await I.logInAndCreateCase(config.swanseaLocalAuthorityUserOne);
  await I.enterMandatoryFields({multipleChildren: false});
  await caseViewPage.goToNewActions(config.applicationActions.submitCase);
  submitApplicationEventPage.giveConsent();
  await I.completeEvent('Submit');

  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await caseViewPage.goToNewActions(config.administrationActions.bulkScan);
  attachScannedDocsEventPage.enterScannedDocument(scannedDocument, config.testFile);
  await I.click('Continue');
  handleSupplementaryEvidenceEventPage.handleSupplementaryEvidence();
  await I.completeEvent('Submit');
});

Scenario('HMCTS admin can see Documents scanned in with Bulk Scan', async (I, caseViewPage) => {
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.seeInTab(['Scanned Documents 1', 'Document type'], scannedDocument.type);
  I.seeInTab(['Scanned Documents 1', 'Document subtype'], scannedDocument.subtype);
  I.seeInTab(['Scanned Documents 1', 'Document url'], 'mockFile.txt');
  I.seeInTab(['Scanned Documents 1', 'Document control number'], scannedDocument.controlNumber);
  I.seeInTab(['Scanned Documents 1', 'File name'], scannedDocument.fileName);
  I.seeInTab(['Scanned Documents 1', 'Scanned date'], '1 Jan 2050, 12:00:00 PM');
  I.seeInTab(['Scanned Documents 1', 'Delivery date'], '1 Jan 2050, 12:00:00 PM');
  I.seeInTab(['Scanned Documents 1', 'Exception record reference'], scannedDocument.exceptionRecordReference);
});
