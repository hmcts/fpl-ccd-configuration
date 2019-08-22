const config = require('../config.js');

Feature('Enter grounds');

Before((I) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
});

Scenario('Filling in grounds for application section of c110a', (I, enterGroundsForApplicationEventPage, caseViewPage) => {
  caseViewPage.goToNewActions(config.applicationActions.enterGrounds);
  enterGroundsForApplicationEventPage.enterThresholdCriteriaDetails();
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterGrounds);
  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeAnswerInTab(1, 'How does this case meet the threshold criteria?', '',
    'Not receiving care that would be reasonably expected from a parent');
});

Scenario('Filling in grounds for application after selecting EPO',
  (I, enterGroundsForApplicationEventPage, caseViewPage, enterOrdersAndDirectionsNeededEventPage) => {
    caseViewPage.goToNewActions(config.applicationActions.selectOrders);
    enterOrdersAndDirectionsNeededEventPage.checkEmergencyProtectionOrder();
    I.continueAndSave();
    I.seeEventSubmissionConfirmation(config.applicationActions.selectOrders);
    caseViewPage.goToNewActions(config.applicationActions.enterGrounds);
    enterGroundsForApplicationEventPage.enterGroundsForEmergencyProtectionOrder();
    I.continueAndSave();
    I.seeEventSubmissionConfirmation(config.applicationActions.enterGrounds);
    caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
    I.seeAnswerInTab(
      1, 'How are there grounds for an emergency protection order?', '', [
        enterGroundsForApplicationEventPage.fields.groundsForApplication.harmIfNotMoved,
        enterGroundsForApplicationEventPage.fields.groundsForApplication.harmIfMoved,
        enterGroundsForApplicationEventPage.fields.groundsForApplication.urgentAccessRequired,
      ]);
  });
