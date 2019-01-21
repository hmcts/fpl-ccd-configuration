const config = require('../config.js');

Feature('Enter grounds').retry(2);

Before((I) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
});

Scenario('Filling in grounds for application section of c110a', (I, enterGroundsPage, caseViewPage) => {
  caseViewPage.goToNewActions(config.applicationActions.enterGrounds);
  enterGroundsPage.enterThresholdCriteriaDetails();
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterGrounds);
  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeAnswerInTab(1, 'How does this case meet the threshold criteria?', '',
    'Not receiving care that would be reasonably expected from a parent');
});

Scenario('Filling in grounds for application after selecting EPO',
  (I, enterGroundsPage, caseViewPage, ordersNeededPage) => {
    caseViewPage.goToNewActions(config.applicationActions.selectOrders);
    ordersNeededPage.checkEmergencyProtectionOrder();
    I.continueAndSave();
    I.seeEventSubmissionConfirmation(config.applicationActions.selectOrders);
    caseViewPage.goToNewActions(config.applicationActions.enterGrounds);
    enterGroundsPage.enterGroundsForEmergencyProtectionOrder();
    I.continueAndSave();
    I.seeEventSubmissionConfirmation(config.applicationActions.enterGrounds);
    caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
    I.seeAnswerInTab(
      1, 'How are there grounds for an emergency protection order?', '', [
        enterGroundsPage.fields.groundsForApplication.harmIfNotMoved,
        enterGroundsPage.fields.groundsForApplication.harmIfMoved,
        enterGroundsPage.fields.groundsForApplication.urgentAccessRequired,
      ]);
  });
