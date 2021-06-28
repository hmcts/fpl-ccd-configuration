const config = require('../config.js');
const dateFormat = require('dateformat');
const applicant = require('../fixtures/applicant.js');
const mandatorySubmissionWithApplicationDocuments = require('../fixtures/caseData/mandatorySubmissionWithApplicationDocuments.json');

let caseId;

Feature('Local authority corrects returned application');

async function setupScenario(I) {
  if (!caseId) { caseId = await I.submitNewCaseWithData(mandatorySubmissionWithApplicationDocuments); }
}

Scenario('Admin returns application to the LA', async ({I, caseViewPage, returnApplicationEventPage}) => {
  await setupScenario(I);
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);

  await caseViewPage.goToNewActions(config.administrationActions.returnApplication);
  await returnApplicationEventPage.selectApplicationIncorrect();
  await returnApplicationEventPage.enterRejectionNote();
  await I.completeEvent('Save and continue', {summary: 'summary', description: 'description'});

  I.seeEventSubmissionConfirmation(config.administrationActions.returnApplication);
});

Scenario('LA makes corrections to the application', async ({I, caseViewPage, enterApplicantEventPage, submitApplicationEventPage}) => {
  const now = new Date();
  const formattedDate = dateFormat(now, 'd mmmm yyyy');

  await setupScenario(I);
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);

  caseViewPage.selectTab(caseViewPage.tabs.viewApplication);
  I.dontSee('mockSubmittedForm.pdf');
  caseViewPage.selectTab(caseViewPage.tabs.overview);
  I.seeInTab(['Return details', 'Date submitted'], formattedDate);
  I.seeInTab(['Return details', 'Date returned'], formattedDate);
  I.seeInTab(['Return details', 'Document'], 'mockSubmittedForm_returned.pdf');
  I.seeInTab(['Return details', 'Reason for rejection'], 'Application Incorrect');
  I.seeInTab(['Return details', 'Let the local authority know what they need to change'], 'PBA number is incorrect');

  await caseViewPage.goToNewActions(config.applicationActions.enterApplicant);
  enterApplicantEventPage.enterPbaNumber(applicant.pbaNumber);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterApplicant);

  caseViewPage.selectTab(caseViewPage.tabs.viewApplication);
  I.seeInTab(['Applicants 1', 'Party', 'Payment by account (PBA) number'], applicant.pbaNumber);

  await caseViewPage.goToNewActions(config.applicationActions.submitCase);
  submitApplicationEventPage.seeDraftApplicationFile();
  await submitApplicationEventPage.giveConsent();
  await I.completeEvent('Submit', null, true);
  I.seeEventSubmissionConfirmation(config.applicationActions.submitCase);

  caseViewPage.selectTab(caseViewPage.tabs.furtherEvidence);
  I.see('e2e_test_case.pdf');
});
