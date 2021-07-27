const config = require('../config.js');
const dateFormat = require('dateformat');

let caseId;

Feature('Urgent and uploaded gatekeeping order');

async function setupScenario(I) {
  if (!caseId) { caseId = await I.submitNewCaseWithData(require('../fixtures/caseData/gatekeepingFullDetails.json')); }
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);
}

Scenario('Gatekeeping judge uploads urgent hearing order', async ({I, caseViewPage, draftStandardDirectionsEventPage}) => {
  await setupScenario(I);
  const allocationDecisionFields = draftStandardDirectionsEventPage.fields.allocationDecision;
  const translationRadioOptions = draftStandardDirectionsEventPage.fields.translationRequirement;
  await caseViewPage.goToNewActions(config.administrationActions.addGatekeepingOrder);
  I.click('Upload an urgent hearing order');
  await I.goToNextPage();

  await draftStandardDirectionsEventPage.makeAllocationDecision(allocationDecisionFields.judgeLevelConfirmation.no, allocationDecisionFields.allocationLevel.magistrate, 'some reason');
  await I.goToNextPage();
  await draftStandardDirectionsEventPage.uploadUrgentHearingOrder(config.testWordFile);
  await draftStandardDirectionsEventPage.selectTranslationRequirement(translationRadioOptions.welshToEnglish);
  await I.completeEvent('Save and continue');

  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeInTab(['Gatekeeping order - urgent hearing order', 'Allocation decision'], 'Magistrate');
  I.seeInTab(['Gatekeeping order - urgent hearing order', 'Order'], 'mockFile.pdf');
  I.seeInTab(['Gatekeeping order - urgent hearing order', 'Date added'], dateFormat('d mmm yyyy'));
  I.see('Sent for translation');

  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeInTab(['Allocation decision', 'Which level of judge is needed for this case?'], 'Magistrate');
  I.seeInTab(['Allocation decision', 'Give reason'], 'some reason');

  caseViewPage.selectTab(caseViewPage.tabs.hearings);
  I.seeInTab(['Notice of proceedings 1', 'File name'], 'Notice_of_proceedings_c6.pdf');
});

Scenario('Gatekeeping judge uploads draft gatekeeping order', async ({I, caseViewPage, draftStandardDirectionsEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.administrationActions.addGatekeepingOrder);
  I.click('Upload a prepared gatekeeping order');
  await I.goToNextPage();

  await draftStandardDirectionsEventPage.uploadPreparedSDO(config.testWordFile);
  I.see('Case assigned to: Her Honour Judge Moley');
  I.click('Yes');
  await I.goToNextPage();

  await draftStandardDirectionsEventPage.markAsDraft();
  await I.completeEvent('Save and continue');

  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  I.see('Draft gatekeeping order');
  I.seeInTab(['Gatekeeping order', 'File'], 'mockFile.docx');
  I.seeInTab(['Gatekeeping order', 'Date uploaded'], dateFormat('d mmm yyyy'));
  I.seeInTab(['Gatekeeping order', 'Uploaded by'], 'Uploaded by');
});

Scenario('Gatekeeping judge uploads final standard directions', async ({I, caseViewPage, draftStandardDirectionsEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.administrationActions.addGatekeepingOrder);
  I.see('mockFile.docx');
  await draftStandardDirectionsEventPage.uploadReplacementSDO(config.testWordFile);
  I.see('Case assigned to: Her Honour Judge Moley');
  await I.goToNextPage();
  await draftStandardDirectionsEventPage.markAsFinal();
  await I.completeEvent('Save and continue');

  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeInTab(['Gatekeeping order', 'File'], 'mockFile.pdf');
  I.seeInTab(['Gatekeeping order', 'Date uploaded'], dateFormat('d mmm yyyy'));
  I.seeInTab(['Gatekeeping order', 'Uploaded by'], 'Uploaded by');

  I.dontSeeTab(caseViewPage.tabs.draftOrders);
});
