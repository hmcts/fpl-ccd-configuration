const config = require('../config.js');
const respondents = require('../fixtures/respondents.js');

Feature('Enter respondents');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseViewPage.goToNewActions(config.applicationActions.enterRespondents);
});

Scenario('Entering information for respondent and submitting', async (I, enterRespondentsPage, caseViewPage) => {
  await enterRespondentsPage.enterRespondent(respondents[0]);
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterRespondents);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
});

Scenario('Entering all information for multiple respondents', async (I, enterRespondentsPage, caseViewPage) => {
  await enterRespondentsPage.enterRespondent(respondents[0]);
  await enterRespondentsPage.enterRelationshipToChild('mock reason');
  await enterRespondentsPage.enterContactDetailsHidden('Yes', 'mock reason');
  await enterRespondentsPage.enterLitigationIssues('Yes', 'mock reason');
  enterRespondentsPage.addRespondent();
  await enterRespondentsPage.enterRespondent(respondents[1]);
  await enterRespondentsPage.enterRelationshipToChild('mock reason');
  await enterRespondentsPage.enterContactDetailsHidden('Yes', 'mock reason');
  await enterRespondentsPage.enterLitigationIssues('No');
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterRespondents);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeAnswerInTab(3, 'Party', 'First name', respondents[0].firstName);
  I.seeAnswerInTab(4, 'Party', 'Last name', respondents[0].lastName);
  I.seeAnswerInTab(5, 'Party', 'Date of birth', '1 Jan 1980');
  I.seeAnswerInTab(6, 'Party', 'Gender', respondents[0].gender);
  I.seeAnswerInTab(7, 'Party', 'Place of birth', respondents[0].placeOfBirth);
  I.seeAnswerInTab(1, 'Current address', 'Building and Street', respondents[0].address.buildingAndStreet.lineOne);
  I.seeAnswerInTab(2, 'Current address', '', respondents[0].address.buildingAndStreet.lineTwo);
  I.seeAnswerInTab(3, 'Current address', '', respondents[0].address.buildingAndStreet.lineThree);
  I.seeAnswerInTab(4, 'Current address', 'Town or City', respondents[0].address.town);
  I.seeAnswerInTab(5, 'Current address', 'Postcode/Zipcode', respondents[0].address.postcode);
  I.seeAnswerInTab(6, 'Current address', 'Country', respondents[0].address.country);
  I.seeAnswerInTab(1, 'Telephone', 'Telephone', respondents[0].telephone);
  I.seeAnswerInTab(10, 'Party', 'What is the respondent’s relationship to the child or children in this case?', 'mock reason');
  I.seeAnswerInTab(11, 'Party', 'Do you need contact details hidden from anyone?', 'Yes');
  I.seeAnswerInTab(12, 'Party', 'Give reason', 'mock reason');
  I.seeAnswerInTab(13, 'Party', 'Do you believe this person will have problems with litigation capacity (understanding what\'s happening in the case)?', 'Yes');
  I.seeAnswerInTab(14, 'Party', 'Give details, including assessment outcomes and referrals to health services', 'mock reason');

  I.seeAnswerInTab(3, 'Party', 'First name', respondents[1].firstName);
  I.seeAnswerInTab(4, 'Party', 'Last name', respondents[1].lastName);
  I.seeAnswerInTab(5, 'Party', 'Date of birth', '1 Jan 1955');
  I.seeAnswerInTab(6, 'Party', 'Gender', respondents[1].gender);
  I.seeAnswerInTab(7, 'Party', 'Place of birth', respondents[1].placeOfBirth);
  I.seeAnswerInTab(1, 'Current address', 'Building and Street', respondents[1].address.buildingAndStreet.lineOne);
  I.seeAnswerInTab(2, 'Current address', '', respondents[1].address.buildingAndStreet.lineTwo);
  I.seeAnswerInTab(3, 'Current address', '', respondents[1].address.buildingAndStreet.lineThree);
  I.seeAnswerInTab(4, 'Current address', 'Town or City', respondents[1].address.town);
  I.seeAnswerInTab(5, 'Current address', 'Postcode/Zipcode', respondents[1].address.postcode);
  I.seeAnswerInTab(6, 'Current address', 'Country', respondents[1].address.country);
  I.seeAnswerInTab(1, 'Telephone', 'Telephone', respondents[1].telephone);
  I.seeAnswerInTab(10, 'Party', 'What is the respondent’s relationship to the child or children in this case?', 'mock reason');
  I.seeAnswerInTab(11, 'Party', 'Do you need contact details hidden from anyone?', 'Yes');
  I.seeAnswerInTab(12, 'Party', 'Give reason', 'mock reason');
  I.seeAnswerInTab(13, 'Party', 'Do you believe this person will have problems with litigation capacity (understanding what\'s happening in the case)?', 'No');
});
