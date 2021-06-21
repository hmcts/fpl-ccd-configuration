const config = require('../config.js');
const directions = require('../fixtures/directions.js');
const gatekeepingCaseData = require('../fixtures/caseData/gatekeepingNoAllocatedJudge.json');

let caseId;

Feature('Gatekeeping judge SDO journey');

BeforeSuite(async ({I}) => {
  caseId = await I.submitNewCaseWithData(gatekeepingCaseData);
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);
});

Before(async ({I}) => await I.navigateToCaseDetails(caseId));

Scenario('Gatekeeping judge drafts gatekeeping order', async ({I, caseViewPage, addGatekeepingOrderEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.addGatekeepingOrder);
  addGatekeepingOrderEventPage.createGatekeepingOrderThroughService();
  await I.runAccessibilityTest();
  await I.goToNextPage();
  await I.addAnotherElementToCollection();
  await addGatekeepingOrderEventPage.enterCustomDirections(directions[0]);
  await I.addAnotherElementToCollection();
  await addGatekeepingOrderEventPage.enterCustomDirections(directions[1]);
  await I.runAccessibilityTest();
  await I.goToNextPage();
  addGatekeepingOrderEventPage.enterIssuingJudge('Judy', 'Bob Ross');
  await I.runAccessibilityTest();
  await I.goToNextPage();
  addGatekeepingOrderEventPage.verifyNextStepsLabel();
  await I.runAccessibilityTest();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.addGatekeepingOrder);

  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  I.seeInTab(['Gatekeeping order', 'File'], 'draft-standard-directions-order.pdf');
});

Scenario('Gatekeeping judge adds allocated judge', async ({I, caseViewPage, allocatedJudgeEventPage}) => {
  await caseViewPage.goToNewActions(config.applicationActions.allocatedJudge);
  await allocatedJudgeEventPage.enterAllocatedJudge('Moley', 'moley@example.com');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.allocatedJudge);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeInTab(['Allocated Judge', 'Judge or magistrate\'s title'], 'Her Honour Judge');
  I.seeInTab(['Allocated Judge', 'Last name'], 'Moley');
  I.seeInTab(['Allocated Judge', 'Email Address'], 'moley@example.com');
});

Scenario('Gatekeeping judge seals gatekeeping order', async ({I, caseViewPage, addGatekeepingOrderEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.addGatekeepingOrder);
  await I.goToNextPage();
  await I.goToNextPage();
  addGatekeepingOrderEventPage.selectAllocatedJudge('Bob Ross');
  await I.goToNextPage();
  await addGatekeepingOrderEventPage.markAsFinal({day: 11, month: 1, year: 2020});
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.addGatekeepingOrder);

  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeInTab(['Gatekeeping order', 'File'], 'standard-directions-order.pdf');
  I.seeInTab(['Gatekeeping order', 'Date of issue'], '11 January 2020');
});
