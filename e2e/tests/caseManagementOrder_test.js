const config = require('../config.js');
const hearingDetails = require('../fixtures/hearingTypeDetails.js');
const directions = require('../fixtures/directions.js');
const schedule = require('../fixtures/schedule.js');

let caseId;

Feature('Case Management Order Journey');

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
  I.click('Continue');
  draftCaseManagementOrderEventPage.markToReviewedBySelf();
  await I.completeEvent('Submit');
  assertCanSeeDraftCMO(I, caseViewPage);
  await caseViewPage.goToNewActions(config.applicationActions.draftCaseManagementOrder);
  await draftCaseManagementOrderEventPage.validatePreviousSelectedHearingDate('1 Jan 2050');
});

const allOtherPartyDetails = [
  {
    email: config.hmctsAdminEmail,
    password: config.hmctsAdminPassword,
  },
  {
    email: config.cafcassEmail,
    password: config.cafcassPassword,
  },
  {
    email: config.judiciaryEmail,
    password: config.judiciaryPassword,
  }];

// This scenario relies on running after 'local authority creates CMO'
Scenario('Other parties cannot see the draft CMO when it is marked for self review', async (I, caseViewPage, draftCaseManagementOrderEventPage) => {
  // Ensure the selection is self review
  await caseViewPage.goToNewActions(config.applicationActions.draftCaseManagementOrder);
  skipToReview(I);
  draftCaseManagementOrderEventPage.markToReviewedBySelf();
  await I.completeEvent('Submit');

  for (let userDetails of allOtherPartyDetails) {
    await assertUserCannotSeeDraftOrders(I, userDetails);
  }

  // Log back in as LA
  I.signOut();
  await I.signIn(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
});

// This scenario relies on running after 'local authority creates CMO'
// Currently send to judge does the same as party review
Scenario('Other parties can see the draft CMO when it is marked for party review', async (I, caseViewPage, draftCaseManagementOrderEventPage) => {
  // Ensure the selection is party review
  await caseViewPage.goToNewActions(config.applicationActions.draftCaseManagementOrder);
  skipToReview(I);
  draftCaseManagementOrderEventPage.markToBeReviewedByParties();
  await I.completeEvent('Submit');

  for (let otherPartyDetails of allOtherPartyDetails) {
    await assertUserCanSeeDraftOrdersAndCMO(I, otherPartyDetails, caseViewPage);
  }

  // Log back in as LA
  I.signOut();
  await I.signIn(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
});

const assertCanSeeDraftCMO = (I, caseViewPage) => {
  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  I.seeAnswerInTab(1, 'Case management order', 'Which hearing is this order for?', '1 Jan 2050');
  I.seeAnswerInTab(1, 'Directions 1', 'Direction title', 'Mock title');
  I.seeAnswerInTab(4, 'Directions 1', 'Description', 'Mock description');
  I.seeAnswerInTab(5, 'Directions 1', 'For', 'All parties');
  I.seeAnswerInTab(6, 'Directions 1', 'Due date and time', '1 Jan 2050, 12:00:00 PM');
  I.seeAnswerInTab(1, 'Directions 2', 'Direction title', 'Mock title');
  I.seeAnswerInTab(4, 'Directions 2', 'Description', 'Mock description');
  I.seeAnswerInTab(5, 'Directions 2', 'For', 'Local Authority');
  I.seeAnswerInTab(6, 'Directions 2', 'Due date and time', '1 Jan 2050, 12:00:00 PM');
  I.seeAnswerInTab(1, 'Directions 3', 'Direction title', 'Mock title');
  I.seeAnswerInTab(4, 'Directions 3', 'Description', 'Mock description');
  I.seeAnswerInTab(5, 'Directions 3', 'For', 'Cafcass');
  I.seeAnswerInTab(6, 'Directions 3', 'Due date and time', '1 Jan 2050, 12:00:00 PM');
  I.seeAnswerInTab(1, 'Directions 4', 'Direction title', 'Mock title');
  I.seeAnswerInTab(4, 'Directions 4', 'Description', 'Mock description');
  I.seeAnswerInTab(5, 'Directions 4', 'For', 'Court');
  I.seeAnswerInTab(6, 'Directions 4', 'Due date and time', '1 Jan 2050, 12:00:00 PM');
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
};

const assertUserCannotSeeDraftOrders = async (I, userDetails) => {
  await switchUserAndNavigateToCase(I, userDetails);
  // Assert that it can't be seen
  I.dontSee('Draft orders', '.tabs .tabs-list');
};

const assertUserCanSeeDraftOrdersAndCMO = async (I, userDetails, caseViewPage) => {
  await switchUserAndNavigateToCase(I, userDetails);
  // Assert that Draft orders can be seen
  I.see('Draft orders', '.tabs .tabs-list');
  assertCanSeeDraftCMO(I, caseViewPage);
};

const switchUserAndNavigateToCase = async (I, userDetails) => {
  // Sign out and login as admin and navigate to the case
  I.signOut();
  await I.signIn(userDetails.email, userDetails.password);
  await I.navigateToCaseDetails(caseId);
};

const skipToReview = (I) => {
  const numOfPagesExcludingReview = 7;
  for (let i = 0; i < numOfPagesExcludingReview; i++) {
    I.click('Continue');
  }
};
