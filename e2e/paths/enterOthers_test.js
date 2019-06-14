const config = require('../config.js');
const others = require('../fixtures/others.js');

Feature('Enter others who should be given notice').retry(2);

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseViewPage.goToNewActions(config.applicationActions.enterOthers);
});

Scenario('Enter other\'s details in c110a application', (I, enterOthersPage, caseViewPage) => {
  enterOthersPage.enterOtherDetails(others[0]);
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterOthers);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
});

Scenario('Complete entering others details in the c110a application', (I, enterOthersPage, caseViewPage) => {
  enterOthersPage.enterOtherDetails(others[0]);
  enterOthersPage.enterRelationshipToChild('Tim Smith');
  enterOthersPage.enterContactDetailsHidden('Yes');
  enterOthersPage.enterLitigationIssues('No');
  enterOthersPage.addOther();
  enterOthersPage.enterOtherDetails(others[1]);
  enterOthersPage.enterRelationshipToChild('Tim Smith');
  enterOthersPage.enterContactDetailsHidden('Yes');
  enterOthersPage.enterLitigationIssues('Yes', 'mock reason');
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterOthers);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeAnswerInTab(1, 'Person 1', 'Full name', 'John Smith');
  I.seeAnswerInTab(2, 'Person 1', 'Date of birth', '1 Jan 1985');
  I.seeAnswerInTab(3, 'Person 1', 'Gender', 'Male');
  I.seeAnswerInTab(4, 'Person 1', 'Place of birth', 'Scotland');
  I.seeAnswerInTab(1, 'Current address', 'Building and Street', 'Flat 2');
  I.seeAnswerInTab(2, 'Current address', '', 'Caversham House 15-17');
  I.seeAnswerInTab(3, 'Current address', '', 'Church Road');
  I.seeAnswerInTab(4, 'Current address', 'Town or City', 'Reading');
  I.seeAnswerInTab(5, 'Current address', 'Postcode/Zipcode', 'RG4 7AA');
  I.seeAnswerInTab(6, 'Current address', 'Country', 'United Kingdom');
  I.seeAnswerInTab(6, 'Person 1', 'Telephone number', '07888288288');
  I.seeAnswerInTab(7, 'Person 1', 'What is this person’s relationship to the child or children in this case?', 'Tim Smith');
  I.seeAnswerInTab(8, 'Person 1', 'Do you need contact details hidden from other parties?', 'Yes');
  I.seeAnswerInTab(9, 'Person 1', 'Give reason', 'mock reason');
  I.seeAnswerInTab(10, 'Person 1', 'Do you believe this person will have problems with litigation capacity (understanding what\'s happening in the case)?', 'No');

  I.seeAnswerInTab(1, 'Other person 1', 'Full name', 'Paul Wilsdon');
  I.seeAnswerInTab(2, 'Other person 1', 'Date of birth', '1 Jan 1984');
  I.seeAnswerInTab(3, 'Other person 1', 'Gender', 'Male');
  I.seeAnswerInTab(4, 'Other person 1', 'Place of birth', 'Wales');
  I.seeAnswerInTab(1, 'Current address', 'Building and Street', '1 Three Tuns Wynd');
  I.seeAnswerInTab(2, 'Current address', '', 'High Street');
  I.seeAnswerInTab(3, 'Current address', '', 'Stokesley');
  I.seeAnswerInTab(4, 'Current address', 'Town or City', 'Middlesbrough');
  I.seeAnswerInTab(5, 'Current address', 'Postcode/Zipcode', 'TS9 5DQ');
  I.seeAnswerInTab(6, 'Current address', 'Country', 'United Kingdom');
  I.seeAnswerInTab(6, 'Other person 1', 'Telephone number', '07888288288');
  I.seeAnswerInTab(7, 'Other person 1', 'What is this person’s relationship to the child or children in this case?', 'Tim Smith');
  I.seeAnswerInTab(8, 'Other person 1', 'Do you need contact details hidden from other parties?', 'Yes');
  I.seeAnswerInTab(9, 'Other person 1', 'Give reason', 'mock reason');
  I.seeAnswerInTab(10, 'Other person 1', 'Do you believe this person will have problems with litigation capacity (understanding what\'s happening in the case)?', 'Yes');
  I.seeAnswerInTab(11, 'Other person 1', 'Give details, including assessment outcomes and referrals to health services', 'mock reason');
});
