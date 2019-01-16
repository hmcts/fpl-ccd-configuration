const config = require('../config.js');

Feature('Change case name').retry(2);

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseViewPage.goToNewActions(config.applicationActions.changeCaseName);
});

Scenario('changed case name', (I, changeCaseNamePage) => {
  changeCaseNamePage.changeCaseName();
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.changeCaseName);
});
