const config = require('../config.js');
const hearingDetails = require('../fixtures/hearingTypeDetails.js');
const directions = require('../fixtures/directions.js');
const schedule = require('../fixtures/schedule.js');

let caseId;

Feature('Local authority manages case after SDO is issued');

Before(async (I, caseViewPage, submitApplicationEventPage, enterFamilyManCaseNumberEventPage, sendCaseToGatekeeperEventPage, addHearingBookingDetailsEventPage, draftStandardDirectionsEventPage) => {
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

    //hmcts login, add case number and send to gatekeeper
    await I.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
    await I.navigateToCaseDetails(caseId);
    caseViewPage.goToNewActions(config.administrationActions.addFamilyManCaseNumber);
    enterFamilyManCaseNumberEventPage.enterCaseID();
    await I.completeEvent('Save and continue');
    caseViewPage.goToNewActions(config.administrationActions.sendToGatekeeper);
    sendCaseToGatekeeperEventPage.enterEmail();
    await I.completeEvent('Save and continue');
    I.seeEventSubmissionConfirmation(config.administrationActions.sendToGatekeeper);
    I.signOut();

    // gatekeeper add hearing booking detail
    await I.signIn(config.gateKeeperEmail, config.gateKeeperPassword);
    await I.navigateToCaseDetails(caseId);
    await caseViewPage.goToNewActions(config.administrationActions.addHearingBookingDetails);
    await addHearingBookingDetailsEventPage.enterHearingDetails(hearingDetails[0]);
    await I.addAnotherElementToCollection();
    await addHearingBookingDetailsEventPage.enterHearingDetails(hearingDetails[1]);
    await I.completeEvent('Save and continue', {summary: 'summary', description: 'description'});
    I.seeEventSubmissionConfirmation(config.administrationActions.addHearingBookingDetails);
    caseViewPage.selectTab(caseViewPage.tabs.hearings);

    // gatekeeper login and create sdo
    await caseViewPage.goToNewActions(config.administrationActions.draftStandardDirections);
    await draftStandardDirectionsEventPage.enterJudgeAndLegalAdvisor('Smith', 'Bob Ross');
    await draftStandardDirectionsEventPage.enterDatesForDirections(directions[0]);
    await draftStandardDirectionsEventPage.markAsFinal();
    await I.completeEvent('Save and continue');
    I.seeEventSubmissionConfirmation(config.administrationActions.draftStandardDirections);
    I.signOut();

    await I.signIn(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  }
  await I.navigateToCaseDetails(caseId);
});

Scenario('local authority creates CMO', async (I, caseViewPage, draftCaseManagementOrderEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.draftCaseManagementOrder);
  await draftCaseManagementOrderEventPage.associateHearingDate('1 Jan 2050');
  I.click('Continue');
  await draftCaseManagementOrderEventPage.enterDirection(directions[0]);
  I.click('Continue');
  await draftCaseManagementOrderEventPage.enterSchedule(schedule);
  I.click('Continue');
  await I.addAnotherElementToCollection();
  await draftCaseManagementOrderEventPage.enterRecital('Recital 1', 'Recital 1 description');
  await I.completeEvent('Submit');
  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  I.seeAnswerInTab(1, 'Case management order', 'Which hearing is this order for?', '1 Jan 2050');
  I.seeAnswerInTab(1, 'Directions 1', 'Direction title', 'Mock title');
  I.seeAnswerInTab(4, 'Directions 1', 'Description', 'Mock description');
  I.seeAnswerInTab(5, 'Directions 1', 'For', 'Parents and other respondents');
  I.seeAnswerInTab(6, 'Directions 1', 'Assignee', 'Respondent 1');
  I.seeAnswerInTab(7, 'Directions 1', 'Due date and time', '1 Jan 2050, 12:00:00 PM');
  I.seeAnswerInTab(1, 'Directions 2', 'Direction title', 'Mock title');
  I.seeAnswerInTab(4, 'Directions 2', 'Description', 'Mock description');
  I.seeAnswerInTab(5, 'Directions 2', 'For', 'Other parties');
  I.seeAnswerInTab(6, 'Directions 2', 'Assignee', 'Person 1');
  I.seeAnswerInTab(7, 'Directions 2', 'Due date and time', '1 Jan 2050, 12:00:00 PM');
  I.seeAnswerInTab(1, 'Schedule', 'Do you want to include a schedule?', 'Yes');
  I.seeAnswerInTab(2, 'Schedule', 'Allocation', 'The proceedings continue to be allocated to Paul Wilson');
  I.seeAnswerInTab(3, 'Schedule', 'Application', 'The local authority has applied for a care order');
  I.seeAnswerInTab(4, 'Schedule', 'Today\'s hearing', 'Today\'s case was listed for an INTERIM CARE ORDER HEARING');
  I.seeAnswerInTab(5, 'Schedule', 'Children\'s current arrangements', 'Mock arrangement');
  I.seeAnswerInTab(6, 'Schedule', 'Timetable for proceedings (26 weeks)', '26 weeks');
  I.seeAnswerInTab(7, 'Schedule', 'Timetable for the children', '05/05/2005 is the child\'s DOB');
  I.seeAnswerInTab(8, 'Schedule', 'Alternative carers', 'Inform the local authority in writing within 7 days');
  I.seeAnswerInTab(9, 'Schedule', 'Threshold', 'The S.31 threshold for the making of orders is in dispute');
  I.seeAnswerInTab(10, 'Schedule', 'Key issues', 'Are there any other family or friends capable of caring in the children');
  I.seeAnswerInTab(11, 'Schedule', 'Parties\' positions', 'The mother agrees section 20');
  I.seeAnswerInTab(1, 'Recitals 1', 'Recital title', 'Recital 1');
  await caseViewPage.goToNewActions(config.applicationActions.draftCaseManagementOrder);
  await draftCaseManagementOrderEventPage.validatePreviousSelectedHearingDate('1 Jan 2050');
});
