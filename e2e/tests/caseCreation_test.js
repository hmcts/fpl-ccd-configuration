const config = require('../config.js');
const respondents = require('../fixtures/respondents.js');
const applicant = require('../fixtures/applicant.js');
const solicitor = require('../fixtures/solicitor.js');

Feature('Case creation');

Scenario('local submits case', async (I, caseViewPage, enterOrdersAndDirectionsNeededEventPage, enterHearingNeededEventPage, enterApplicantEventPage, enterChildrenEventPage, enterRespondentsEventPage, enterGroundsForApplicationEventPage, uploadDocumentsEventPage, enterAllocationProposalEventPage) => {
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
