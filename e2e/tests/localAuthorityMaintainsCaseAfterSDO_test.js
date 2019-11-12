const config = require('../config.js');
const hearingDetails = require('../fixtures/hearingTypeDetails.js');
const directions = require('../fixtures/directions.js');

let caseId;

Feature('Local authority manages case after SDO is issued');

Before(async (I, caseViewPage, submitApplicationEventPage, sendCaseToGatekeeperEventPage, addHearingBookingDetailsEventPage, draftStandardDirectionsEventPage) => {
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

    //hmcts login and send to gatekeeper
    await I.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
    await I.navigateToCaseDetails(caseId);
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
  await I.completeEvent('Submit');
  caseViewPage.selectTab(caseViewPage.tabs.draftOrders);
  I.seeAnswerInTab(1, 'Case management order', 'Which hearing is this order for?', '1 Jan 2050');
});
