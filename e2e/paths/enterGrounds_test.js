/* global xScenario */
const config = require('../config.js');

Feature('Enter grounds');

Before((I) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
});

Scenario('Filling in grounds for application section of c110a', (I, enterGroundsPage, caseViewPage) => {
  caseViewPage.goToNewActions(config.applicationActions.enterGrounds);
  enterGroundsPage.enterThresholdCriteriaDetails();
  I.continueAndSubmit();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterGrounds);
  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeAnswerInTab(1, 'How does this case meet the threshold criteria?', '',
    'Not receiving care that would be reasonably expected from a parent');
});

xScenario('Filling in grounds for application after selecting EPO', (I, enterGroundsPage, caseViewPage, ordersNeededPage) => {
  caseViewPage.goToNewActions(config.applicationActions.selectOrders);
  ordersNeededPage.checkEmergencyProtectionOrder();
  I.continueAndSubmit();
  I.seeEventSubmissionConfirmation(config.applicationActions.selectOrders);
  caseViewPage.goToNewActions(config.applicationActions.enterGrounds);
});
