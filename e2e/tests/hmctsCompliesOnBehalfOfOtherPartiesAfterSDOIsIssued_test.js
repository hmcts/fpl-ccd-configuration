const config = require('../config.js');
const directions = require('../fixtures/directions');
const hearingDetails = require('../fixtures/hearingTypeDetails.js');

let caseId;

Feature('Comply with directions');

Before(async (I, caseViewPage, submitApplicationEventPage, enterFamilyManCaseNumberEventPage, addHearingBookingDetailsEventPage, sendCaseToGatekeeperEventPage, draftStandardDirectionsEventPage) => {
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

    //hmcts login, add case number, add hearing details and send to gatekeeper
    await I.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
    await I.navigateToCaseDetails(caseId);
    caseViewPage.goToNewActions(config.administrationActions.addFamilyManCaseNumber);
    enterFamilyManCaseNumberEventPage.enterCaseID();
    await I.completeEvent('Save and continue');
    caseViewPage.goToNewActions(config.administrationActions.addHearingBookingDetails);
    await addHearingBookingDetailsEventPage.enterHearingDetails(hearingDetails[0]);
    await I.completeEvent('Save and continue');
    caseViewPage.goToNewActions(config.administrationActions.sendToGatekeeper);
    sendCaseToGatekeeperEventPage.enterEmail();
    await I.completeEvent('Save and continue');
    I.seeEventSubmissionConfirmation(config.administrationActions.sendToGatekeeper);
    I.signOut();

    //gatekeeper login, draft sdo and select issued
    await I.signIn(config.gateKeeperEmail, config.gateKeeperPassword);
    await I.navigateToCaseDetails(caseId);
    await caseViewPage.goToNewActions(config.administrationActions.draftStandardDirections);
    await draftStandardDirectionsEventPage.enterJudgeAndLegalAdvisor('Smith', 'Bob Ross');
    await draftStandardDirectionsEventPage.enterDatesForDirections(directions[0]);
    draftStandardDirectionsEventPage.markAsFinal();
    await I.completeEvent('Save and continue');
    I.seeEventSubmissionConfirmation(config.administrationActions.draftStandardDirections);
    I.signOut();
  }
});

Scenario('hmcts admin complies with directions on behalf of other parties', async (I, caseViewPage, complyOnBehalfOfOthersEventPage) => {
  await I.signIn(config.hmctsAdminEmail, config.hmctsAdminPassword);
  await I.navigateToCaseDetails(caseId);
  caseViewPage.goToNewActions(config.applicationActions.complyOnBehalfOf);
  await complyOnBehalfOfOthersEventPage.addNewResponseOnBehalfOf('respondentDirectionsCustom', 'Respondent 1', 'Yes');
  await I.retryUntilExists(() => I.click('Continue'), '#otherPartiesDirectionsCustom');
  await I.retryUntilExists(() => I.click('Continue'), '#cafcassDirectionsCustom');
  await I.completeEvent('Save and continue');
  await I.seeEventSubmissionConfirmation(config.applicationActions.complyOnBehalfOf);
  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeAnswerInTab(1, 'Compliance 1', 'Party', 'Court');
  I.seeAnswerInTab(2, 'Compliance 1', 'Complying on behalf of', 'Respondent 1');
  I.seeAnswerInTab(4, 'Compliance 1', 'Has this direction been complied with?', 'Yes');
});
