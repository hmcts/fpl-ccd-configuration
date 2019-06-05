const config = require('../config.js');
const otherProceedingData = require('../fixtures/otherProceedingData');

Feature('Enter Other Proceedings').retry(2);

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseViewPage.goToNewActions(config.applicationActions.enterOtherProceedings);
});

Scenario('Select not aware of any ongoing or previous proceedings', (I, enterOtherProceedingsPage, caseViewPage) => {
  enterOtherProceedingsPage.selectNoForProceeding();
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterOtherProceedings);
  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeAnswerInTab(1, 'Other proceedings', 'Are there any past or ongoing' +
    ' proceedings relevant to this case?', 'No');
});

Scenario('Select yes for ongoing or previous proceedings and fill in other proceedings one',
  (I, enterOtherProceedingsPage, caseViewPage) => {
    enterOtherProceedingsPage.selectYesForProceeding();
    enterOtherProceedingsPage.enterProceedingInformation(otherProceedingData[0]);
    I.continueAndSave();
    I.seeEventSubmissionConfirmation(config.applicationActions.enterOtherProceedings);
    caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
    I.seeAnswerInTab(1, 'Other proceedings', 'Are there any past or ongoing' +
      ' proceedings relevant to this case?', 'Yes');
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

Scenario('Select yes for ongoing or previous proceedings and fill in multiple other proceedings',
  (I, enterOtherProceedingsPage, caseViewPage) => {
    enterOtherProceedingsPage.selectYesForProceeding();
    enterOtherProceedingsPage.enterProceedingInformation(otherProceedingData[0]);
    enterOtherProceedingsPage.addNewProceeding();
    enterOtherProceedingsPage.enterProceedingInformation(otherProceedingData[1]);
    I.continueAndSave();
    I.seeEventSubmissionConfirmation(config.applicationActions.enterOtherProceedings);
    caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
    I.seeAnswerInTab(1, 'Other proceedings', 'Are there any past or ongoing' +
      ' proceedings relevant to this case?', 'Yes');
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
    I.seeAnswerInTab(1, 'Additional proceedings 1', 'Proceeding status', 'Previous');
    I.seeAnswerInTab(2, 'Additional proceedings 1', 'Case number', '000123');
    I.seeAnswerInTab(3, 'Additional proceedings 1', 'Started', '02/02/02');
    I.seeAnswerInTab(4, 'Additional proceedings 1', 'Ended', '03/03/03');
    I.seeAnswerInTab(5, 'Additional proceedings 1', 'Orders made', 'Yes');
    I.seeAnswerInTab(6, 'Additional proceedings 1', 'Judge', 'District Judge Martin' +
      ' Brown');
    I.seeAnswerInTab(7, 'Additional proceedings 1', 'Children subject to proceedings', 'James Simpson');
    I.seeAnswerInTab(8, 'Additional proceedings 1', 'Guardian', 'David Burns');
    I.seeAnswerInTab(9, 'Additional proceedings 1', 'Same guardian needed?', 'Yes');
  });
