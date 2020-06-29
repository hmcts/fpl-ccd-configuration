const config = require('../config.js');
const blankOrder = require('../fixtures/orders/blankOrder.js');
const interimSuperVisionOrder = require('../fixtures/orders/interimSupervision.js');
const finalSuperVisionOrder = require('../fixtures/orders/finalSupervisionOrder.js');
const emergencyProtectionOrder = require('../fixtures/orders/emergencyProtectionOrder.js');
const interimCareOrder = require('../fixtures/orders/interimCareOrder.js');
const finalCareOrder = require('../fixtures/orders/finalCareOrder.js');
const dischargeOfCareOrder = require('../fixtures/orders/dischargeOfCareOrder.js');
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

Scenario('Judiciary creates blank order', async (I, caseViewPage, createOrderEventPage) => {
  await verifyOrderCreation(I, caseViewPage, createOrderEventPage, blankOrder);
});

Scenario('Judiciary creates interim supervision order', async (I, caseViewPage, createOrderEventPage) => {
  await verifyOrderCreation(I, caseViewPage, createOrderEventPage, interimSuperVisionOrder);
});

Scenario('Judiciary creates final supervision order', async (I, caseViewPage, createOrderEventPage) => {
  await verifyOrderCreation(I, caseViewPage, createOrderEventPage, finalSuperVisionOrder);
});

Scenario('Judiciary creates emergency protection order', async (I, caseViewPage, createOrderEventPage) => {
  await verifyOrderCreation(I, caseViewPage, createOrderEventPage, emergencyProtectionOrder);
});

Scenario('Judiciary creates interim care order', async (I, caseViewPage, createOrderEventPage) => {
  await verifyOrderCreation(I, caseViewPage, createOrderEventPage, interimCareOrder);
});

Scenario('Judiciary creates final care order', async (I, caseViewPage, createOrderEventPage) => {
  await verifyOrderCreation(I, caseViewPage, createOrderEventPage, finalCareOrder);
});

Scenario('Judiciary creates discharge of care order', async (I, caseViewPage, createOrderEventPage) => {
  await verifyOrderCreation(I, caseViewPage, createOrderEventPage, dischargeOfCareOrder);
});

const verifyOrderCreation = async function(I, caseViewPage, createOrderEventPage, order){
  await caseViewPage.goToNewActions(config.administrationActions.createOrder);
  const defaultIssuedDate = new Date();
  await orderFunctions.createOrder(I, createOrderEventPage, order);
  I.seeEventSubmissionConfirmation(config.administrationActions.createOrder);
  await orderFunctions.assertOrder(I, caseViewPage, order, defaultIssuedDate);
};
