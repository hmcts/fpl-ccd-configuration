const config = require('../config.js');

const orderCaseData = require('../fixtures/testData/caseDataWithOrderCollection.json');
const orderFunctions = require('../helpers/generated_order_helper');
const blankOrder = require('../fixtures/orders/blankOrder.js');

Feature('Case administration by super user');

let caseId;

BeforeSuite(async I => caseId = await I.submitNewCaseWithData(orderCaseData));

Before(async I => await I.navigateToCaseDetailsAs(config.hmctsSuperUser, caseId));

Scenario('HMCTS super user updates FamilyMan reference number', async (I, caseViewPage, loginPage, enterFamilyManCaseNumberEventPage) => {
  I.seeFamilyManNumber('mockCaseID');
  await caseViewPage.goToNewActions(config.administrationActions.addFamilyManCaseNumber);
  enterFamilyManCaseNumberEventPage.enterCaseID('newMockCaseID');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.addFamilyManCaseNumber);
  I.seeFamilyManNumber('newMockCaseID');
});

Scenario('HMCTS super user removes an order from a case', async (I, caseViewPage, loginPage, removeOrderEventPage) => {
  await caseViewPage.goToNewActions(config.superUserActions.removeOrder);
  let order = orderCaseData.caseData.orderCollection[0];
  const labelToSelect = order.value.title + ' - ' + order.value.dateOfIssue;
  removeOrderEventPage.selectOrderToRemove(labelToSelect);
  await I.retryUntilExists(() => I.click('Continue'), removeOrderEventPage.fields.reason);
  removeOrderEventPage.addRemoveOrderReason('Entered incorrect order');
  await I.completeEvent('Submit');
  I.seeEventSubmissionConfirmation(config.superUserActions.removeOrder);
  const issuedDate = new Date(2020, 4, 26, 14, 33);
  order = {
    ...blankOrder,
    ...{
      document: 'Blank order (C21).pdf',
    },
  };
  await orderFunctions.assertOrder(I, caseViewPage, order, issuedDate, false, true);
});
