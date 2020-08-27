const config = require('../config.js');

const orderCollection = require('../fixtures/testData/orderCollection.json');
const mandatoryWithMultipleChildren = require('../fixtures/mandatoryWithMultipleChildren.json');

Feature('Case administration by super user');

let caseId;
let submittedAt;

console.log('test------------------------------------->');
BeforeSuite(async (I) => {
  caseId = await I.submitNewCaseWithData({...mandatoryWithMultipleChildren, ...orderCollection});

  submittedAt = new Date();
  await I.navigateToCaseDetailsAs(config.hmctsSuperUser, caseId);

});

Scenario('HMCTS super user removes an order from a case', async (I, caseViewPage, loginPage, removeAnOrderPage) => {
  await caseViewPage.goToNewActions(config.superUserActions.removeOrder);
  I.waitForElement('#removableOrderList');
  removeAnOrderPage.addRemoveOrderReason('Entered incorrect order');
  await I.completeEvent('Save and continue');
  await orderFunctions.assertOrder(I, caseViewPage, order, defaultIssuedDate);
});
