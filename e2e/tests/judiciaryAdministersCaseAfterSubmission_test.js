const config = require('../config.js');
const hearingDetails = require('../fixtures/hearingTypeDetails.js');
const orders = require('../fixtures/orders.js');
const orderFunctions = require('../fragments/orderFunctions');
const dateFormat = require('dateformat');
const dateToString = require('../helpers/date_to_string_helper');

let caseId;

Feature('Judiciary case administration after submission');

Before(async (I, caseViewPage, submitApplicationEventPage, enterFamilyManCaseNumberEventPage) => {
  if (!caseId) {
    await I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
    await I.enterMandatoryFields();
    await caseViewPage.goToNewActions(config.applicationActions.submitCase);
    submitApplicationEventPage.giveConsent();
    await I.completeEvent('Submit');

    // eslint-disable-next-line require-atomic-updates
    caseId = await I.grabTextFrom('.heading-h1');
    console.log(`Case ${caseId} has been submitted`);

    I.signOut();
    await I.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
    await I.navigateToCaseDetails(caseId);
    await caseViewPage.goToNewActions(config.administrationActions.addFamilyManCaseNumber);
    enterFamilyManCaseNumberEventPage.enterCaseID();
    await I.completeEvent('Save and continue');
    I.seeEventSubmissionConfirmation(config.administrationActions.addFamilyManCaseNumber);
    I.signOut();

    await I.signIn(config.judiciaryEmail, config.judiciaryPassword);
  }
  await I.navigateToCaseDetails(caseId);
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
  I.seeAnswerInTab(1, 'Hearing 1', 'Type of hearing', hearingDetails[0].caseManagement);
  I.seeAnswerInTab(2, 'Hearing 1', 'Venue', hearingDetails[0].venue);
  I.seeAnswerInTab(3, 'Hearing 1', 'Start date and time', dateFormat(startDate, 'd mmm yyyy, h:MM:ss TT'));
  I.seeAnswerInTab(4, 'Hearing 1', 'End date and time', dateFormat(endDate, 'd mmm yyyy, h:MM:ss TT'));
  I.seeAnswerInTab(5, 'Hearing 1', 'Hearing needs booked', hearingDetails[0].type.interpreter);
  I.seeAnswerInTab(5, 'Hearing 1', '', hearingDetails[0].type.welsh);
  I.seeAnswerInTab(5, 'Hearing 1', '', hearingDetails[0].type.somethingElse);
  I.seeAnswerInTab(6, 'Hearing 1', 'Give details', hearingDetails[0].giveDetails);
  I.seeAnswerInTab(1, 'Judge and legal advisor', 'Judge or magistrate\'s title', hearingDetails[0].judgeAndLegalAdvisor.judgeTitle);
  I.seeAnswerInTab(2, 'Judge and legal advisor', 'Last name', hearingDetails[0].judgeAndLegalAdvisor.judgeLastName);
  I.seeAnswerInTab(3, 'Judge and legal advisor', 'Legal advisor\'s full name', hearingDetails[0].judgeAndLegalAdvisor.legalAdvisorName);

  startDate = dateToString(hearingDetails[1].startDate);
  endDate = dateToString(hearingDetails[1].endDate);
  I.seeAnswerInTab(1, 'Hearing 2', 'Type of hearing', hearingDetails[1].caseManagement);
  I.seeAnswerInTab(2, 'Hearing 2', 'Venue', hearingDetails[1].venue);
  I.seeAnswerInTab(3, 'Hearing 2', 'Start date and time', dateFormat(startDate, 'd mmm yyyy, h:MM:ss TT'));
  I.seeAnswerInTab(4, 'Hearing 2', 'End date and time', dateFormat(endDate, 'd mmm yyyy, h:MM:ss TT'));
  I.seeAnswerInTab(5, 'Hearing 2', 'Hearing needs booked', hearingDetails[1].type.interpreter);
  I.seeAnswerInTab(5, 'Hearing 2', '', hearingDetails[1].type.welsh);
  I.seeAnswerInTab(5, 'Hearing 2', '', hearingDetails[1].type.somethingElse);
  I.seeAnswerInTab(6, 'Hearing 2', 'Give details', hearingDetails[1].giveDetails);
  I.seeAnswerInTab(1, 'Judge and legal advisor', 'Judge or magistrate\'s title', hearingDetails[1].judgeAndLegalAdvisor.judgeTitle);
  I.seeAnswerInTab(2, 'Judge and legal advisor', 'Title', hearingDetails[1].judgeAndLegalAdvisor.otherTitle);
  I.seeAnswerInTab(3, 'Judge and legal advisor', 'Last name', hearingDetails[1].judgeAndLegalAdvisor.judgeLastName);
  I.seeAnswerInTab(4, 'Judge and legal advisor', 'Legal advisor\'s full name', hearingDetails[1].judgeAndLegalAdvisor.legalAdvisorName);
});

Scenario('Judiciary creates multiple orders for the case @flag', async (I, caseViewPage, createOrderEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.createOrder);
  await orderFunctions.createOrder(I, createOrderEventPage, orders[0]);
  let orderTime = new Date();
  I.seeEventSubmissionConfirmation(config.administrationActions.createOrder);

  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeAnswerInTab(1, 'Order 1', 'Type of order', orders[0].type);
  I.seeAnswerInTab(2, 'Order 1', 'Order title', orders[0].title);
  I.seeAnswerInTab(4, 'Order 1', 'Order document', orders[0].document);
  I.seeAnswerInTab(5, 'Order 1', 'Date and time of upload', dateFormat(orderTime, 'd mmmm yyyy'));
  I.seeAnswerInTab(1, 'Judge and legal advisor', 'Judge or magistrate\'s title', orders[0].judgeAndLegalAdvisor.judgeTitle);
  I.seeAnswerInTab(2, 'Judge and legal advisor', 'Last name', orders[0].judgeAndLegalAdvisor.judgeLastName);
  I.seeAnswerInTab(3, 'Judge and legal advisor', 'Legal advisor\'s full name',  orders[0].judgeAndLegalAdvisor.legalAdvisorName);

  await caseViewPage.goToNewActions(config.administrationActions.createOrder);
  await orderFunctions.createOrder(I, createOrderEventPage, orders[1]);
  orderTime = new Date();
  I.seeEventSubmissionConfirmation(config.administrationActions.createOrder);

  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeAnswerInTab(1, 'Order 2', 'Type of order', orders[1].type);
  I.seeAnswerInTab(2, 'Order 2', 'Order document', orders[1].document);
  I.seeAnswerInTab(3, 'Order 2', 'Date and time of upload', dateFormat(orderTime, 'd mmmm yyyy'));
  I.seeAnswerInTab(1, 'Judge and legal advisor', 'Judge or magistrate\'s title', orders[1].judgeAndLegalAdvisor.judgeTitle);
  I.seeAnswerInTab(2, 'Judge and legal advisor', 'Last name', orders[1].judgeAndLegalAdvisor.judgeLastName);
  I.seeAnswerInTab(3, 'Judge and legal advisor', 'Legal advisor\'s full name',  orders[1].judgeAndLegalAdvisor.legalAdvisorName);

  await caseViewPage.goToNewActions(config.administrationActions.createOrder);
  await orderFunctions.createOrder(I, createOrderEventPage, orders[2]);
  orderTime = new Date();
  const expiryDate = new Date(orderTime.setMonth(orderTime.getMonth() + parseInt(orders[2].months)));
  I.seeEventSubmissionConfirmation(config.administrationActions.createOrder);

  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeAnswerInTab(1, 'Order 3', 'Type of order', orders[2].type);
  I.seeAnswerInTab(2, 'Order 3', 'Order document', orders[2].document);
  I.seeAnswerInTab(3, 'Order 3', 'Date and time of upload', dateFormat(orderTime, 'd mmmm yyyy'));
  I.seeAnswerInTab(4, 'Order 3', 'Order expires on', dateFormat(expiryDate, 'hh:MM, dd mmmm yyyy'));
  I.seeAnswerInTab(1, 'Judge and legal advisor', 'Judge or magistrate\'s title', orders[2].judgeAndLegalAdvisor.judgeTitle);
  I.seeAnswerInTab(2, 'Judge and legal advisor', 'Last name', orders[2].judgeAndLegalAdvisor.judgeLastName);
  I.seeAnswerInTab(3, 'Judge and legal advisor', 'Legal advisor\'s full name',  orders[2].judgeAndLegalAdvisor.legalAdvisorName);
});
