const config = require('../config.js');

let caseId;
let caseName;

Feature('Local authority deletes application');

async function setupScenario(I) {
  if (!caseName) { caseName = `Case ${new Date().toISOString()}`; }
  if (!caseId) { caseId = await I.submitNewCase(config.swanseaLocalAuthorityUserOne, caseName); }
}

Scenario('local authority deletes application', async ({I, caseViewPage, deleteApplicationEventPage, caseListPage, login}) => {
  await setupScenario(I);
  await login('swanseaLocalAuthorityUserOne');
  await I.navigateToCaseDetails(caseId);
  await caseViewPage.goToNewActions(config.applicationActions.deleteApplication);
  await deleteApplicationEventPage.tickDeletionConsent();
  // I.completeEvent() tries to search for the success alert, this can sometimes disappear to quickly as the user is
  // redirected to the case list due to losing permissions to view the case.
  // As such a manual completion of the event is required here
  await I.goToNextPage();
  I.click('Delete application');
  I.waitForSelector('.search-block', 20);
  caseListPage.searchForCasesWithName(caseName);
  I.grabCurrentUrl();
  I.see('No cases found.');
});
