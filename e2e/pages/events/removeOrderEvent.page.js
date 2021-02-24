const {I} = inject();

module.exports = {
  fields: {
    reason: '#reasonToRemoveOrder',
    list: '#removableOrderList',
  },

  async selectOrderToRemove(option) {
    I.waitForElement(this.fields.list);
    await I.runAccessibilityTest();
    console.log('remove order event');
    I.selectOption(this.fields.list, option);
  },

  addRemoveOrderReason(reason) {
    I.fillField(this.fields.reason, reason);
  },
};
