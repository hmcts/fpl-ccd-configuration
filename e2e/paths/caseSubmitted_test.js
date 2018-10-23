const config = require('../config.js');

Feature('Submit Case');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
  I.selectOption(caseViewPage.actionsDropdown, config.applicationActions.submitCase);
  I.click(caseViewPage.goButton);
});


Scenario('Submitting case', (I, caseViewPage) => {
  I.click('Submit');
  I.waitForElement('.tabs', 10);
  I.seeEventSubmissionConfirmation(config.applicationActions.submitCase);
  I.dontSee(caseViewPage.actionsDropdown);
});
