const config = require('../config.js');

Feature('Enter attending hearings information into the application');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseViewPage.goToNewActions(config.applicationActions.attendingHearing);
});

Scenario('completing half of the attending hearings section of the c110a application',
  (I, enterAttendingHearingEventPage, caseViewPage) => {
    enterAttendingHearingEventPage.enterInterpreter();
    enterAttendingHearingEventPage.enterWelshProceedings();
    enterAttendingHearingEventPage.enterIntermediary();
    I.continueAndSave();
    I.seeEventSubmissionConfirmation(config.applicationActions.attendingHearing);
    caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
    I.seeAnswerInTab(1, 'Attending the hearings', 'Interpreter', 'Yes');
    I.seeAnswerInTab(2, 'Attending the hearings', 'Give details including person, language and dialect', 'French' +
      ' translator');
    I.seeAnswerInTab(3, 'Attending the hearings', 'Spoken or written Welsh', 'No');
    I.seeAnswerInTab(4, 'Attending the hearings', 'Intermediary', 'No');
  });

Scenario('completing the attending hearings section of the c110a application',
  (I, enterAttendingHearingEventPage, caseViewPage) => {
    enterAttendingHearingEventPage.enterInterpreter();
    enterAttendingHearingEventPage.enterWelshProceedings();
    enterAttendingHearingEventPage.enterIntermediary();
    enterAttendingHearingEventPage.enterDisabilityAssistance();
    enterAttendingHearingEventPage.enterExtraSecurityMeasures();
    enterAttendingHearingEventPage.enterSomethingElse();
    I.continueAndSave();
    I.seeEventSubmissionConfirmation(config.applicationActions.attendingHearing);
    caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
    I.seeAnswerInTab(1, 'Attending the hearings', 'Interpreter', 'Yes');
    I.seeAnswerInTab(2, 'Attending the hearings', 'Give details including person, language and dialect', 'French translator');
    I.seeAnswerInTab(3, 'Attending the hearings', 'Spoken or written Welsh', 'No');
    I.seeAnswerInTab(4, 'Attending the hearings', 'Intermediary', 'No');
    I.seeAnswerInTab(5, 'Attending the hearings', 'Facilities or assistance for a disability', 'Yes');
    I.seeAnswerInTab(6, 'Attending the hearings', 'Give details', 'learning difficulty');
    I.seeAnswerInTab(7, 'Attending the hearings', 'Separate waiting room or other security measures', 'Yes');
    I.seeAnswerInTab(8, 'Attending the hearings', 'Give details', 'Separate waiting rooms');
    I.seeAnswerInTab(9, 'Attending the hearings', 'Something else', 'Yes');
    I.seeAnswerInTab(10, 'Attending the hearings', 'Give details', 'I need this for this person');
  });
