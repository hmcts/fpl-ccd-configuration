const config = require('../config.js');

Feature('Delete Case').retry(2);

Before((I) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
});

Scenario('Can delete an application and not have actions within it anymore', (I, caseViewPage, deleteApplicationEventPage) => {
  caseViewPage.goToNewActions(config.applicationActions.deleteApplication);
  deleteApplicationEventPage.tickDeletionConsent();
  I.click('Continue');
  I.waitForElement('.check-your-answers');
  I.click('Delete application');
  I.seeEventSubmissionConfirmation(config.applicationActions.deleteApplication);
  I.dontSee(caseViewPage.actionsDropdown);
});
