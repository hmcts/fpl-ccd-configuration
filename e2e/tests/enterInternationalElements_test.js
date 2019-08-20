const config = require('../config.js');

Feature('Enter international element');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseViewPage.goToNewActions(config.applicationActions.enterInternationalElement);
});

Scenario('completing half of the international element section of the c110a' +
  ' application', (I, enterInternationalElementEventPage, caseViewPage) => {
  enterInternationalElementEventPage.halfFillForm();
  I.see('Give reason');
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterInternationalElement);
  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeAnswerInTab(1, 'International element', 'Are there any suitable carers outside of the UK?', 'Yes');
  I.seeAnswerInTab(2, 'International element', 'Give reason', 'test');
  I.seeAnswerInTab(3, 'International element', 'Are you aware of any' +
    ' significant events that have happened outside the UK?', 'Yes');
  I.seeAnswerInTab(4, 'International element', 'Give reason', 'test');
});

Scenario('completed international element of the c110a application', (I, enterInternationalElementEventPage, caseViewPage) => {
  enterInternationalElementEventPage.fillForm();
  I.see('Give reason');
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterInternationalElement);
  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeAnswerInTab(1, 'International element', 'Are there any suitable carers outside of the UK?', 'Yes');
  I.seeAnswerInTab(2, 'International element', 'Give reason', 'test');
  I.seeAnswerInTab(3, 'International element', 'Are you aware of any' +
    ' significant events that have happened outside the UK?', 'Yes');
  I.seeAnswerInTab(4, 'International element', 'Give reason', 'test');
  I.seeAnswerInTab(5, 'International element', 'Are you aware of any issues' +
    ' with the jurisdiction of this case - for example under the Brussels 2' +
    ' regulation?', 'No');
  I.seeAnswerInTab(6, 'International element', 'Are you aware of any' +
    ' proceedings outside the UK?', 'Yes');
  I.seeAnswerInTab(7, 'International element', 'Give reason', 'test');
  I.seeAnswerInTab(8, 'International element', 'Has, or should, a government or central authority in another ' +
    'country been involved in this case?', 'Yes');
  I.seeAnswerInTab(9, 'International element', 'Give reason', 'International involvement reason');
});
