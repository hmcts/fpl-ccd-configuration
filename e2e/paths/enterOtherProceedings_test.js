const config = require('../config.js');
const otherProceedingData = require('../fixtures/otherProceedingData');

Feature('Enter Other Proceedings').retry(2);

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseViewPage.goToNewActions(config.applicationActions.enterOtherProceedings);
});

Scenario('Select not aware of any ongoing or previous proceedings', (I, enterOtherProceedingsPage, caseViewPage) => {
  enterOtherProceedingsPage.selectNoForProceeding();
  I.continueAndSubmit();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterOtherProceedings);
  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeAnswerInTab(1, 'Other proceedings', 'Are you aware of any ongoing or' +
    ' previous proceedings?', 'No');
});

Scenario('Select yes for ongoing or previous proceedings and fill in information',
  (I, enterOtherProceedingsPage, caseViewPage) => {
    enterOtherProceedingsPage.selectYesForProceeding();
    enterOtherProceedingsPage.enterProceedingInformation(otherProceedingData);
    I.continueAndSubmit();
    I.seeEventSubmissionConfirmation(config.applicationActions.enterOtherProceedings);
    caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
    I.seeAnswerInTab(1, 'Other proceedings', 'Are you aware of any ongoing or' +
      ' previous proceedings?', 'Yes');
    I.seeAnswerInTab(2, 'Other proceedings', 'Proceeding status', 'Ongoing');
    I.seeAnswerInTab(3, 'Other proceedings', 'Case number', '000000');
    I.seeAnswerInTab(4, 'Other proceedings', 'Started', '01/01/01');
    I.seeAnswerInTab(5, 'Other proceedings', 'Ended', '02/01/01');
    I.seeAnswerInTab(6, 'Other proceedings', 'Orders made', 'Yes');
    I.seeAnswerInTab(7, 'Other proceedings', 'Judge', 'District Judge Martin' +
      ' Brown');
    I.seeAnswerInTab(8, 'Other proceedings', 'Children subject to proceedings', 'Joe Bloggs');
    I.seeAnswerInTab(9, 'Other proceedings', 'Guardian', 'John Smith');
    I.seeAnswerInTab(10, 'Other proceedings', 'Same guardian needed?', 'Yes');
  });


