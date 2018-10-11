const config = require('../config.js');

Feature('Select hearing');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
  caseViewPage.goToNewActions(config.applicationActions.selectHearing);
});

Scenario('completing half the fields in the Select hearing section of the c110a application', (I, selectHearingPage) => {
  selectHearingPage.halfFillForm();
  I.see('Give reason');
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.selectHearing);
});

Scenario('completing the Select hearing section of the c110a application', (I, selectHearingPage) => {
  selectHearingPage.fillForm();
  I.see('Give reason');
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.selectHearing);
});
