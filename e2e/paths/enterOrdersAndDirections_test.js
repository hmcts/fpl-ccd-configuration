const config = require('../config.js');

Feature('Enter order and details');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventSummary);
  caseViewPage.goToNewActions(config.applicationActions.selectOrders);
});

Scenario('Select the care order case order and continue', (I, ordersNeededPage) => {
  ordersNeededPage.checkCareOrder();
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.selectOrders);
});

Scenario('Select all case orders and fill in directions & interim information', (I, ordersNeededPage) => {
  ordersNeededPage.checkAllOrdersAndDirections();
  ordersNeededPage.fillTextArea();
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.selectOrders);
});
