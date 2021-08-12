const config = require('../config.js');
const dateFormat = require('dateformat');
const mandatorySubmissionWithApplicationDocuments = require('../fixtures/caseData/mandatorySubmissionFields.json');

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

Scenario('LA makes corrections to the application', async ({I, caseViewPage, enterLocalAuthorityEventPage, submitApplicationEventPage}) => {
  const now = new Date();
  const formattedDate = dateFormat(now, 'd mmmm yyyy');
  const newPbaNumber = 'PBA0082848';

  await setupScenario(I);
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);

  caseViewPage.selectTab(caseViewPage.tabs.viewApplication);
  I.dontSee('c110a.pdf');
  caseViewPage.selectTab(caseViewPage.tabs.overview);
  I.seeInTab(['Return details', 'Date submitted'], formattedDate);
  I.seeInTab(['Return details', 'Date returned'], formattedDate);
  I.seeInTab(['Return details', 'Document'], 'c110a_returned.pdf');
  I.seeInTab(['Return details', 'Reason for rejection'], 'Application Incorrect');
  I.seeInTab(['Return details', 'Let the local authority know what they need to change'], 'PBA number is incorrect');

  await caseViewPage.goToNewActions(config.applicationActions.enterLocalAuthority);
  enterLocalAuthorityEventPage.enterDetails({pbaNumber: newPbaNumber});
  await I.goToNextPage();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterLocalAuthority);

  caseViewPage.selectTab(caseViewPage.tabs.viewApplication);
  I.seeInTab(['Local authority 1', 'PBA number'], newPbaNumber);

  await caseViewPage.goToNewActions(config.applicationActions.submitCase);
  submitApplicationEventPage.seeDraftApplicationFile();
  await submitApplicationEventPage.giveConsent();
  await I.completeEvent('Submit', null, true);
  I.seeEventSubmissionConfirmation(config.applicationActions.submitCase);

  caseViewPage.selectTab(caseViewPage.tabs.furtherEvidence);
  I.see('e2e_test_case.pdf');
});
