const config = require('../config.js');
const otherProceedingData = require('../fixtures/otherProceedingData');

Feature('Enter Other Proceedings');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseViewPage.goToNewActions(config.applicationActions.enterOtherProceedings);
});

Scenario('Select not aware of any ongoing or previous proceedings', (I, enterOtherProceedingsEventPage, caseViewPage) => {
  enterOtherProceedingsEventPage.selectNoForProceeding();
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterOtherProceedings);
  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeAnswerInTab(1, 'Other proceedings', 'Are there any past or ongoing' +
    ' proceedings relevant to this case?', 'No');
});

Scenario('Select yes for ongoing or previous proceedings and fill in other proceedings one',
  async (I, enterOtherProceedingsEventPage, caseViewPage) => {
    enterOtherProceedingsEventPage.selectYesForProceeding();
    await enterOtherProceedingsEventPage.enterProceedingInformation(otherProceedingData[0]);
    I.continueAndSave();
    I.seeEventSubmissionConfirmation(config.applicationActions.enterOtherProceedings);
    caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
    I.seeAnswerInTab(1, 'Other proceedings', 'Are there any past or ongoing' +
      ' proceedings relevant to this case?', 'Yes');
    I.seeAnswerInTab(2, 'Other proceedings', 'Are these previous or ongoing proceedings?', 'Ongoing');
    I.seeAnswerInTab(3, 'Other proceedings', 'Case number', '000000');
    I.seeAnswerInTab(4, 'Other proceedings', 'Date started', '01/01/01');
    I.seeAnswerInTab(5, 'Other proceedings', 'Date ended', '02/01/01');
    I.seeAnswerInTab(6, 'Other proceedings', 'Orders made', 'Yes');
    I.seeAnswerInTab(7, 'Other proceedings', 'Judge', 'District Judge Martin' +
      ' Brown');
    I.seeAnswerInTab(8, 'Other proceedings', 'Names of children involved', 'Joe Bloggs');
    I.seeAnswerInTab(9, 'Other proceedings', 'Name of guardian', 'John Smith');
    I.seeAnswerInTab(10, 'Other proceedings', 'Is the same guardian needed?', 'Yes');
  });

Scenario('Select yes for ongoing or previous proceedings and fill in multiple proceedings',
  async (I, enterOtherProceedingsEventPage, caseViewPage) => {
    await enterOtherProceedingsEventPage.selectYesForProceeding();
    await enterOtherProceedingsEventPage.enterProceedingInformation(otherProceedingData[0]);
    enterOtherProceedingsEventPage.addNewProceeding();
    await enterOtherProceedingsEventPage.enterProceedingInformation(otherProceedingData[1]);
    I.continueAndSave();
    I.seeEventSubmissionConfirmation(config.applicationActions.enterOtherProceedings);
    caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
    I.seeAnswerInTab(1, 'Other proceedings', 'Are there any past or ongoing' +
      ' proceedings relevant to this case?', 'Yes');
    I.seeAnswerInTab(2, 'Other proceedings', 'Are these previous or ongoing proceedings?', 'Ongoing');
    I.seeAnswerInTab(3, 'Other proceedings', 'Case number', '000000');
    I.seeAnswerInTab(4, 'Other proceedings', 'Date started', '01/01/01');
    I.seeAnswerInTab(5, 'Other proceedings', 'Date ended', '02/01/01');
    I.seeAnswerInTab(6, 'Other proceedings', 'Orders made', 'Yes');
    I.seeAnswerInTab(7, 'Other proceedings', 'Judge', 'District Judge Martin' +
      ' Brown');
    I.seeAnswerInTab(8, 'Other proceedings', 'Names of children involved', 'Joe Bloggs');
    I.seeAnswerInTab(9, 'Other proceedings', 'Name of guardian', 'John Smith');
    I.seeAnswerInTab(10, 'Other proceedings', 'Is the same guardian needed?', 'Yes');
    I.seeAnswerInTab(1, 'Additional proceedings 1', 'Are these previous or ongoing proceedings?', 'Previous');
    I.seeAnswerInTab(2, 'Additional proceedings 1', 'Case number', '000123');
    I.seeAnswerInTab(3, 'Additional proceedings 1', 'Date started', '02/02/02');
    I.seeAnswerInTab(4, 'Additional proceedings 1', 'Date ended', '03/03/03');
    I.seeAnswerInTab(5, 'Additional proceedings 1', 'Orders made', 'Yes');
    I.seeAnswerInTab(6, 'Additional proceedings 1', 'Judge', 'District Judge Martin' +
      ' Brown');
    I.seeAnswerInTab(7, 'Additional proceedings 1', 'Names of children involved', 'James Simpson');
    I.seeAnswerInTab(8, 'Additional proceedings 1', 'Name of guardian', 'David Burns');
    I.seeAnswerInTab(9, 'Additional proceedings 1', 'Is the same guardian needed?', 'Yes');
  });
