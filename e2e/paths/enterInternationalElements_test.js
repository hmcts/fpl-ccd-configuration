const config = require('../config.js');

Feature('Enter international elements');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
  caseViewPage.goToNewActions(config.applicationActions.enterInternationalElement);
});

Scenario('completing half of the international elements section of the c110a' +
  ' application', (I, enterInternationalElementsPage, caseViewPage) => {
  enterInternationalElementsPage.halfFillForm();
  I.see('Give reason');
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.enterInternationalElement);
  caseViewPage.selectTab(caseViewPage.tabs.legalOpinion);
  I.seeAnswerInTab(1, 'International elements', 'Is there anyone in the' +
    ' genogram outside the UK who has been assessed as a possible carer?', 'Yes');
  I.seeAnswerInTab(2, 'International elements', 'Give reason', 'test');
  I.seeAnswerInTab(3, 'International elements', 'Are you aware of any' +
    ' significant events that have happened outside the UK?', 'Yes');
  I.seeAnswerInTab(4, 'International elements', 'Give reason', 'test');
});

Scenario('completed international elements of the c110a application', (I, enterInternationalElementsPage, caseViewPage) => {
  enterInternationalElementsPage.fillForm();
  I.see('Give reason');
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.enterInternationalElement);
  caseViewPage.selectTab(caseViewPage.tabs.legalOpinion);
  I.seeAnswerInTab(1, 'International elements', 'Is there anyone in the' +
    ' genogram outside the UK who has been assessed as a possible carer?', 'Yes');
  I.seeAnswerInTab(2, 'International elements', 'Give reason', 'test');
  I.seeAnswerInTab(3, 'International elements', 'Are you aware of any' +
    ' significant events that have happened outside the UK?', 'Yes');
  I.seeAnswerInTab(4, 'International elements', 'Give reason', 'test');
  I.seeAnswerInTab(5, 'International elements', 'Are you aware of any issues' +
    ' with the jurisdiction of this case - for example under the Brussels 2' +
    ' regulation?', 'No');
  I.seeAnswerInTab(6, 'International elements', 'Are you aware of any' +
    ' proceedings outside the UK?', 'Yes');
  I.seeAnswerInTab(7, 'International elements', 'Give reason', 'test');
});
