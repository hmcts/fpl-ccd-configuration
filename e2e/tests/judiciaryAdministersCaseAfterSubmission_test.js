const config = require('../config.js');
const orders = require('../fixtures/orders.js');
const orderFunctions = require('../helpers/generated_order_helper');
const mandatoryWithMultipleChildren = require('../fixtures/mandatoryWithMultipleChildren.json');

let caseId;

Feature('Judiciary case administration after submission');

BeforeSuite(async (I, caseViewPage, submitApplicationEventPage, enterFamilyManCaseNumberEventPage) => {
  caseId = await I.submitNewCaseWithData(mandatoryWithMultipleChildren);

  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);

  await caseViewPage.goToNewActions(config.administrationActions.addFamilyManCaseNumber);
  enterFamilyManCaseNumberEventPage.enterCaseID();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.addFamilyManCaseNumber);

  await I.navigateToCaseDetailsAs(config.judicaryUser, caseId);
});

Before(async I => await I.navigateToCaseDetails(caseId));


Scenario('Judiciary creates multiple orders for the case', async (I, caseViewPage, addHearingBookingDetailsEventPage, createOrderEventPage) => {
  for (let i = 0; i < orders.length; i++) {
    const defaultIssuedDate = new Date();
    await caseViewPage.goToNewActions(config.administrationActions.createOrder);
    await orderFunctions.createOrder(I, createOrderEventPage, orders[i], true);
    I.seeEventSubmissionConfirmation(config.administrationActions.createOrder);
    await orderFunctions.assertOrder(I, caseViewPage, orders[i], i + 1, defaultIssuedDate, true);
  }
});
