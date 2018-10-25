const config = require('../config.js');

Feature('Enter attending hearing information into the application');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
  caseViewPage.goToNewActions(config.applicationActions.attendingHearing);
});

Scenario('completing half of the attending hearing section of the c110a application',
  (I, attendingHearingPage, caseViewPage) => {
  attendingHearingPage.enterInterpreter();
  attendingHearingPage.enterIntermediary();
  attendingHearingPage.enterLitigationIssues();
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.attendingHearing);
  caseViewPage.selectTab(caseViewPage.tabs.legalOpinion);
  I.seeAnswerInTab(1, 'Attending the court', 'Any Interpreter required?', 'Yes');
  I.seeAnswerInTab(2, 'Attending the court', 'Give details', 'French' +
    ' translator');
  I.seeAnswerInTab(3, 'Attending the court', 'Any intermediary required?', 'No');
  I.seeAnswerInTab(4, 'Attending the court', 'Litigation capacity issues', 'No');
});

Scenario('completing the attending hearing section of the c110a application',
  (I, attendingHearingPage, caseViewPage) => {
  attendingHearingPage.enterInterpreter();
  attendingHearingPage.enterIntermediary();
  attendingHearingPage.enterLitigationIssues();
  attendingHearingPage.enterLearningDisability();
  attendingHearingPage.enterWelshProceedings();
  attendingHearingPage.enterExtraSecurityMeasures();
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.attendingHearing);
  caseViewPage.selectTab(caseViewPage.tabs.legalOpinion);
  I.seeAnswerInTab(1, 'Attending the court', 'Any Interpreter required?', 'Yes');
  I.seeAnswerInTab(2, 'Attending the court', 'Give details', 'French' +
    ' translator');
  I.seeAnswerInTab(3, 'Attending the court', 'Any intermediary required?', 'No');
  I.seeAnswerInTab(4, 'Attending the court', 'Litigation capacity issues', 'No');
  I.seeAnswerInTab(5, 'Attending the court', 'Learning disability issues', 'Yes');
  I.seeAnswerInTab(6, 'Attending the court', 'Give details', 'learning' +
    ' difficulty');
  I.seeAnswerInTab(7, 'Attending the court', 'Do you need Welsh proceedings', 'No');
  I.seeAnswerInTab(8, 'Attending the court', 'Any security needed?', 'Yes');
  I.seeAnswerInTab(8, 'Attending the court', 'Give details', 'Separate' +
    ' waiting rooms');
});
