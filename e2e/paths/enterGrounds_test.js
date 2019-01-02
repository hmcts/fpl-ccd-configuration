const config = require('../config.js');

Feature('Enter grounds').retry(2);

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseViewPage.goToNewActions(config.applicationActions.enterGrounds);
});

Scenario('Filling in grounds for application section of c110a', (I, enterGroundsPage, caseViewPage) => {
  enterGroundsPage.enterThresholdCriteriaDetails();
  I.continueAndSubmit();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterGrounds);
  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeAnswerInTab(1, 'Grounds for the application', 'How does this case meet the threshold criteria?',
    'Not receiving care that would be reasonably expected from a parent');
});
