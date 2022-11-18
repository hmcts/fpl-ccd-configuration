const config = require('../config.js');
const mandatoryWithMultipleChildren = require('../fixtures/caseData/mandatoryWithMultipleChildren.json');
const placementResponses = require('../fixtures/placementResponses.js');

let caseId;
const placementFee = '490.0';
const childName = 'Timothy Jones';

Feature('Placement');

async function setupScenario(I) {
  if (!caseId) { caseId = await I.submitNewCaseWithData(mandatoryWithMultipleChildren); }
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
}

async function createPlacementApplication(I, placementEventPage, caseViewPage) {
  await caseViewPage.goToNewActions(config.administrationActions.placement);
  await placementEventPage.selectChild(childName);

  await I.goToNextPage();
  await placementEventPage.addApplication(config.testWordFile);

  await placementEventPage.attachSupportingDocument(0, config.testFile);
  await placementEventPage.attachSupportingDocument(1, config.testFile2, 'Description 1');
  await placementEventPage.addSupportingDocument(2, 'Maintenance agreement/award', config.testFile3);
  await placementEventPage.attachConfidentialDocument(0, config.testFile4);
  await placementEventPage.addConfidentialDocument(1, 'Other confidential documents', config.testFile5, 'Description 2');

  await I.goToNextPage();
  await placementEventPage.selectNotifyAllRespondents();

  await I.goToNextPage();
  I.see(placementFee);
  await placementEventPage.setPaymentDetails('PBA0082848', '8888', 'Customer reference');

  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.placement);

  caseViewPage.selectTab(caseViewPage.tabs.placement);

  I.seeInTab(['Child 1', 'Name'], 'Timothy Jones');
  I.seeInTab(['Child 1', 'Application document'], 'mockFile.pdf');
  I.seeInTab(['Child 1', 'Supporting document 1', 'Document type'], 'Birth/Adoption Certificate');
  I.seeInTab(['Child 1', 'Supporting document 1', 'Document'], 'mockFile.txt');
  I.seeInTab(['Child 1', 'Supporting document 2', 'Document type'], 'Statement of facts');
  I.seeInTab(['Child 1', 'Supporting document 2', 'Document'], 'mockFile2.txt');
  I.seeInTab(['Child 1', 'Supporting document 2', 'Description'], 'Description 1');
  I.seeInTab(['Child 1', 'Supporting document 3', 'Document type'], 'Maintenance agreement/award');
  I.seeInTab(['Child 1', 'Supporting document 3', 'Document'], 'mockFile3.txt');

  I.seeInTab(['Child 1', 'Confidential document 1', 'Document type'], 'Annex B');
  I.seeInTab(['Child 1', 'Confidential document 1', 'Document'], 'mockFile4.txt');
  I.seeTagInTab(['Child 1', 'Confidential document 1', 'Confidential']);
  I.seeInTab(['Child 1', 'Confidential document 2', 'Document type'], 'Other confidential documents');
  I.seeInTab(['Child 1', 'Confidential document 2', 'Document'], 'mockFile5.txt');
  I.seeInTab(['Child 1', 'Confidential document 2', 'Description'], 'Description 2');
  I.seeTagInTab(['Child 1', 'Confidential document 1', 'Confidential']);
}

async function createPlacementNoticeOfHearing(I, placementHearingEventPage, caseViewPage) {
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await caseViewPage.goToNewActions(config.administrationActions.placementHearing);

  await placementHearingEventPage.selectPlacementApplication(childName);
  await I.goToNextPage();

  let date = new Date(Date.now());
  let day = new Intl.DateTimeFormat('en-GB', {day: '2-digit'}).format(date);
  let month = new Intl.DateTimeFormat('en-GB', {month: 'long'}).format(date);
  let fileName = 'notice_of_hearing_placement_' + day + month + '.pdf';

  await placementHearingEventPage.enterHearingDetails({day: 1, month: 1, year: 2021, hour: 9, minute: 0, second: 0});
  await I.goToNextPage();

  await I.see(fileName);
  await I.seeCheckAnswersAndCompleteEvent('Save and continue');

  await I.seeEventSubmissionConfirmation(config.administrationActions.placementHearing);

  await caseViewPage.selectTab(caseViewPage.tabs.placement);
  await I.seeInTab(['Child 1', 'Notice of hearing for placement'], fileName);

}

Scenario('Local authority creates a placement application', async ({I, placementEventPage, caseViewPage}) => {
  await setupScenario(I);
  await createPlacementApplication(I, placementEventPage, caseViewPage);
});

Scenario('Admin issues a notice of hearing for placement', async({I, placementEventPage, placementHearingEventPage, caseViewPage}) => {
  await setupScenario(I);
  await createPlacementApplication(I, placementEventPage, caseViewPage);
  await createPlacementNoticeOfHearing(I, placementHearingEventPage, caseViewPage);
});

Scenario('LA uploads response to notice of placement', async({I, placementEventPage, placementHearingEventPage, caseViewPage, manageDocumentsLAEventPage}) => {
  await setupScenario(I);
  await createPlacementApplication(I, placementEventPage, caseViewPage);
  await createPlacementNoticeOfHearing(I, placementHearingEventPage, caseViewPage);

  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.manageDocumentsLA);

  await manageDocumentsLAEventPage.selectPlacementResponses();
  await manageDocumentsLAEventPage.selectPlacementApplication(childName);

  await I.goToNextPage();
  await I.addAnotherElementToCollection();
  await manageDocumentsLAEventPage.uploadPlacementResponse(placementResponses[0]);

  await I.seeCheckAnswersAndCompleteEvent('Save and continue');
  await caseViewPage.selectTab(caseViewPage.tabs.placement);
  await I.seeInTab(['Child 1', 'Notice of placement response 1', 'Notice of placement response description'], placementResponses[0].description);
  await I.seeInTab(['Child 1', 'Notice of placement response 1', 'Recipient type'], placementResponses[0].type);
});
