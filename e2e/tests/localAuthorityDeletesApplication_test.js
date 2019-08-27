const config = require('../config.js');

Feature('Application draft');

Before(async (I) => {
  await I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
});

Scenario('local authority deletes application', (I, caseViewPage, deleteApplicationEventPage) => {
  caseViewPage.goToNewActions(config.applicationActions.deleteApplication);
  deleteApplicationEventPage.tickDeletionConsent();
  I.click('Continue');
  I.waitForElement('.check-your-answers');
  I.click('Delete application');
  I.seeEventSubmissionConfirmation(config.applicationActions.deleteApplication);
  I.dontSee(caseViewPage.actionsDropdown);
});
