const I = actor();

module.exports = {
  fields: {
    orderType: {
      careOrder: 'Care order',
      supervisionOrder:'Supervision order',
      educationSupervisionOrder: 'Education supervision order',
      emergencyProtectionOrder: 'Emergency protection order',
      other: 'Other',
    },
    directionAndInterim: '#orders_directionsAndInterim',
  },
  
  checkCareOrder() {
    I.checkOption(this.fields.orderType.careOrder);
  },

  checkSupervisionOrder() {
    I.checkOption(this.fields.orderType.supervisionOrder);    
  },

  checkEducationSupervisionOrder() {
    I.checkOption(this.fields.orderType.supervisionOrder);    
  },

  checkEmergencyProtectionOrder() {
    I.checkOption(this.fields.orderType.emergencyProtectionOrder);    
  },

  checkOtherOrder() {
    I.checkOption(this.fields.orderType.other);    
  },

  enterDirectionAndInterim(testString = 'Test string') {
    I.fillField(this.fields.directionAndInterim, testString);
  },
};
