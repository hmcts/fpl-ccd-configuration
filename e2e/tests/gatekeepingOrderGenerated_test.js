const config = require('../config.js');
const directions = require('../fixtures/directions.js');
const gatekeepingCaseData = require('../fixtures/caseData/gatekeepingNoAllocatedJudge.json');
const hearingDate = '10 January 2050, 3:15pm';

let caseId;

Feature('Generated gatekeeping order');

async function setupScenario(I) {
  if (!caseId) { caseId = await I.submitNewCaseWithData(gatekeepingCaseData); }
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);
}

Scenario('Gatekeeping judge drafts gatekeeping order', async ({I, caseViewPage, addGatekeepingOrderEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.administrationActions.addGatekeepingOrder);
  addGatekeepingOrderEventPage.selectCorrectLevelOfJudge('Yes');
  await I.goToNextPage();

  addGatekeepingOrderEventPage.createGatekeepingOrderThroughService();
  await I.runAccessibilityTest();
  await I.goToNextPage();

  I.see('Request permission for expert evidence');
  I.see('Request help to take part in proceedings');
  I.see('Ask for disclosure');
  I.see('Attend the pre-hearing and hearing');
  I.see('Contact alternative carers');

  I.see('Send documents to all parties');
  I.see('Send missing annex documents to the court and all parties');
  I.see('Identify alternative carers');
  I.see('Send translated case documents to respondents');
  I.see('Lodge a bundle');
  I.see('Send case summary to all parties');
  I.see('Urgently consider jurisdiction and invite any representations');

  I.see('Send response to threshold statement to all parties');

  I.see('Arrange an advocates\' meeting');
  I.see('Send the guardian\'s analysis to all parties');
  I.see('Appoint a children\'s guardian');

  I.see('Object to a request for disclosure');

  I.see('Arrange interpreters');

  I.click('Request permission for expert evidence');
  I.click('Ask for disclosure');
  I.click('Send documents to all parties');

  await I.runAccessibilityTest();

  await I.goToNextPage();

  I.see(hearingDate);

  I.see('All parties');
  I.see('Request permission for expert evidence');
  I.dontSee('Request help to take part in proceedings');
  I.see('Ask for disclosure');
  I.dontSee('Attend the pre-hearing and hearing');
  I.dontSee('Contact alternative carers');

  I.see('Local authority');
  I.see('Send documents to all parties');
  I.dontSee('Send missing annex documents to the court and all parties');
  I.dontSee('Identify alternative carers');
  I.dontSee('Send translated case documents to respondents');
  I.dontSee('Lodge a bundle');
  I.dontSee('Send case summary to all parties');
  I.dontSee('Urgently consider jurisdiction and invite any representations');

  I.dontSee('Cafcass');
  I.dontSee('Arrange an advocates\' meeting');
  I.dontSee('Send the guardian\'s analysis to all parties');
  I.dontSee('Appoint a children\'s guardian');

  I.dontSee('Parents and respondents');
  I.dontSee('Send response to threshold statement to all parties');

  I.dontSee('Other parties');
  I.dontSee('Object to a request for disclosure');

  I.dontSee('Court');
  I.dontSee('Arrange interpreters');

  await addGatekeepingOrderEventPage.seeDays('REQUEST_PERMISSION_FOR_EXPERT_EVIDENCE',3);
  await addGatekeepingOrderEventPage.clickDateAndTime('REQUEST_PERMISSION_FOR_EXPERT_EVIDENCE');
  await addGatekeepingOrderEventPage.seeDate('REQUEST_PERMISSION_FOR_EXPERT_EVIDENCE','2050-01-05 12:00:00');

  await addGatekeepingOrderEventPage.seeDays('ASK_FOR_DISCLOSURE',2);

  await addGatekeepingOrderEventPage.seeDays('SEND_DOCUMENTS_TO_ALL_PARTIES',2);
  await addGatekeepingOrderEventPage.seeDetails('SEND_DOCUMENTS_TO_ALL_PARTIES',
    'Give all parties access to all documents sent to the court, including:\n\n' +
      '- the application form\n' +
      '- annex documents\n' +
      '- evidential checklist documents\n' +
      '- any documents sent later on\n');

  await I.runAccessibilityTest();
  await I.goToNextPage();

  I.see(hearingDate);

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

  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  I.seeInTab(['Gatekeeping order', 'File'], 'draft-standard-directions-order.pdf');

  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeInTab(['Allocation decision', 'Which level of judge is needed for this case?'], 'District Judge');
});

Scenario('Gatekeeping order allocated to Magistrate judge', async ({I, caseViewPage, addGatekeepingOrderEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.administrationActions.addGatekeepingOrder);
  addGatekeepingOrderEventPage.selectCorrectLevelOfJudge('No');
  await addGatekeepingOrderEventPage.selectAllocationDecision('Magistrate');
  await addGatekeepingOrderEventPage.enterProposalReason('new information was acquired');
  await I.goToNextPage();

  addGatekeepingOrderEventPage.createGatekeepingOrderThroughService();
  await I.goToNextPage();

  I.see('Request permission for expert evidence');
  I.see('Request help to take part in proceedings');
  I.see('Ask for disclosure');
  I.click('Request permission for expert evidence');
  I.click('Ask for disclosure');
  I.click('Send documents to all parties');
  await I.goToNextPage();

  I.see(hearingDate);
  I.see('Request permission for expert evidence');
  I.see('Ask for disclosure');
  await I.goToNextPage();

  I.see(hearingDate);
  await I.addAnotherElementToCollection();
  await addGatekeepingOrderEventPage.enterCustomDirections(directions[0]);
  await I.goToNextPage();

  addGatekeepingOrderEventPage.enterIssuingJudge('Judy', 'Bob Ross');
  await I.goToNextPage();

  addGatekeepingOrderEventPage.verifyNextStepsLabel();
  await I.completeEvent('Save and continue');

  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  I.seeInTab(['Gatekeeping order', 'File'], 'draft-standard-directions-order.pdf');

  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeInTab(['Allocation decision', 'Which level of judge is needed for this case?'], 'Magistrate');
  I.seeInTab(['Allocation decision', 'Give reason'], 'new information was acquired');
});

Scenario('Gatekeeping judge adds allocated judge', async ({I, caseViewPage, allocatedJudgeEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.applicationActions.allocatedJudge);
  await allocatedJudgeEventPage.enterAllocatedJudge('Moley', 'moley@example.com');
  await I.completeEvent('Save and continue');

  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeInTab(['Allocated Judge', 'Judge or magistrate\'s title'], 'Her Honour Judge');
  I.seeInTab(['Allocated Judge', 'Last name'], 'Moley');
  I.seeInTab(['Allocated Judge', 'Email Address'], 'moley@example.com');
});

Scenario('Gatekeeping judge seals gatekeeping order', async ({I, caseViewPage, addGatekeepingOrderEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.administrationActions.addGatekeepingOrder);
  addGatekeepingOrderEventPage.selectCorrectLevelOfJudge('Yes');
  await I.goToNextPage();

  I.seeCheckboxIsChecked('Request permission for expert evidence');
  I.seeCheckboxIsChecked('Ask for disclosure');
  I.seeCheckboxIsChecked('Send documents to all parties');

  I.dontSeeCheckboxIsChecked('Request help to take part in proceedings');
  I.dontSeeCheckboxIsChecked('Attend the pre-hearing and hearing');
  I.dontSeeCheckboxIsChecked('Contact alternative carers');
  I.dontSeeCheckboxIsChecked('Send missing annex documents to the court and all parties');
  I.dontSeeCheckboxIsChecked('Identify alternative carers');
  I.dontSeeCheckboxIsChecked('Send translated case documents to respondents');
  I.dontSeeCheckboxIsChecked('Lodge a bundle');
  I.dontSeeCheckboxIsChecked('Send case summary to all parties');
  I.dontSeeCheckboxIsChecked('Urgently consider jurisdiction and invite any representations');
  I.dontSeeCheckboxIsChecked('Send response to threshold statement to all parties');
  I.dontSeeCheckboxIsChecked('Arrange an advocates\' meeting');
  I.dontSeeCheckboxIsChecked('Send the guardian\'s analysis to all parties');
  I.dontSeeCheckboxIsChecked('Appoint a children\'s guardian');
  I.dontSeeCheckboxIsChecked('Object to a request for disclosure');
  I.dontSeeCheckboxIsChecked('Arrange interpreters');

  I.click('Send documents to all parties');
  I.click('Ask for disclosure');
  I.click('Identify alternative carers');

  await I.runAccessibilityTest();

  await I.goToNextPage();

  I.see(hearingDate);

  I.see('All parties');
  I.see('Request permission for expert evidence');
  I.dontSee('Ask for disclosure');

  I.see('Local authority');
  I.see('Identify alternative carers');
  I.dontSee('Send documents to all parties');

  await addGatekeepingOrderEventPage.seeDate('REQUEST_PERMISSION_FOR_EXPERT_EVIDENCE','2050-01-05 12:00:00');

  await addGatekeepingOrderEventPage.clickNumberOfDaysBeforeHearing('IDENTIFY_ALTERNATIVE_CARERS');
  await addGatekeepingOrderEventPage.seeDays('IDENTIFY_ALTERNATIVE_CARERS',0);

  await I.goToNextPage();

  await I.goToNextPage();
  addGatekeepingOrderEventPage.selectAllocatedJudge('Bob Ross');
  await I.goToNextPage();
  await addGatekeepingOrderEventPage.markAsFinal({day: 11, month: 1, year: 2020});
  await I.completeEvent('Save and continue');

  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeInTab(['Gatekeeping order', 'File'], 'standard-directions-order.pdf');
  I.seeInTab(['Gatekeeping order', 'Date of issue'], '11 January 2020');

  caseViewPage.selectTab(caseViewPage.tabs.history);
  I.seeEndStateForEvent(config.administrationActions.addGatekeepingOrder, 'Case management');
});
