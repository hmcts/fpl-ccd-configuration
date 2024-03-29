const config = require('../config.js');
const scannedDocument = require('../fixtures/scannedDocument.js');

let caseId;

Feature('Uploading bulk scan document');

async function setupScenario(I) {
  if (!caseId) { caseId = await I.submitNewCaseWithData(); }
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
}

Scenario('HMCTS admin uploads documents to be scanned @nightlyOnly', async ({I, caseViewPage, handleSupplementaryEvidenceEventPage, attachScannedDocsEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.administrationActions.bulkScan);
  await attachScannedDocsEventPage.enterScannedDocument(scannedDocument, config.testFile);
  await I.goToNextPage();
  await handleSupplementaryEvidenceEventPage.handleSupplementaryEvidence();
  await I.completeEvent('Submit');
});

Scenario('HMCTS admin can see documents scanned in with Bulk Scan @nightlyOnly', async ({I, caseViewPage}) => {
  await setupScenario(I);
  caseViewPage.selectTab(caseViewPage.tabs.furtherEvidence);
  I.expandDocumentSection('Any other documents', 'Example file name');
  I.seeInExpandedDocument('Example file name', null, '12:00pm, 1 January 2050');
});
