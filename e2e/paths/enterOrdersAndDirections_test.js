const config = require('../config.js');

Feature('Enter order and details').retry(2);

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventSummary);
  caseViewPage.goToNewActions(config.applicationActions.selectOrders);
});

Scenario('Select the care order case order and continue', (I, caseViewPage, ordersNeededPage) => {
  ordersNeededPage.checkCareOrder();
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.selectOrders);
  caseViewPage.selectTab(caseViewPage.tabs.ordersHearing);
  I.seeAnswerInTab(1, 'Orders and Directions', 'Which orders and directions do you want to apply for?', 'Care order');
});

Scenario('Select all case orders and fill in directions & interim information', (I, caseViewPage, ordersNeededPage) => {
  ordersNeededPage.checkCareOrder();
  ordersNeededPage.checkSupervisionOrder();
  ordersNeededPage.checkEducationSupervisionOrder();
  ordersNeededPage.checkEmergencyProtectionOrder();
  ordersNeededPage.checkOtherOrder();
  ordersNeededPage.enterDirectionAndInterim();
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.selectOrders);
  caseViewPage.selectTab(caseViewPage.tabs.ordersHearing);
  I.seeAnswerInTab(1, 'Orders and Directions', 'Which orders and directions do you want to apply for?',
    ['Care order', 'Supervision order', 'Education supervision order', 'Emergency protection order',
      'Other order under part 4 of the Children Act 1989']);
  I.seeAnswerInTab(2, 'Orders and Directions', 'Directions and interim orders, if needed', 'Test direction and interim');
});
