const config = require('../config.js');

Feature('Change case name');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseViewPage.goToNewActions(config.applicationActions.changeCaseName);
});

Scenario('changed case name', (I, changeCaseNameEventPage) => {
  changeCaseNameEventPage.changeCaseName();
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.changeCaseName);
});
