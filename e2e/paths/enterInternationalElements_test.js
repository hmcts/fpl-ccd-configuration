const config = require('../config.js');

Feature('Enter international element').retry(2);

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseViewPage.goToNewActions(config.applicationActions.enterInternationalElement);
});

Scenario('completing half of the international element section of the c110a' +
  ' application', (I, enterInternationalElementsPage, caseViewPage) => {
  enterInternationalElementsPage.halfFillForm();
  I.see('Give reason');
  I.continueAndSubmit();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterInternationalElement);
  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeAnswerInTab(1, 'International element', 'Is there anyone in the' +
    ' genogram outside the UK who has been assessed as a possible carer?', 'Yes');
  I.seeAnswerInTab(2, 'International element', 'Give reason', 'test');
  I.seeAnswerInTab(3, 'International element', 'Are you aware of any' +
    ' significant events that have happened outside the UK?', 'Yes');
  I.seeAnswerInTab(4, 'International element', 'Give reason', 'test');
});

Scenario('completed international element of the c110a application', (I, enterInternationalElementsPage, caseViewPage) => {
  enterInternationalElementsPage.fillForm();
  I.see('Give reason');
  I.continueAndSubmit();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterInternationalElement);
  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeAnswerInTab(1, 'International element', 'Is there anyone in the' +
    ' genogram outside the UK who has been assessed as a possible carer?', 'Yes');
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
});
