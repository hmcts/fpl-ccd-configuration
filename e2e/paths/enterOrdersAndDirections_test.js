const config = require('../config.js');

Feature('Enter order and details');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventSummary);
  caseViewPage.goToNewActions(config.applicationActions.selectOrders);
});

Scenario('Select the care order case order and continue', (I, caseViewPage, ordersNeededPage) => {
  ordersNeededPage.checkCareOrder();
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  caseViewPage.selectTab(caseViewPage.tabs.ordersHearing);
  I.seeAnswerInTab('Orders & Directions', 'Which orders and directions do you want to apply for?', 'Care order');
});

Scenario('Select all case orders and fill in directions & interim information', (I, caseViewPage, ordersNeededPage) => {
  ordersNeededPage.checkCareOrder();
  ordersNeededPage.checkSupervisionOrder();
  ordersNeededPage.checkEducationSupervisionOrder();
  ordersNeededPage.checkEmergencyProtectionOrder();
  ordersNeededPage.checkOtherOrder();
  ordersNeededPage.enterDirectionAndInterim();
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  caseViewPage.selectTab(caseViewPage.tabs.ordersHearing);
  I.seeAnswerInTab('Orders & Directions', 'Which orders and directions do you want to apply for?', ['Care order', 'Supervision order', 'Education supervision order', 'Other order under part 4 of the Children Act 1989']);
  I.seeAnswerInTab('Orders & Directions', 'Directions and interim orders, if needed', 'Test direction and interim');
});
