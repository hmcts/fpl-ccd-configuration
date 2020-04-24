const config = require('../config.js');

let caseId;

Feature('Application draft (empty draft)');

Before(async (I) => {
  if (!caseId) {
    // eslint-disable-next-line require-atomic-updates
    caseId = await I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);

    console.log(`Application draft ${caseId} has been created`);
  } else {
    await I.navigateToCaseDetails(caseId);
  }
});

Scenario('local authority tries to submit incomplete case', async (I, caseViewPage, submitApplicationEventPage) => {
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

Scenario('local authority deletes application', async (I, caseViewPage, deleteApplicationEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.deleteApplication);
  deleteApplicationEventPage.tickDeletionConsent();
  await I.completeEvent('Delete application');
  I.seeEventSubmissionConfirmation(config.applicationActions.deleteApplication);
  I.dontSee(caseViewPage.actionsDropdown);
});
