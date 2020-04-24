const config = require('../config.js');
const response = require('../fixtures/response');
const directions = require('../fixtures/directions');
const hearingDetails = require('../fixtures/hearingTypeDetails.js');

let caseId;

Feature('Comply with directions');

BeforeSuite(async (I, caseViewPage, submitApplicationEventPage, enterFamilyManCaseNumberEventPage, addHearingBookingDetailsEventPage, sendCaseToGatekeeperEventPage, draftStandardDirectionsEventPage, allocatedJudgeEventPage) => {
  caseId = await I.logInAndCreateCase(config.swanseaLocalAuthorityUserOne);
  await I.enterMandatoryFields();
  await caseViewPage.goToNewActions(config.applicationActions.submitCase);
  submitApplicationEventPage.giveConsent();
  await I.completeEvent('Submit');

  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);

  await caseViewPage.goToNewActions(config.administrationActions.addFamilyManCaseNumber);
  enterFamilyManCaseNumberEventPage.enterCaseID();
  await I.completeEvent('Save and continue');
  await caseViewPage.goToNewActions(config.administrationActions.addHearingBookingDetails);
  await addHearingBookingDetailsEventPage.enterHearingDetails(hearingDetails[0]);
  await I.completeEvent('Save and continue', {summary: 'summary', description: 'description'});
  await caseViewPage.goToNewActions(config.applicationActions.allocatedJudge);
  await allocatedJudgeEventPage.enterAllocatedJudge('Moley');
  await I.completeEvent('Save and continue');
  await caseViewPage.goToNewActions(config.administrationActions.sendToGatekeeper);
  sendCaseToGatekeeperEventPage.enterEmail();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.sendToGatekeeper);


  await I.navigateToCaseDetailsAs(config.gateKeeperUser, caseId);
  await caseViewPage.goToNewActions(config.administrationActions.draftStandardDirections);
  await draftStandardDirectionsEventPage.skipDateOfIssue();
  await draftStandardDirectionsEventPage.useAllocatedJudge('Bob Ross');
  await draftStandardDirectionsEventPage.enterDatesForDirections(directions[0]);
  draftStandardDirectionsEventPage.markAsFinal();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.draftStandardDirections);

  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
});

Scenario('local authority complies with directions', async (I, caseViewPage, complyWithDirectionsEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.complyWithDirections);
  await complyWithDirectionsEventPage.canComplyWithDirection('localAuthorityDirections', 0, response, config.testFile);
  await I.completeEvent('Save and continue');
  await I.seeEventSubmissionConfirmation(config.applicationActions.complyWithDirections);
  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeInTab(['Compliance 1', 'Party'], 'Local Authority');
  I.seeInTab(['Compliance 1', 'Has this direction been complied with?'], 'Yes');
  I.seeInTab(['Compliance 1', 'Give details'], response.complied.yes.documentDetails);
  I.seeInTab(['Compliance 1', 'Upload file'], 'mockFile.txt');
});
