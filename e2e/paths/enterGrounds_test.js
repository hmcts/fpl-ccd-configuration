const config = require('../config.js');

Feature('EnterGrounds');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventSummary);
  caseViewPage.goToNewActions(config.applicationActions.enterGrounds);
});

Scenario('Filling in grounds for application section of c110a', (I, enterGroundsPage) => {
  enterGroundsPage.enterThresholdCriteriaDetails();
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.enterGrounds);
});
