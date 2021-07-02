const {I} = inject();

module.exports = {
  fields: {
    selection: {
      order: '#removeOrderOrApplication-ORDER',
      application: '#removeOrderOrApplication-APPLICATION',
    },
    orderReason: '#reasonToRemoveOrder',
    applicationReason: '#reasonToRemoveApplication',
    applicationReasonDetails: '#applicationRemovalDetails',
    orderList: '#removableOrderList',
    applicationList: '#removableApplicationList',
  },

  async selectOrderToRemove(option) {
    I.click(this.fields.selection.order);
    I.waitForElement(this.fields.orderList);
    await I.runAccessibilityTest();
    I.selectOption(this.fields.orderList, option);
  },

  async selectApplicationToRemove(option) {
    I.click(this.fields.selection.application);
    I.waitForElement(this.fields.applicationList);
    await I.runAccessibilityTest();
    I.selectOption(this.fields.applicationList, option);
  },

  addRemoveOrderReason(reason) {
    I.fillField(this.fields.orderReason, reason);
  },

  addRemoveApplicationReason(reason) {
    I.selectOption(this.fields.applicationReason, 'Other (with details)');
    I.fillField(this.fields.applicationReasonDetails, reason);
  },
};
