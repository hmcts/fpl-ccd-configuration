const config = require('../config.js');
const other = require('../fixtures/others.js');

Feature('Enter others who should be given notice');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
  caseViewPage.goToNewActions(config.applicationActions.enterOthers);
});

Scenario('Enter other\'s details in c110a application', (I, enterOthersPage, caseViewPage) => {
  enterOthersPage.enterOtherDetails(other);
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.enterOthers);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeAnswerInTab(1, 'Other', 'Full name', 'John Smith');
  I.seeAnswerInTab(2, 'Other', 'Date of birth', '1 Jan 1985');
  I.seeAnswerInTab(3, 'Other', 'Gender', 'Male');
  I.seeAnswerInTab(4, 'Other', 'Place of birth', 'Scotland');
  I.seeAnswerInTab(5, 'Other', 'Current address', '123 London lane, London');
  I.seeAnswerInTab(6, 'Other', 'Telephone number', '07888288288');
});

Scenario('Complete entering others details in the c110a application', (I, enterOthersPage, caseViewPage) => {
  enterOthersPage.enterOtherDetails(other);
  enterOthersPage.enterRelationshipToChild('Tim Smith');
  enterOthersPage.enterContactDetailsHidden('Yes');
  enterOthersPage.enterLitigationIssues('No');
  enterOthersPage.addOther();
  enterOthersPage.enterOtherDetails(other);
  enterOthersPage.enterRelationshipToChild('Tim Smith');
  enterOthersPage.enterContactDetailsHidden('Yes');
  enterOthersPage.enterLitigationIssues('No');
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeAnswerInTab(1, 'Other', 'Full name', 'John Smith');
  I.seeAnswerInTab(2, 'Other', 'Date of birth', '1 Jan 1985');
  I.seeAnswerInTab(3, 'Other', 'Gender', 'Male');
  I.seeAnswerInTab(4, 'Other', 'Place of birth', 'Scotland');
  I.seeAnswerInTab(5, 'Other', 'Current address', '123 London lane, London');
  I.seeAnswerInTab(6, 'Other', 'Telephone number', '07888288288');
  I.seeAnswerInTab(7, 'Other', 'What is this person’s relationship to the child or children in this case?', 'Tim Smith');
  I.seeAnswerInTab(8, 'Other', 'Do you need contact details hidden from other parties?', 'Yes');
  I.seeAnswerInTab(9, 'Other', 'Give reason', 'mock reason');
  I.seeAnswerInTab(10, 'Other', 'Does this person have any issues with litigation capacity?', 'No');

  I.seeAnswerInTab(1, 'Additional others 1', 'Full name', 'John Smith');
  I.seeAnswerInTab(2, 'Additional others 1', 'Date of birth', '1 Jan 1985');
  I.seeAnswerInTab(3, 'Additional others 1', 'Gender', 'Male');
  I.seeAnswerInTab(4, 'Additional others 1', 'Place of birth', 'Scotland');
  I.seeAnswerInTab(5, 'Additional others 1', 'Current address', '123 London lane, London');
  I.seeAnswerInTab(6, 'Additional others 1', 'Telephone number', '07888288288');
  I.seeAnswerInTab(7, 'Additional others 1', 'What is this person’s relationship to the child or children in this case?', 'Tim Smith');
  I.seeAnswerInTab(8, 'Additional others 1', 'Do you need contact details hidden from other parties?', 'Yes');
  I.seeAnswerInTab(9, 'Additional others 1', 'Give reason', 'mock reason');
  I.seeAnswerInTab(10, 'Additional others 1', 'Does this person have any issues with litigation capacity?', 'No');
});
