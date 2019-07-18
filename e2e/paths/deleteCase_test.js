const config = require('../config.js');

Feature('Submit Case').retry(2);
Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  I.selectOption(caseViewPage.actionsDropdown, config.applicationActions.deleteCase);
  I.click(caseViewPage.goButton);
});

Scenario('Can delete a case and not have actions within it anymore', (I, caseViewPage, deleteCasePage) => {
  deleteCasePage.delete();
  I.click('Continue');
  I.waitForElement('.check-your-answers');
  I.click('Delete an application');
  I.seeEventSubmissionConfirmation(config.applicationActions.deleteCase);
  I.dontSee(caseViewPage.actionsDropdown);
});
