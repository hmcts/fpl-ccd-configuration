const config = require('../config.js');

let caseId;
let caseName;

Feature('Application draft (empty draft)');

BeforeSuite(async I => {
  caseName = `Case ${new Date().toISOString()}`;
  caseId = await I.logInAndCreateCase(config.swanseaLocalAuthorityUserOne, caseName);
});

Before(async I => await I.navigateToCaseDetails(caseId));
//EUI-2060
xScenario('local authority tries to submit incomplete case', async (I, caseViewPage, submitApplicationEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.submitCase);
  submitApplicationEventPage.giveConsent();
  I.click('Continue');
  I.waitForElement('.error-summary-list');
  I.see('Tell us the status of all documents including those that you haven\'t uploaded');
  I.see('You need to add details to orders and directions needed');
  I.see('You need to add details to children');
  I.see('You need to add details to applicant');
  I.see('You need to add details to hearing needed');
  I.see('You need to add details to grounds for the application');
});

Scenario('local authority deletes application', async (I, caseViewPage, deleteApplicationEventPage, caseListPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.deleteApplication);
  deleteApplicationEventPage.tickDeletionConsent();
  await I.completeEvent('Delete application');
  await caseListPage.searchForCasesWithName(caseName);
  I.see('No cases found.');
});
