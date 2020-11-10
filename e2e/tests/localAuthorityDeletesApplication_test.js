const config = require('../config.js');

let caseId;
let caseName;

Feature('Application draft (empty draft)');

BeforeSuite(async ({I}) => {
  caseName = `Case ${new Date().toISOString()}`;
  caseId = await I.logInAndCreateCase(config.swanseaLocalAuthorityUserOne, caseName);
});

Before(async ({I}) => await I.navigateToCaseDetails(caseId));

Scenario('local authority tries to submit incomplete case', async ({I, caseViewPage, submitApplicationEventPage}) => {
  await caseViewPage.goToNewActions(config.applicationActions.submitCase);
  submitApplicationEventPage.giveConsent();
  await I.goToNextPage();
  I.waitForElement('.error-summary-list');
  I.see('Add the orders and directions sought');
  I.see('Add the hearing urgency details');
  I.see('Add the grounds for the application');
  I.see('Add social work documents, or details of when you\'ll send them');
  I.see('Add your organisation\'s details');
  I.see('Add the applicant\'s solicitor\'s details');
  I.see('Add the child\'s details');
  I.see('Add the respondents\' details');
  I.see('Add the allocation proposal');
});

Scenario('local authority deletes application', async ({I, caseViewPage, deleteApplicationEventPage, caseListPage}) => {
  await caseViewPage.goToNewActions(config.applicationActions.deleteApplication);
  deleteApplicationEventPage.tickDeletionConsent();
  await I.goToNextPage();
  await I.goToNextPage2(() => I.click('Delete application'));
  await caseListPage.searchForCasesWithName(caseName);
  I.see('No cases found.');
});
