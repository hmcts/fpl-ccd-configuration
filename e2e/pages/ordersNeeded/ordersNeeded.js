const I = actor();

module.exports = {
  fields: {
    ordersAndDirectionsCheckGroup: {
      careOrder: 'Care order',
      supervisionOrder:'Supervision order',
      educationSupervisionOrder: 'Education supervision order',
      emergencyProtectionOrder: 'Emergency protection order',
      other: 'Other',
    },
    directionIntermOrdersTextarea: '#orders_ordersAndDetailsReason',
  },

  fillTextArea(testString = 'Test string') {
    I.fillField(this.fields.directionIntermOrdersTextarea, testString);
  },
  
  SelectCareOrderOnly() {
    I.checkOption(this.fields.ordersAndDirectionsCheckGroup.careOrder);
  },

  SelectAllOrdersAndDirections() {
    this.SelectCareOrderOnly();
    I.checkOption(this.fields.ordersAndDirectionsCheckGroup.supervisionOrder);
    I.checkOption(this.fields.ordersAndDirectionsCheckGroup.educationSupervisionOrder);
    I.checkOption(this.fields.ordersAndDirectionsCheckGroup.emergencyProtectionOrder);
    I.checkOption(this.fields.ordersAndDirectionsCheckGroup.other);
  },
};
