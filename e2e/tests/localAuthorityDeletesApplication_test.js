const config = require('../config.js');

Feature('Application draft deletion');

Before(async (I) => {
  await I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
});

Scenario('local authority deletes application', (I, caseViewPage, deleteApplicationEventPage, eventSummaryPage) => {
  caseViewPage.goToNewActions(config.applicationActions.deleteApplication);
  deleteApplicationEventPage.tickDeletionConsent();
  I.click('Continue');
  I.waitForElement('.check-your-answers');
  eventSummaryPage.submit('Delete application');
  I.seeEventSubmissionConfirmation(config.applicationActions.deleteApplication);
  I.dontSee(caseViewPage.actionsDropdown);
});
