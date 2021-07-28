const config = require('../config.js');

const selectCourtError = 'Select court in the Select court to issue';

let caseId;

Feature('Local authority select court');

async function setupScenario(I) {
  if (!caseId) { caseId = await I.submitNewCase(config.hillingdonLocalAuthorityUserOne); }
  await I.navigateToCaseDetailsAs(config.hillingdonLocalAuthorityUserOne, caseId);
}

Scenario('Local authority select court to issue', async ({I, caseViewPage, selectCourtEventPage}) => {
  await setupScenario(I);

  caseViewPage.selectTab(caseViewPage.tabs.startApplication);
  caseViewPage.checkTaskIsNotStarted(config.applicationActions.selectCourt);
  await caseViewPage.checkTasksContainsError(selectCourtError);

  await caseViewPage.goToNewActions(config.applicationActions.selectCourt);
  await selectCourtEventPage.selectCourt('Family Court sitting at West London');
  await I.seeCheckAnswersAndCompleteEvent('Save and continue');
  await I.seeEventSubmissionConfirmation(config.applicationActions.selectCourt);

  caseViewPage.selectTab(caseViewPage.tabs.startApplication);
  await caseViewPage.checkTaskIsFinished(config.applicationActions.selectCourt);
  await caseViewPage.checkTasksDoesNotContainError(selectCourtError);
});
