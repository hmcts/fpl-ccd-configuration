const config = require('../config.js');

Feature('Enter attending hearing information into the application').retry(2);

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseViewPage.goToNewActions(config.applicationActions.attendingHearing);
});

Scenario('completing half of the attending hearing section of the c110a application',
  (I, attendingHearingPage, caseViewPage) => {
    attendingHearingPage.enterInterpreter();
    attendingHearingPage.enterIntermediary();
    attendingHearingPage.enterLitigationIssues();
    I.continueAndSave();
    I.seeEventSubmissionConfirmation(config.applicationActions.attendingHearing);
    caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
    I.seeAnswerInTab(1, 'Attending the hearing', 'Any Interpreter required?', 'Yes');
    I.seeAnswerInTab(2, 'Attending the hearing', 'Give details', 'French' +
      ' translator');
    I.seeAnswerInTab(3, 'Attending the hearing', 'Any intermediary required?', 'No');
    I.seeAnswerInTab(4, 'Attending the hearing', 'Litigation capacity issues', 'No');
  });

Scenario('completing the attending hearing section of the c110a application',
  (I, attendingHearingPage, caseViewPage) => {
    attendingHearingPage.enterInterpreter();
    attendingHearingPage.enterIntermediary();
    attendingHearingPage.enterLitigationIssues();
    attendingHearingPage.enterLearningDisability();
    attendingHearingPage.enterWelshProceedings();
    attendingHearingPage.enterExtraSecurityMeasures();
    I.continueAndSave();
    I.seeEventSubmissionConfirmation(config.applicationActions.attendingHearing);
    caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
    I.seeAnswerInTab(1, 'Attending the hearing', 'Any Interpreter required?', 'Yes');
    I.seeAnswerInTab(2, 'Attending the hearing', 'Give details', 'French translator');
    I.seeAnswerInTab(3, 'Attending the hearing', 'Any intermediary required?', 'No');
    I.seeAnswerInTab(4, 'Attending the hearing', 'Litigation capacity issues', 'No');
    I.seeAnswerInTab(5, 'Attending the hearing', 'Learning disability issues', 'Yes');
    I.seeAnswerInTab(6, 'Attending the hearing', 'Give details', 'learning difficulty');
    I.seeAnswerInTab(7, 'Attending the hearing', 'Do you need Welsh proceedings', 'No');
    I.seeAnswerInTab(8, 'Attending the hearing', 'Any security needed?', 'Yes');
    I.seeAnswerInTab(9, 'Attending the hearing', 'Give details', 'Separate waiting rooms');
  });
