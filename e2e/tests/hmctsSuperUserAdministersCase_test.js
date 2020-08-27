const config = require('../config.js');

const orderCollection = require('../fixtures/testData/orderCollection.json');
const mandatoryWithMultipleChildren = require('../fixtures/mandatoryWithMultipleChildren.json');

Feature('Case administration by super user');

let caseId;

BeforeSuite(async I => caseId = await I.submitNewCaseWithData({...mandatoryWithMultipleChildren, ...orderCollection}));

Before(async I => await I.navigateToCaseDetailsAs(config.hmctsSuperUser, caseId));

Scenario('HMCTS super user removes an order from a case', async (I, caseViewPage, loginPage, removeOrderEventPage) => {
  await caseViewPage.goToNewActions(config.superUserActions.removeOrder);
  const labelToSelect = orderCollection[0].value.title + '-' + orderCollection[0].value.date;
  removeOrderEventPage.selectOrderToRemove(labelToSelect);
  await I.retryUntilExists(() => I.click('Continue'), removeOrderEventPage.fields.reason);
  removeOrderEventPage.addRemoveOrderReason('Entered incorrect order');
  await I.completeEvent('Save and continue');
  // await orderFunctions.assertOrder(I, caseViewPage, order, defaultIssuedDate);
});
