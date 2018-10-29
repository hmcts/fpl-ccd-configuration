const config = require('../config.js');
const respondent = require('../fixtures/respondent.js');

Feature('Enter respondents').retry(2);

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
  caseViewPage.goToNewActions(config.applicationActions.enterRespondents);
});

Scenario('Entering information for respondent and submitting', (I, enterRespondentsPage) => {
  enterRespondentsPage.enterRespondent('firstRespondent', respondent);
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.enterRespondents);
});

Scenario('Entering all information for first respondent and an additional respondent', (I, enterRespondentsPage) => {
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
});
