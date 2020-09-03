const {I} = inject();

module.exports = {
  fields: {
    reason: '#reasonToRemoveOrder',
    list: '#removableOrderList',
  },

  selectOrderToRemove(option) {
    I.waitForElement(this.fields.list);
    I.selectOption(this.fields.list, option);
  },

  addRemoveOrderReason(reason) {
    I.fillField(this.fields.reason, reason);
  },
};
