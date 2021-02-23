const config = require('../config.js');

let caseId;
let caseName;

Feature('Local authority deletes application');

BeforeSuite(async ({I}) => {
  caseName = `Case ${new Date().toISOString()}`;
  caseId = await I.submitNewCase(config.swanseaLocalAuthorityUserOne, caseName);
});

Before(async ({I}) => await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId));

Scenario('local authority deletes application', async ({I, caseViewPage, deleteApplicationEventPage, caseListPage}) => {
  await caseViewPage.goToNewActions(config.applicationActions.deleteApplication);
  await deleteApplicationEventPage.tickDeletionConsent();
  // I.completeEvent() tries to search for the success alert, this can sometimes disappear to quickly as the user is
  // redirected to the case list due to losing permissions to view the case.
  // As such a manual completion of the event is required here
  await I.goToNextPage();
  await I.retryUntilExists(() => I.click('Delete application'), '.search-block');
  await caseListPage.searchForCasesWithName(caseName);
  I.see('No cases found.');
});
