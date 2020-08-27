const {I} = inject();

module.exports = {
  fields: {
    reason: '#removeOrderReason',
  },



  addRemoveOrderReason(reason) {
    I.fillField(this.fields.reason, reason);
  },
};
