const I = actor();

module.exports = {
  fields: {
    ordersAndDirections: {
      careOrder: 'Care order',
      supervisionOrder:'Supervision order',
      educationSupervisionOrder: 'Education supervision order',
      emergencyProtectionOrder: 'Emergency protection order',
      other: 'Other',
    },
    directionAndInterim: '#orders_directionsAndInterim',
  },

  fillTextArea(testString = 'Test string') {
    I.fillField(this.fields.directionAndInterim, testString);
  },
  
  checkCareOrder() {
    I.checkOption(this.fields.ordersAndDirections.careOrder);
  },

  checkAllOrdersAndDirections() {
    this.checkCareOrder();
    I.checkOption(this.fields.ordersAndDirections.supervisionOrder);
    I.checkOption(this.fields.ordersAndDirections.educationSupervisionOrder);
    I.checkOption(this.fields.ordersAndDirections.emergencyProtectionOrder);
    I.checkOption(this.fields.ordersAndDirections.other);
  },
};
