const config = require('../config.js');
const directions = require('../fixtures/directions.js');
const gatekeepingCaseData = require('../fixtures/caseData/gatekeepingNoAllocatedJudge.json');
const hearingDetails = require('../fixtures/hearingTypeDetails.js');
const moment = require('moment');
const api = require('../helpers/api_helper');

let caseId;

Feature('Gatekeeping judge SDO journey');

BeforeSuite(async ({I}) => {
  caseId = await I.submitNewCaseWithData(gatekeepingCaseData);
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);
});

Before(async ({I}) => await I.navigateToCaseDetails(caseId));

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

Scenario('Gatekeeping judge adds hearing', async ({I, caseViewPage, manageHearingsEventPage}) => {
  let hearingStartDate = moment('2030-01-10 14:00:00').toDate();
  let hearingEndDate = moment(hearingStartDate).add(30,'m').toDate();

  await caseViewPage.goToNewActions(config.administrationActions.manageHearings);
  await manageHearingsEventPage.enterHearingDetails({startDate: hearingStartDate, endDate: hearingEndDate, presence: hearingDetails[0].presence});
  manageHearingsEventPage.enterVenue(hearingDetails[0]);
  await I.goToNextPage();
  manageHearingsEventPage.enterJudgeDetails(hearingDetails[0]);
  manageHearingsEventPage.enterLegalAdvisorName(hearingDetails[0].judgeAndLegalAdvisor.legalAdvisorName);
  await I.goToNextPage();
  manageHearingsEventPage.dontSendNoticeOfHearing();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageHearings);

  await api.pollLastEvent(caseId, config.internalActions.populateSDO);
});

Scenario('Gatekeeping judge drafts gatekeeping order', async ({I, caseViewPage, addGatekeepingOrderEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.addGatekeepingOrder);
  await addGatekeepingOrderEventPage.createGatekeepingOrderThroughService();

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

  await I.goToNextPage();

  I.see('All parties');
  I.see('Request permission for expert evidence');
  I.dontSee('Request help to take part in proceedings');
  I.see('Ask for disclosure');
  I.dontSee('Attend the pre-hearing and hearing');
  I.dontSee('Contact alternative carers');

  I.see('Local Authority');
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

  addGatekeepingOrderEventPage.clickDateAndTime('Request permission for expert evidence');
  await addGatekeepingOrderEventPage.seeDate('Request permission for expert evidence','2030-01-07 12:00:00');

  addGatekeepingOrderEventPage.clickNumberOfDaysBeforeHearing('Ask for disclosure');
  await addGatekeepingOrderEventPage.seeDays('Ask for disclosure',2);

  addGatekeepingOrderEventPage.clickNumberOfDaysBeforeHearing('Send documents to all parties');
  await addGatekeepingOrderEventPage.seeDays('Send documents to all parties',2);
  await addGatekeepingOrderEventPage.seeDetails('Send documents to all parties', 'Give all parties access to all documents sent to the court, including:\n\n' +
      '- the application form\n' +
      '- annex documents\n' +
      '- evidential checklist documents\n' +
      '- any documents sent later on\n');

  await I.goToNextPage();

  await I.addAnotherElementToCollection();
  await addGatekeepingOrderEventPage.enterCustomDirections(directions[0]);
  await I.addAnotherElementToCollection();
  await addGatekeepingOrderEventPage.enterCustomDirections(directions[1]);
  await I.goToNextPage();
  await addGatekeepingOrderEventPage.enterIssuingJudge('Bob Ross');
  await I.goToNextPage();
  addGatekeepingOrderEventPage.markAsDraft();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.addGatekeepingOrder);

  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  I.seeInTab(['Gatekeeping order', 'File'], 'draft-standard-directions-order.pdf');
});

Scenario('Gatekeeping judge seals gatekeeping order', async ({I, caseViewPage, addGatekeepingOrderEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.addGatekeepingOrder);
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

  await I.goToNextPage();

  I.see('All parties');
  I.see('Request permission for expert evidence');
  I.dontSee('Ask for disclosure');

  I.see('Local Authority');
  I.see('Identify alternative carers');
  I.dontSee('Send documents to all parties');

  await addGatekeepingOrderEventPage.seeDate('Request permission for expert evidence','2030-01-07 12:00:00');

  addGatekeepingOrderEventPage.clickNumberOfDaysBeforeHearing('Identify alternative carers');
  await addGatekeepingOrderEventPage.seeDays('Identify alternative carers',0);

  await I.goToNextPage();

  await I.goToNextPage();

  await I.goToNextPage();

  await addGatekeepingOrderEventPage.markAsFinal({day: 11, month: 1, year: 2020});
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.addGatekeepingOrder);

  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeInTab(['Gatekeeping order', 'File'], 'standard-directions-order.pdf');
  I.seeInTab(['Gatekeeping order', 'Date of issue'], '11 January 2020');
});
