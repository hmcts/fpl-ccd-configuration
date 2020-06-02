const config = require('../config.js');
const dateFormat = require('dateformat');
const mandatorySubmissionFields = require('../fixtures/mandatorySubmissionFields.json');

let caseId;

Feature('Local authority corrects returned application');

BeforeSuite(async (I) => {
  caseId = await I.submitNewCaseWithData(mandatorySubmissionFields);
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
});

Scenario('Admin returns application to the LA', async (I, caseViewPage, returnApplicationEventPage) => {
  await caseViewPage.goToNewActions(config.administrationActions.returnApplication);
  returnApplicationEventPage.selectApplicationIncorrect();
  returnApplicationEventPage.enterRejectionNote();
  await I.completeEvent('Save and continue', {summary: 'summary', description: 'description'});
  await I.seeEventSubmissionConfirmation(config.administrationActions.returnApplication);
});

Scenario('LA makes corrections to the application', async (I, caseViewPage, enterApplicantEventPage, submitApplicationEventPage) => {
  const now = new Date();
  const formattedDate = dateFormat(now, 'd mmmm yyyy');
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.dontSee('mockSubmittedForm.pdf');
  caseViewPage.selectTab(caseViewPage.tabs.overview);
  I.seeInTab(['Return details', 'Date submitted'], formattedDate);
  I.seeInTab(['Return details', 'Date returned'], formattedDate);
  I.seeInTab(['Return details', 'Document'], 'mockSubmittedForm_returned.pdf');
  I.seeInTab(['Return details', 'Reason for rejection'], 'Application Incorrect');
  I.seeInTab(['Return details', 'Let the local authority know what they need to change'], 'PBA number is incorrect');
  await caseViewPage.goToNewActions(config.applicationActions.enterApplicant);
  enterApplicantEventPage.enterPbaNumber();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterApplicant);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeInTab(['Applicants 1', 'Party', 'Payment by account (PBA) number'], 'PBA1234567');
  await caseViewPage.goToNewActions(config.applicationActions.submitCase);
  submitApplicationEventPage.seeDraftApplicationFile();
  submitApplicationEventPage.giveConsent();
  await I.completeEvent('Submit');
  I.seeEventSubmissionConfirmation(config.applicationActions.submitCase);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.see('e2e_test_case.pdf');
});
