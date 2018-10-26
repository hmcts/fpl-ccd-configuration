const config = require('../config.js');
const other = require('../fixtures/others.js');

Feature('Enter others who should be given notice').retry(2);

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
  caseViewPage.goToNewActions(config.applicationActions.enterOthers);
});

Scenario('Enter other\'s details in c110a application', (I, enterOthersPage) => {
  enterOthersPage.enterOtherDetails(other);
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.enterOthers);
});

Scenario('Complete entering others details in the c110a application', (I, enterOthersPage) => {
  enterOthersPage.enterOtherDetails(other);
  enterOthersPage.enterRelationshipToChild('Tim Smith');
  enterOthersPage.enterContactDetailsHidden('Yes');
  enterOthersPage.enterLitigationIssues('No');
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
});
