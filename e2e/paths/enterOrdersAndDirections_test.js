const config = require('../config.js');

Feature('Enter order and details');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventSummary);
  caseViewPage.goToNewActions(config.applicationActions.selectOrders);
});

Scenario('Add directions and interim orders with no orders and directions selected', 
(I, ordersNeededPage) => {
  ordersNeededPage.SelectCareOrderOnly();
  I.continueAndSubmit(config.eventSummary, config.eventDescription);
  I.seeEventSubmissionConfirmation(config.applicationActions.selectOrders);
});

Scenario('Apply for all case orders and add directions and interim orders', 
  (I, ordersNeededPage) => {
    ordersNeededPage.SelectAllOrdersAndDirections();
    ordersNeededPage.fillTextArea();
    I.continueAndSubmit(config.eventSummary, config.eventDescription);
    I.seeEventSubmissionConfirmation(config.applicationActions.selectOrders);
});
