const config = require('../config.js');

Feature('Change case name').retry(2);

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
  caseViewPage.goToNewActions(config.applicationActions.changeCaseName);
});

Scenario('changed case name', (I, changeCaseNamePage) => {
  changeCaseNamePage.changeCaseName();
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.changeCaseName);
});