const config = require('../config.js');

Feature('Submit Case').retry(2);

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
  I.selectOption(caseViewPage.actionsDropdown, config.applicationActions.submitCase);
  I.click(caseViewPage.goButton);
  I.click('Submit');
  I.waitForElement('.tabs');
});

Scenario('Check add local authority event occurred', (I) => {
  I.see('Add Local Authority');
});

Scenario('Submitting case', (I, caseViewPage) => {
  I.seeEventSubmissionConfirmation(config.applicationActions.submitCase);
  I.dontSee(caseViewPage.actionsDropdown);
});
