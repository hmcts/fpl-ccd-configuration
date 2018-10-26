const config = require('../config.js');

Feature('Enter grounds');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventSummary);
  caseViewPage.goToNewActions(config.applicationActions.enterGrounds);
});

Scenario('Filling in grounds for application section of c110a', (I, enterGroundsPage, caseViewPage) => {
  enterGroundsPage.enterThresholdCriteriaDetails();
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.enterGrounds);
  caseViewPage.selectTab(caseViewPage.tabs.legalOpinion);
  I.seeAnswerInTab(1, 'Grounds for application', 'How does this case meet the' +
    ' threshold' +
    ' criteria?', 'Not receiving care that would be reasonably expected from' +
    ' a parent');
});
