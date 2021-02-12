const {I} = inject();

module.exports = {
  fields: {
    reason: '#reasonToRemoveOrder',
    list: '#removableOrderList',
  },

  selectOrderToRemove(option) {
    I.waitForElement(this.fields.list);
    I.selectOption(this.fields.list, option);
    // I.runAccessibilityTest();
  },

  addRemoveOrderReason(reason) {
    I.fillField(this.fields.reason, reason);
  },
};
