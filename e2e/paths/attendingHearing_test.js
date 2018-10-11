const config = require('../config.js');

Feature('Enter attending hearing information into the application');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
  caseViewPage.goToNewActions(config.applicationActions.attendingHearing);
});

Scenario('completing half of the attending hearing section of the c110a application', (I, attendingHearingPage) => {
  attendingHearingPage.enterInterpreter();
  attendingHearingPage.enterIntermediary();
  attendingHearingPage.enterLitigationIssues();
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.attendingHearing);
});

Scenario('completing the attending hearing section of the c110a application', (I, attendingHearingPage) => {
  attendingHearingPage.enterInterpreter();
  attendingHearingPage.enterIntermediary();
  attendingHearingPage.enterLitigationIssues();
  attendingHearingPage.enterLearningDisability();
  attendingHearingPage.enterWelshProceedings();
  attendingHearingPage.enterSecurity();
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.attendingHearing);
});
