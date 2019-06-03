const config = require('../config.js');

Feature('Enter attending hearing information into the application').retry(2);

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseViewPage.goToNewActions(config.applicationActions.attendingHearing);
});

Scenario('completing half of the attending hearing section of the c110a application',
  (I, attendingHearingPage, caseViewPage) => {
    attendingHearingPage.enterInterpreter();
    attendingHearingPage.enterWelshProceedings();
    attendingHearingPage.enterIntermediary();
    I.continueAndSave();
    I.seeEventSubmissionConfirmation(config.applicationActions.attendingHearing);
    caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
    I.seeAnswerInTab(1, 'Attending the hearing', 'Interpreter', 'Yes');
    I.seeAnswerInTab(2, 'Attending the hearing', 'Give details', 'French' +
      ' translator');
    I.seeAnswerInTab(3, 'Attending the hearing', 'Spoken or written Welsh', 'No');
    I.seeAnswerInTab(4, 'Attending the hearing', 'Intermediary', 'No');
  });

Scenario('completing the attending hearing section of the c110a application',
  (I, attendingHearingPage, caseViewPage) => {
    attendingHearingPage.enterInterpreter();
    attendingHearingPage.enterWelshProceedings();
    attendingHearingPage.enterIntermediary();
    attendingHearingPage.enterDisabilityAssistance();
    attendingHearingPage.enterExtraSecurityMeasures();
    attendingHearingPage.enterSomethingElse();
    I.continueAndSave();
    I.seeEventSubmissionConfirmation(config.applicationActions.attendingHearing);
    caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
    I.seeAnswerInTab(1, 'Attending the hearing', 'Interpreter', 'Yes');
    I.seeAnswerInTab(2, 'Attending the hearing', 'Give details', 'French translator');
    I.seeAnswerInTab(3, 'Attending the hearing', 'Spoken or written Welsh', 'No');
    I.seeAnswerInTab(4, 'Attending the hearing', 'Intermediary', 'No');
    I.seeAnswerInTab(5, 'Attending the hearing', 'Facilities or assistance for a disability', 'Yes');
    I.seeAnswerInTab(6, 'Attending the hearing', 'Give details', 'learning difficulty');
    I.seeAnswerInTab(7, 'Attending the hearing', 'Separate waiting room or other security measures', 'Yes');
    I.seeAnswerInTab(8, 'Attending the hearing', 'Give details', 'Separate waiting rooms');
    I.seeAnswerInTab(9, 'Attending the hearing', 'Something else', 'Yes');
    I.seeAnswerInTab(10, 'Attending the hearing', 'Give details', 'I need this for this person');
  });
