const config = require('../config.js');
const respondents = require('../fixtures/respondents.js');

Feature('Enter respondents').retry(2);

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseViewPage.goToNewActions(config.applicationActions.enterRespondents);
});

Scenario('Entering information for respondent and submitting', (I, enterRespondentsPage, caseViewPage) => {
  enterRespondentsPage.enterRespondent('firstRespondent', respondents[0]);
  I.continueAndSubmit();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterRespondents);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
});

Scenario('Entering all information for first respondent and an additional respondent', (I, enterRespondentsPage, caseViewPage) => {
  enterRespondentsPage.enterRespondent('firstRespondent', respondents[0]);
  enterRespondentsPage.enterRelationshipToChild('firstRespondent', 'mock reason');
  enterRespondentsPage.enterContactDetailsHidden('firstRespondent', 'Yes', 'mock reason');
  enterRespondentsPage.enterLitigationIssues('firstRespondent', 'No');
  I.click(enterRespondentsPage.addRespondent);
  enterRespondentsPage.enterRespondent('additional_0', respondents[1]);
  enterRespondentsPage.enterRelationshipToChild('additional_0', 'mock reason');
  enterRespondentsPage.enterContactDetailsHidden('additional_0', 'Yes', 'mock reason');
  enterRespondentsPage.enterLitigationIssues('additional_0', 'No');
  I.continueAndSubmit();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterRespondents);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeAnswerInTab(1, 'Respondent 1', 'Full name', 'Joe Bloggs');
  I.seeAnswerInTab(2, 'Respondent 1', 'Date of birth', '1 Jan 1980');
  I.seeAnswerInTab(3, 'Respondent 1', 'Gender', 'Male');
  I.seeAnswerInTab(4, 'Respondent 1', 'Place of birth', 'London');
  I.seeAnswerInTab(1, 'Current address', 'Building and Street', 'Flat 2');
  I.seeAnswerInTab(2, 'Current address', '', 'Caversham House 15-17');
  I.seeAnswerInTab(3, 'Current address', '', 'Church Road');
  I.seeAnswerInTab(4, 'Current address', 'Town or City', 'Reading');
  I.seeAnswerInTab(5, 'Current address', 'Postcode/Zipcode', 'RG4 7AA');
  I.seeAnswerInTab(6, 'Current address', 'Country', 'United Kingdom');
  I.seeAnswerInTab(6, 'Respondent 1', 'Telephone number', '00000 000000');
  I.seeAnswerInTab(7, 'Respondent 1', 'What is the respondent’s relationship to the child or children in this case?',
    'mock reason');
  I.seeAnswerInTab(8, 'Respondent 1', 'Do you need contact details hidden from anyone?', 'Yes');
  I.seeAnswerInTab(9, 'Respondent 1', 'Give reason', 'mock reason');
  I.seeAnswerInTab(10, 'Respondent 1', 'Does this respondent have any issues with litigation capacity?', 'No');
  I.seeAnswerInTab(1, 'Additional respondents 1', 'Full name', 'Wayne Best');
  I.seeAnswerInTab(2, 'Additional respondents 1', 'Date of birth', '1 Jan 1955');
  I.seeAnswerInTab(3, 'Additional respondents 1', 'Gender', 'Male');
  I.seeAnswerInTab(4, 'Additional respondents 1', 'Place of birth', 'London');
  I.seeAnswerInTab(1, 'Current address', 'Building and Street', '1 Three Tuns Wynd');
  I.seeAnswerInTab(2, 'Current address', '', 'High Street');
  I.seeAnswerInTab(3, 'Current address', '', 'Stokesley');
  I.seeAnswerInTab(4, 'Current address', 'Town or City', 'Middlesbrough');
  I.seeAnswerInTab(5, 'Current address', 'Postcode/Zipcode', 'TS9 5DQ');
  I.seeAnswerInTab(6, 'Current address', 'Country', 'United Kingdom');
  I.seeAnswerInTab(6, 'Additional respondents 1', 'Telephone number', '00000 000000');
  I.seeAnswerInTab(7, 'Additional respondents 1', 'What is the respondent’s relationship to the child or children in this case?',
    'mock reason');
  I.seeAnswerInTab(8, 'Additional respondents 1', 'Do you need contact details hidden from anyone?', 'Yes');
  I.seeAnswerInTab(9, 'Additional respondents 1', 'Give reason', 'mock reason');
  I.seeAnswerInTab(10, 'Additional respondents 1', 'Does this respondent have any issues with litigation capacity?', 'No');
});
