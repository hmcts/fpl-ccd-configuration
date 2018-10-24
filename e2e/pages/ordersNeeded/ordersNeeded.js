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
  
  checkCareOrder() {
    I.checkOption(this.fields.ordersAndDirections.careOrder);
  },

  checkSupervisionOrder() {
    I.checkOption(this.fields.ordersAndDirections.supervisionOrder);    
  },

  checkEducationSupervisionOrder() {
    I.checkOption(this.fields.ordersAndDirections.supervisionOrder);    
  },

  checkEmergencyProtectionOrder() {
    I.checkOption(this.fields.ordersAndDirections.emergencyProtectionOrder);    
  },

  checkOtherOrder() {
    I.checkOption(this.fields.ordersAndDirections.other);    
  },

  enterDirectionAndInterim(testString = 'Test string') {
    I.fillField(this.fields.directionAndInterim, testString);
  },
};
