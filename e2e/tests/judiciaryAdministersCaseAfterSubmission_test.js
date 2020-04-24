const config = require('../config.js');
const hearingDetails = require('../fixtures/hearingTypeDetails.js');
const orders = require('../fixtures/orders.js');
const orderFunctions = require('../helpers/generated_order_helper');
const dateFormat = require('dateformat');
const dateToString = require('../helpers/date_to_string_helper');

let caseId;

Feature('Judiciary case administration after submission');

BeforeSuite(async (I, caseViewPage, submitApplicationEventPage, enterFamilyManCaseNumberEventPage) => {
  caseId = await I.logInAndCreateCase(config.swanseaLocalAuthorityUserOne);
  await I.enterMandatoryFields({multipleChildren: true});
  await caseViewPage.goToNewActions(config.applicationActions.submitCase);
  submitApplicationEventPage.giveConsent();
  await I.completeEvent('Submit');

  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);

  await caseViewPage.goToNewActions(config.administrationActions.addFamilyManCaseNumber);
  enterFamilyManCaseNumberEventPage.enterCaseID();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.addFamilyManCaseNumber);

  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);
});

Before(async I => await I.navigateToCaseDetails(caseId));

Scenario('Judiciary adds allocated judge', async (I, caseViewPage, allocatedJudgeEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.allocatedJudge);
  await allocatedJudgeEventPage.enterAllocatedJudge('Moley');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.allocatedJudge);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeInTab(['Allocated Judge', 'Judge or magistrate\'s title'], 'Her Honour Judge');
  I.seeInTab(['Allocated Judge', 'Last name'], 'Moley');
});

Scenario('Judiciary enters hearing details and submits', async (I, caseViewPage, loginPage, addHearingBookingDetailsEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.addHearingBookingDetails);
  await addHearingBookingDetailsEventPage.enterHearingDetails(hearingDetails[0]);
  await I.addAnotherElementToCollection();
  await addHearingBookingDetailsEventPage.enterHearingDetails(hearingDetails[1]);
  await I.completeEvent('Save and continue', {summary: 'summary', description: 'description'});
  I.seeEventSubmissionConfirmation(config.administrationActions.addHearingBookingDetails);
  caseViewPage.selectTab(caseViewPage.tabs.hearings);

  let startDate = dateToString(hearingDetails[0].startDate);
  let endDate = dateToString(hearingDetails[0].endDate);
  I.seeInTab(['Hearing 1', 'Type of hearing'], hearingDetails[0].caseManagement);
  I.seeInTab(['Hearing 1', 'Venue'], hearingDetails[0].venue);
  I.seeInTab(['Hearing 1', 'Start date and time'], dateFormat(startDate, 'd mmm yyyy, h:MM:ss TT'));
  I.seeInTab(['Hearing 1', 'End date and time'], dateFormat(endDate, 'd mmm yyyy, h:MM:ss TT'));
  I.seeInTab(['Hearing 1', 'Hearing needs booked'], [hearingDetails[0].type.interpreter, hearingDetails[0].type.welsh, hearingDetails[0].type.somethingElse]);
  I.seeInTab(['Hearing 1', 'Give details'], hearingDetails[0].giveDetails);
  I.seeInTab(['Hearing 1', 'Judge and Justices\' Legal Adviser', 'Judge or magistrate\'s title'], hearingDetails[0].judgeAndLegalAdvisor.judgeTitle);
  I.seeInTab(['Hearing 1', 'Judge and Justices\' Legal Adviser', 'Last name'], hearingDetails[0].judgeAndLegalAdvisor.judgeLastName);
  I.seeInTab(['Hearing 1', 'Judge and Justices\' Legal Adviser', 'Justices\' Legal Adviser\'s full name'], hearingDetails[0].judgeAndLegalAdvisor.legalAdvisorName);

  startDate = dateToString(hearingDetails[1].startDate);
  endDate = dateToString(hearingDetails[1].endDate);
  I.seeInTab(['Hearing 2', 'Type of hearing'], hearingDetails[1].caseManagement);
  I.seeInTab(['Hearing 2', 'Venue'], hearingDetails[1].venue);
  I.seeInTab(['Hearing 2', 'Venue address', 'Building and Street'], hearingDetails[1].venueCustomAddress.buildingAndStreet.lineOne);
  I.seeInTab(['Hearing 2', 'Venue address', 'Address Line 2'], hearingDetails[1].venueCustomAddress.buildingAndStreet.lineTwo);
  I.seeInTab(['Hearing 2', 'Venue address', 'Address Line 3'], hearingDetails[1].venueCustomAddress.buildingAndStreet.lineThree);
  I.seeInTab(['Hearing 2', 'Venue address', 'Town or City'], hearingDetails[1].venueCustomAddress.town);
  I.seeInTab(['Hearing 2', 'Venue address', 'Postcode/Zipcode'], hearingDetails[1].venueCustomAddress.postcode);
  I.seeInTab(['Hearing 2', 'Venue address', 'Country'], hearingDetails[1].venueCustomAddress.country);
  I.seeInTab(['Hearing 2', 'Start date and time'], dateFormat(startDate, 'd mmm yyyy, h:MM:ss TT'));
  I.seeInTab(['Hearing 2', 'End date and time'], dateFormat(endDate, 'd mmm yyyy, h:MM:ss TT'));
  I.seeInTab(['Hearing 2', 'Hearing needs booked'], [hearingDetails[1].type.interpreter, hearingDetails[1].type.welsh, hearingDetails[1].type.somethingElse]);
  I.seeInTab(['Hearing 2', 'Give details'], hearingDetails[1].giveDetails);
  I.seeInTab(['Hearing 2', 'Judge and Justices\' Legal Adviser', 'Judge or magistrate\'s title'], hearingDetails[1].judgeAndLegalAdvisor.judgeTitle);
  I.seeInTab(['Hearing 2', 'Judge and Justices\' Legal Adviser', 'Title'], hearingDetails[1].judgeAndLegalAdvisor.otherTitle);
  I.seeInTab(['Hearing 2', 'Judge and Justices\' Legal Adviser', 'Last name'], hearingDetails[1].judgeAndLegalAdvisor.judgeLastName);
  I.seeInTab(['Hearing 2', 'Judge and Justices\' Legal Adviser', 'Justices\' Legal Adviser\'s full name'], hearingDetails[1].judgeAndLegalAdvisor.legalAdvisorName);
});

Scenario('Judiciary creates multiple orders for the case', async (I, caseViewPage, addHearingBookingDetailsEventPage, createOrderEventPage) => {
  for (let i = 0; i < orders.length; i++) {
    const defaultIssuedDate = new Date();
    await caseViewPage.goToNewActions(config.administrationActions.createOrder);
    await orderFunctions.createOrder(I, createOrderEventPage, orders[i], true);
    I.seeEventSubmissionConfirmation(config.administrationActions.createOrder);
    await orderFunctions.assertOrder(I, caseViewPage, orders[i], i + 1, defaultIssuedDate, true);
  }
});

Scenario('Judiciary creates notice of proceedings documents with allocated judge', async (I, caseViewPage, createNoticeOfProceedingsEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.createNoticeOfProceedings);
  await createNoticeOfProceedingsEventPage.checkC6();
  await createNoticeOfProceedingsEventPage.useAllocatedJudge();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.createNoticeOfProceedings);
  await caseViewPage.goToNewActions(config.administrationActions.createNoticeOfProceedings);
  await createNoticeOfProceedingsEventPage.checkC6A();
  await createNoticeOfProceedingsEventPage.useAlternateJudge();
  await createNoticeOfProceedingsEventPage.selectJudgeTitle();
  await createNoticeOfProceedingsEventPage.enterJudgeLastName('Sarah Simpson');
  await createNoticeOfProceedingsEventPage.enterLegalAdvisorName('Ian Watson');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.createNoticeOfProceedings);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.seeInTab(['Notice of proceedings 1', 'File name'], 'Notice_of_proceedings_c6a.pdf');
  I.seeInTab(['Notice of proceedings 2', 'File name'], 'Notice_of_proceedings_c6.pdf');
});
