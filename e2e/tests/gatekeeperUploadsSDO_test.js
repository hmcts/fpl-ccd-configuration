const config = require('../config.js');
const gatekeeping = require('../fixtures/caseData/gatekeepingFullDetails.json');
const dateFormat = require('dateformat');

let caseId;

Feature('Gatekeeper uploads standard directions order').retry(config.maxTestRetries);

BeforeSuite(async ({I}) => {
  caseId = await I.submitNewCaseWithData(gatekeeping);

  await I.navigateToCaseDetailsAs(config.gateKeeperUser, caseId);
});

Scenario('Gatekeeper uploads draft standard directions', async ({I, caseViewPage, draftStandardDirectionsEventPage}) => {
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
  await caseViewPage.goToNewActions(config.administrationActions.draftStandardDirections);
  await draftStandardDirectionsEventPage.useAllocatedJudge('Bob Ross');
  await I.goToNextPage();
  I.see('mockFile.docx');
  await draftStandardDirectionsEventPage.uploadReplacementSDO(config.testWordFile);
  await draftStandardDirectionsEventPage.markAsFinal();
  await draftStandardDirectionsEventPage.checkC6();
  draftStandardDirectionsEventPage.checkC6A();
  await I.completeEvent('Save and continue');

  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeInTab(['Gatekeeping order', 'File'], 'mockFile.pdf');
  I.seeInTab(['Gatekeeping order', 'Date uploaded'], dateFormat('d mmm yyyy'));
  I.seeInTab(['Gatekeeping order', 'Uploaded by'], 'Uploaded by'); // Asserting row is there, data in local and aat are different
  caseViewPage.selectTab(caseViewPage.tabs.hearings);
  I.seeInTab(['Notice of proceedings 1', 'File name'], 'Notice_of_proceedings_c6.pdf');
  I.seeInTab(['Notice of proceedings 2', 'File name'], 'Notice_of_proceedings_c6a.pdf');
}).retry(1); //async action in previous test
