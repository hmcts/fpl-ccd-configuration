const config = require('../config.js');


const directions = require('../fixtures/directions.js');
const hearingDetails = require('../fixtures/hearingTypeDetails.js');
const respondents = require('../fixtures/respondents.js');
const applicant = require('../fixtures/applicant.js');
const solicitor = require('../fixtures/solicitor.js');

Feature('Manual case creation');

Scenario('local authority tries to submit after filling mandatory data manually', async (I, caseViewPage, enterOrdersAndDirectionsNeededEventPage, enterHearingNeededEventPage, enterApplicantEventPage, enterChildrenEventPage, enterRespondentsEventPage, enterGroundsForApplicationEventPage, uploadDocumentsEventPage, enterAllocationProposalEventPage) => {
  await I.logInAndCreateCase(config.swanseaLocalAuthorityUserOne);

  await caseViewPage.goToNewActions(config.applicationActions.enterOrdersAndDirectionsNeeded);
  enterOrdersAndDirectionsNeededEventPage.checkCareOrder();
  await I.completeEvent('Save and continue');
  await caseViewPage.goToNewActions(config.applicationActions.enterHearingNeeded);
  enterHearingNeededEventPage.enterTimeFrame();
  await I.completeEvent('Save and continue');
  await caseViewPage.goToNewActions(config.applicationActions.enterApplicant);
  enterApplicantEventPage.enterApplicantDetails(applicant);
  enterApplicantEventPage.enterSolicitorDetails(solicitor);
  await I.completeEvent('Save and continue');
  await caseViewPage.goToNewActions(config.applicationActions.enterChildren);
  await enterChildrenEventPage.enterChildDetails('Timothy', 'Jones', '01', '08', '2015');
  await I.completeEvent('Save and continue');
  await caseViewPage.goToNewActions(config.applicationActions.enterRespondents);
  await enterRespondentsEventPage.enterRespondent(respondents[0]);
  await I.completeEvent('Save and continue');
  await caseViewPage.goToNewActions(config.applicationActions.enterGrounds);
  enterGroundsForApplicationEventPage.enterThresholdCriteriaDetails();
  await I.completeEvent('Save and continue');
  await caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
  uploadDocumentsEventPage.selectSocialWorkChronologyToFollow();
  uploadDocumentsEventPage.selectSocialWorkStatementIncludedInSWET();
  uploadDocumentsEventPage.uploadSocialWorkAssessment(config.testFile);
  uploadDocumentsEventPage.uploadCarePlan(config.testFile);
  uploadDocumentsEventPage.uploadSWET(config.testFile);
  uploadDocumentsEventPage.uploadThresholdDocument(config.testFile);
  uploadDocumentsEventPage.uploadChecklistDocument(config.testFile);
  await I.completeEvent('Save and continue');
  await caseViewPage.goToNewActions(config.applicationActions.enterAllocationProposal);
  enterAllocationProposalEventPage.selectAllocationProposal('District judge');
  await I.completeEvent('Save and continue');
});


Scenario('admin sends case to gatekeeper after filling mandatory data manually', async (I, caseViewPage, enterFamilyManCaseNumberEventPage, allocatedJudgeEventPage, sendCaseToGatekeeperEventPage, addHearingBookingDetailsEventPage) => {
  const caseId = await I.submitNewCaseWithData();
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
  await sendCaseToGatekeeperEventPage.enterEmail();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.sendToGatekeeper);
});

Scenario('gatekeeper drafts SDO manually', async (I, caseViewPage, draftStandardDirectionsEventPage) => {
  const caseId = await I.submitNewCaseWithData('gatekeeping');

  await I.navigateToCaseDetailsAs(config.gateKeeperUser, caseId);
  await caseViewPage.goToNewActions(config.administrationActions.draftStandardDirections);
  await draftStandardDirectionsEventPage.skipDateOfIssue();
  await draftStandardDirectionsEventPage.useAllocatedJudge('Bob Ross');
  await draftStandardDirectionsEventPage.enterDatesForDirections(directions[0]);
  draftStandardDirectionsEventPage.markAsFinal();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.draftStandardDirections);
});
