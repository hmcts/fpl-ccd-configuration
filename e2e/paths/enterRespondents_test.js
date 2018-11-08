const config = require('../config.js');
const respondent = require('../fixtures/respondent.js');

Feature('Enter respondents').retry(2);

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
  caseViewPage.goToNewActions(config.applicationActions.enterRespondents);
});

Scenario('Entering information for respondent and submitting', (I, enterRespondentsPage, caseViewPage) => {
  enterRespondentsPage.enterRespondent('firstRespondent', respondent);
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.enterRespondents);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
});

Scenario('Entering all information for first respondent and an additional respondent', (I, enterRespondentsPage, caseViewPage) => {
  enterRespondentsPage.enterRespondent('firstRespondent', respondent);
  enterRespondentsPage.enterRelationshipToChild('firstRespondent', 'mock reason');
  enterRespondentsPage.enterContactDetailsHidden('firstRespondent', 'Yes', 'mock reason');
  enterRespondentsPage.enterLitigationIssues('firstRespondent', 'No');
  I.click(enterRespondentsPage.addRespondent);
  enterRespondentsPage.enterRespondent('additional_0', respondent);
  enterRespondentsPage.enterRelationshipToChild('additional_0', 'mock reason');
  enterRespondentsPage.enterContactDetailsHidden('additional_0', 'Yes', 'mock reason');
  enterRespondentsPage.enterLitigationIssues('additional_0', 'No');
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.enterRespondents);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeAnswerInTab(1, 'Respondent 1', 'Full name', 'Joe Bloggs');
  I.seeAnswerInTab(2, 'Respondent 1', 'Date of birth', '1 Jan 1980');
  I.seeAnswerInTab(3, 'Respondent 1', 'Gender', 'Male');
  I.seeAnswerInTab(4, 'Respondent 1', 'Place of birth', 'London');
  I.seeAnswerInTab(5, 'Respondent 1', 'Current address', 'London Lane, London, SE1 1AA');
  I.seeAnswerInTab(6, 'Respondent 1', 'Telephone number', '00000 000000');
  I.seeAnswerInTab(7, 'Respondent 1', 'What is the respondent’s relationship to the child or children in this case?',
    'mock reason');
  I.seeAnswerInTab(8, 'Respondent 1', 'Do you need contact details hidden from anyone?', 'Yes');
  I.seeAnswerInTab(9, 'Respondent 1', 'Give reason', 'mock reason');
  I.seeAnswerInTab(10, 'Respondent 1', 'Does this respondent have any issues with litigation capacity?', 'No');
  I.seeAnswerInTab(1, 'Additional respondents 1', 'Full name', 'Joe Bloggs');
  I.seeAnswerInTab(2, 'Additional respondents 1', 'Date of birth', '1 Jan 1980');
  I.seeAnswerInTab(3, 'Additional respondents 1', 'Gender', 'Male');
  I.seeAnswerInTab(4, 'Additional respondents 1', 'Place of birth', 'London');
  I.seeAnswerInTab(5, 'Additional respondents 1', 'Current address', 'London Lane, London, SE1 1AA');
  I.seeAnswerInTab(6, 'Additional respondents 1', 'Telephone number', '00000 000000');
  I.seeAnswerInTab(7, 'Additional respondents 1', 'What is the respondent’s relationship to the child or children in this case?',
    'mock reason');
  I.seeAnswerInTab(8, 'Additional respondents 1', 'Do you need contact details hidden from anyone?', 'Yes');
  I.seeAnswerInTab(9, 'Additional respondents 1', 'Give reason', 'mock reason');
  I.seeAnswerInTab(10, 'Additional respondents 1', 'Does this respondent have any issues with litigation capacity?', 'No');
});
