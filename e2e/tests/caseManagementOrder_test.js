const config = require('../config.js');
const directions = require('../fixtures/directions.js');
const schedule = require('../fixtures/schedule.js');
const cmoHelper = require('../helpers/case_management_order_helper.js');
const standardDirectionOrder = require('../fixtures/standardDirectionOrder.json');

let caseId;

Feature('Case Management Order Journey');

BeforeSuite(async (I) => {
  caseId = await I.submitNewCaseWithData(standardDirectionOrder);
});

Scenario('local authority creates CMO', async (I, caseViewPage, draftCaseManagementOrderEventPage) => {
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
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
  cmoHelper.assertCanSeeDraftCMO(I, caseViewPage, {status: draftCaseManagementOrderEventPage.staticFields.statusRadioGroup.selfReview});
});

// This scenario relies on running after 'local authority creates CMO'
Scenario('Other parties cannot see the draft CMO document when it is marked for self review', async (I, caseViewPage, draftCaseManagementOrderEventPage) => {
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  // Ensure the selection is self review
  await caseViewPage.goToNewActions(config.applicationActions.draftCaseManagementOrder);
  await cmoHelper.sendDraftForSelfReview(I, draftCaseManagementOrderEventPage);

  cmoHelper.assertCanSeeDraftCMO(I, caseViewPage, {status: draftCaseManagementOrderEventPage.staticFields.statusRadioGroup.selfReview});

  for (let userDetails of cmoHelper.allOtherPartyDetails) {
    await cmoHelper.assertUserCannotSeeDraftOrdersTab(I, userDetails, caseId);
  }
});

// This scenario relies on running after 'local authority creates CMO'
Scenario('Other parties can see the draft CMO document when it is marked for party review', async (I, caseViewPage, draftCaseManagementOrderEventPage) => {
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  // Ensure the selection is party review
  await caseViewPage.goToNewActions(config.applicationActions.draftCaseManagementOrder);

  await cmoHelper.sendDraftForPartyReview(I, draftCaseManagementOrderEventPage);

  cmoHelper.assertCanSeeDraftCMO(I, caseViewPage, {status: draftCaseManagementOrderEventPage.staticFields.statusRadioGroup.partiesReview});

  for (let otherPartyDetails of cmoHelper.allOtherPartyDetails) {
    await cmoHelper.assertUserCanSeeDraftCMODocument(I, otherPartyDetails, caseViewPage, caseId);
  }
});

Scenario('Judge sees Action CMO placeholder when CMO is not in Judge Review', async (I, caseViewPage) => {
  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);

  await caseViewPage.goToNewActions(config.applicationActions.actionCaseManagementOrder);
  await I.see('You cannot edit this order');
  await I.see('You can only review the draft order after it has been submitted');
});

Scenario('Local Authority sends draft to Judge who requests corrections', async (I, caseViewPage, draftCaseManagementOrderEventPage, actionCaseManagementOrderEventPage) => {
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.draftCaseManagementOrder);
  await cmoHelper.sendDraftForJudgeReview(I, draftCaseManagementOrderEventPage);

  await caseViewPage.goToNewActions(config.applicationActions.draftCaseManagementOrder);
  I.see('You can no longer edit this order');
  await I.completeEvent('Submit');

  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);

  await caseViewPage.goToNewActions(config.applicationActions.actionCaseManagementOrder);
  await cmoHelper.actionDraft(I, actionCaseManagementOrderEventPage);

  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);

  const details = {
    status: draftCaseManagementOrderEventPage.staticFields.statusRadioGroup.selfReview,
    hasIssuedDate: true,
    orderActions: {
      type: actionCaseManagementOrderEventPage.staticFields.statusRadioGroup.options.judgeRequestedChanges,
      reason: 'Mock reason',
    },
  };

  cmoHelper.assertCanSeeDraftCMO(I, caseViewPage, details);
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

  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);
  cmoHelper.assertCanSeeDraftCMO(I, caseViewPage, {status: draftCaseManagementOrderEventPage.staticFields.statusRadioGroup.sendToJudge});

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
