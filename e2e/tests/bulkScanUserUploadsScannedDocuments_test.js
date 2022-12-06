const config = require('../config.js');
const scannedDocument = require('../fixtures/scannedDocument.js');

let caseId;

Feature('Uploading bulk scan document');

async function setupScenario(I, login) {
  if (!caseId) { caseId = await I.submitNewCaseWithData(); }
  await login('hmctsAdminUser');
  await I.navigateToCaseDetails(caseId);
}

Scenario('HMCTS admin uploads documents to be scanned', async ({I, caseViewPage, handleSupplementaryEvidenceEventPage, attachScannedDocsEventPage, login}) => {
  await setupScenario(I, login);
  await caseViewPage.goToNewActions(config.administrationActions.bulkScan);
  await attachScannedDocsEventPage.enterScannedDocument(scannedDocument, config.testFile);
  await I.goToNextPage();
  await handleSupplementaryEvidenceEventPage.handleSupplementaryEvidence();
  await I.completeEvent('Submit');
}).tag('@nightly-only');

Scenario('HMCTS admin can see documents scanned in with Bulk Scan', async ({I, caseViewPage, login}) => {
  await setupScenario(I, login);
  caseViewPage.selectTab(caseViewPage.tabs.furtherEvidence);
  I.expandDocumentSection('Any other documents', 'Example file name');
  I.seeInExpandedDocument('Example file name', null, '12:00pm, 1 January 2050');
}).tag('@nightly-only');
