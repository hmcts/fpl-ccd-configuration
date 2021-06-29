const config = require('../config.js');
const gatekeeping = require('../fixtures/caseData/gatekeepingFullDetails.json');
const dateFormat = require('dateformat');

let caseId;

Feature('Alternate gatekeeping order route');

async function setupScenario(I) {
  if (!caseId) { caseId = await I.submitNewCaseWithData(gatekeeping); }
  await I.navigateToCaseDetailsAs(config.gateKeeperUser, caseId);
}

Scenario('Gatekeeper uploads urgent hearing order', async ({I, caseViewPage, draftStandardDirectionsEventPage}) => {
  await setupScenario(I);
  const allocationDecisionFields = draftStandardDirectionsEventPage.fields.allocationDecision;
  await caseViewPage.goToNewActions(config.administrationActions.draftStandardDirections);
  await draftStandardDirectionsEventPage.createUrgentHearingOrder();
  await draftStandardDirectionsEventPage.makeAllocationDecision(allocationDecisionFields.judgeLevelConfirmation.no, allocationDecisionFields.allocationLevel.magistrate, 'some reason');
  await I.goToNextPage();
  await draftStandardDirectionsEventPage.uploadUrgentHearingOrder(config.testWordFile);
  await I.completeEvent('Save and continue');

  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeInTab(['Gatekeeping order - urgent hearing order', 'Allocation decision'], 'Magistrate');
  I.seeInTab(['Gatekeeping order - urgent hearing order', 'Order'], 'mockFile.pdf');
  I.seeInTab(['Gatekeeping order - urgent hearing order', 'Date added'], dateFormat('d mmm yyyy'));

  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeInTab(['Allocation decision', 'Which level of judge is needed for this case?'], 'Magistrate');
  I.seeInTab(['Allocation decision', 'Give reason'], 'some reason');

  caseViewPage.selectTab(caseViewPage.tabs.hearings);
  I.seeInTab(['Notice of proceedings 1', 'File name'], 'Notice_of_proceedings_c6.pdf');
});

Scenario('Gatekeeper uploads draft standard directions', async ({I, caseViewPage, draftStandardDirectionsEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.administrationActions.draftStandardDirections);
  await draftStandardDirectionsEventPage.createSDOThroughUpload();
  await draftStandardDirectionsEventPage.useAllocatedJudge('Bob Ross');
  await I.goToNextPage();
  await draftStandardDirectionsEventPage.uploadPreparedSDO(config.testWordFile);
  await draftStandardDirectionsEventPage.markAsDraft();
  await I.completeEvent('Save and continue');

  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  I.see('Draft gatekeeping order');
  I.seeInTab(['Gatekeeping order', 'File'], 'mockFile.docx');
  I.seeInTab(['Gatekeeping order', 'Date uploaded'], dateFormat('d mmm yyyy'));
  I.seeInTab(['Gatekeeping order', 'Uploaded by'], 'Uploaded by'); // Asserting row is there, data in local and aat are different
});

Scenario('Gatekeeper uploads final standard directions', async ({I, caseViewPage, draftStandardDirectionsEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.administrationActions.draftStandardDirections);
  await draftStandardDirectionsEventPage.useAllocatedJudge('Bob Ross');
  await I.goToNextPage();
  I.see('mockFile.docx');
  await draftStandardDirectionsEventPage.uploadReplacementSDO(config.testWordFile);
  await draftStandardDirectionsEventPage.markAsFinal();
  await I.completeEvent('Save and continue');

  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeInTab(['Gatekeeping order', 'File'], 'mockFile.pdf');
  I.seeInTab(['Gatekeeping order', 'Date uploaded'], dateFormat('d mmm yyyy'));
  I.seeInTab(['Gatekeeping order', 'Uploaded by'], 'Uploaded by'); // Asserting row is there, data in local and aat are different
}).retry(1); //async action in previous test
