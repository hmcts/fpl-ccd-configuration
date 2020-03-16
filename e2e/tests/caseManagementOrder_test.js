const config = require('../config.js');
const hearingDetails = require('../fixtures/hearingTypeDetails.js');
const directions = require('../fixtures/directions.js');
const schedule = require('../fixtures/schedule.js');
const cmoHelper = require('../helpers/case_management_order_helper.js');

let caseId;

Feature('Case Management Order Journey');

Before(async (I, caseViewPage, submitApplicationEventPage, enterFamilyManCaseNumberEventPage, sendCaseToGatekeeperEventPage, addHearingBookingDetailsEventPage, draftStandardDirectionsEventPage,
  allocatedJudgeEventPage) => {
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

    //gatekeeper adds allocated judge
    await caseViewPage.goToNewActions(config.applicationActions.allocatedJudge);
    await allocatedJudgeEventPage.enterAllocatedJudge('Moley');
    await I.completeEvent('Save and continue');

    // gatekeeper login and create sdo
    await caseViewPage.goToNewActions(config.administrationActions.draftStandardDirections);
    await draftStandardDirectionsEventPage.enterJudgeAndLegalAdvisor('Smith', 'Bob Ross');
    await draftStandardDirectionsEventPage.enterDatesForDirections(directions[0]);
    await draftStandardDirectionsEventPage.markAsFinal();
    await I.completeEvent('Save and continue');
    I.seeEventSubmissionConfirmation(config.administrationActions.draftStandardDirections);
  }
  // Log back in as LA
  // I.signOut();
  await I.signIn(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);

  await I.navigateToCaseDetails(caseId);
});

Scenario('local authority creates CMO', async (I, caseViewPage, draftCaseManagementOrderEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.draftCaseManagementOrder);
  await draftCaseManagementOrderEventPage.associateHearingDate('1 Jan 2050');
  await I.retryUntilExists(() => I.click('Continue'), '#allPartiesLabelCMO');
  await draftCaseManagementOrderEventPage.enterDirection(directions[0]);
  await I.retryUntilExists(() => I.click('Continue'), '#orderBasisLabel');
  await I.addAnotherElementToCollection();
  await draftCaseManagementOrderEventPage.enterRecital('Recital 1', 'Recital 1 description');
  await I.retryUntilExists(() => I.click('Continue'), '#schedule_schedule');
  await draftCaseManagementOrderEventPage.enterSchedule(schedule);
  await I.retryUntilExists(() => I.click('Continue'), '#caseManagementOrder_status');
  await draftCaseManagementOrderEventPage.markToReviewedBySelf();
  await I.completeEvent('Submit');
  cmoHelper.assertCanSeeDraftCMO(I, caseViewPage, draftCaseManagementOrderEventPage.staticFields.statusRadioGroup.selfReview);
});

// This scenario relies on running after 'local authority creates CMO'
Scenario('Other parties cannot see the draft CMO document when it is marked for self review', async (I, caseViewPage, draftCaseManagementOrderEventPage) => {
  // Ensure the selection is self review
  await caseViewPage.goToNewActions(config.applicationActions.draftCaseManagementOrder);
  await cmoHelper.sendDraftForSelfReview(I, draftCaseManagementOrderEventPage);

  cmoHelper.assertCanSeeDraftCMO(I, caseViewPage, draftCaseManagementOrderEventPage.staticFields.statusRadioGroup.selfReview);

  for (let userDetails of cmoHelper.allOtherPartyDetails) {
    await cmoHelper.assertUserCannotSeeDraftOrdersTab(I, userDetails, caseId);
  }
});

// This scenario relies on running after 'local authority creates CMO'
Scenario('Other parties can see the draft CMO document when it is marked for party review', async (I, caseViewPage, draftCaseManagementOrderEventPage) => {
  // Ensure the selection is party review
  await caseViewPage.goToNewActions(config.applicationActions.draftCaseManagementOrder);

  await cmoHelper.sendDraftForPartyReview(I, draftCaseManagementOrderEventPage);

  cmoHelper.assertCanSeeDraftCMO(I, caseViewPage, draftCaseManagementOrderEventPage.staticFields.statusRadioGroup.partiesReview);

  for (let otherPartyDetails of cmoHelper.allOtherPartyDetails) {
    await cmoHelper.assertUserCanSeeDraftCMODocument(I, otherPartyDetails, caseViewPage, caseId);
  }
});

Scenario('Judge sees Action CMO placeholder when CMO is not in Judge Review', async (I, caseViewPage) => {
  // Login as Judge
  await cmoHelper.switchUserAndNavigateToCase(I, {
    email: config.judiciaryEmail,
    password: config.judiciaryPassword,
  }, caseId);

  await caseViewPage.goToNewActions(config.applicationActions.actionCaseManagementOrder);
  await I.see('You cannot edit this order');
  await I.see('You can only review the draft order after it has been submitted');
});

Scenario('Local Authority sends draft to Judge who requests corrections', async (I, caseViewPage, draftCaseManagementOrderEventPage, actionCaseManagementOrderEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.draftCaseManagementOrder);
  await cmoHelper.sendDraftForJudgeReview(I, draftCaseManagementOrderEventPage);

  await caseViewPage.goToNewActions(config.applicationActions.draftCaseManagementOrder);
  I.see('You can no longer edit this order');
  await I.completeEvent('Submit');

  await cmoHelper.switchUserAndNavigateToCase(I, {
    email: config.judiciaryEmail,
    password: config.judiciaryPassword,
  }, caseId);

  await caseViewPage.goToNewActions(config.applicationActions.actionCaseManagementOrder);
  await cmoHelper.actionDraft(I, actionCaseManagementOrderEventPage);

  await cmoHelper.switchUserAndNavigateToCase(I, {
    email: config.swanseaLocalAuthorityEmailUserOne,
    password: config.localAuthorityPassword,
  }, caseId);

  await caseViewPage.goToNewActions(config.applicationActions.draftCaseManagementOrder);
  await cmoHelper.sendDraftForJudgeReview(I, draftCaseManagementOrderEventPage);
});


//Skipped due to new error validation for approving a CMO with a hearing date in the future. We need to come up with
// a better solution to account for this. Options:
// - Have dynamic config to disable validation when e2es are run so it will allow us to skip the rules about submitting.
// - Invoke the endpoint (not sure if jenkins have got access) to set the data with hearing date in past?
// This would either require new endpoint on FPL or invoke the ccd endpoints.
xScenario('Local Authority sends draft to Judge who approves CMO', async (I, caseViewPage, draftCaseManagementOrderEventPage, actionCaseManagementOrderEventPage) => {
  // LA sends to judge
  await caseViewPage.goToNewActions(config.applicationActions.draftCaseManagementOrder);
  await cmoHelper.skipToReview(I);
  draftCaseManagementOrderEventPage.markToBeSentToJudge();
  await I.completeEvent('Submit');
  I.dontSee('Draft orders', '.tabs .tabs-list');

  // Login as Judge
  await cmoHelper.switchUserAndNavigateToCase(I, {
    email: config.judiciaryEmail,
    password: config.judiciaryPassword,
  }, caseId);
  cmoHelper.assertCanSeeDraftCMO(I, caseViewPage, draftCaseManagementOrderEventPage.staticFields.statusRadioGroup.sendToJudge);

  // Approve CMO
  await caseViewPage.goToNewActions(config.applicationActions.actionCaseManagementOrder);
  await cmoHelper.skipToSchedule(I);
  await I.retryUntilExists(() => I.click('Continue'), actionCaseManagementOrderEventPage.staticFields.statusRadioGroup.groupName);
  actionCaseManagementOrderEventPage.markToBeSentToAllParties();
  actionCaseManagementOrderEventPage.markNextHearingToBeFinalHearing();
  await I.retryUntilExists(() => I.click('Continue'), actionCaseManagementOrderEventPage.fields.nextHearingDateList);
  actionCaseManagementOrderEventPage.selectNextHearingDate('1 Jan 2050');
  await I.completeEvent('Save and continue');
  cmoHelper.assertCanSeeActionCMO(I, caseViewPage, actionCaseManagementOrderEventPage.labels.files.sealedCaseManagementOrder);
});
