/* global locate */

const I = actor();

module.exports = {
  fields: {
    orderType: {
      careOrder: locate('input').withAttr({id: 'orders_orderType-Care order'}),
      interimCareOrder: locate('input').withAttr({id: 'orders_orderType-Interim care order'}),
      supervisionOrder: locate('input').withAttr({id: 'orders_orderType-Supervision order'}),
      interimSupervisionOrder: locate('input').withAttr({id: 'orders_orderType-Interim supervision order'}),
      educationSupervisionOrder: locate('input').withAttr({id: 'orders_orderType-Education supervision order'}),
      emergencyProtectionOrder: locate('input').withAttr({id: 'orders_orderType-Emergency protection order'}),
      other: locate('input').withAttr({id: 'orders_orderType-Other'}),
      otherDetails: '#orders_otherOrder',
    },
    protectionOrders: {
      whereabouts: locate('input').withAttr({id: 'orders_emergencyProtectionOrders-Child whereabouts'}),
      entry: locate('input').withAttr({id: 'orders_emergencyProtectionOrders-Entry premises'}),
      search: locate('input').withAttr({id: 'orders_emergencyProtectionOrders-Search for another child'}),
      other: locate('input').withAttr({id: 'orders_emergencyProtectionOrders-Other'}),
      orderDetails: '#orders_emergencyProtectionOrderDetails',
    },
    protectionDirections: {
      contact: locate('input').withAttr({id: 'orders_emergencyProtectionDirections-Contact with any named person'}),
      assessment: locate('input').withAttr({id: 'orders_emergencyProtectionDirections-Child medical assessment'}),
      medicalPractitioner: locate('input').withAttr({id: 'orders_emergencyProtectionDirections-Medical practitioner'}),
      exclusion: locate('input').withAttr({id: 'orders_emergencyProtectionDirections-An exclusion requirement'}),
      other: locate('input').withAttr({id: 'orders_emergencyProtectionDirections-Other'}),
      directionsDetails: '#orders_emergencyProtectionDirectionsDetails',
    },

    directions: '#orders_directions-Yes',
    directionsDetails: '#orders_directionsDetails',
  },

  checkCareOrder() {
    I.checkOption(this.fields.orderType.careOrder);
  },

  checkSupervisionOrder() {
    I.checkOption(this.fields.orderType.supervisionOrder);
  },

  checkEducationSupervisionOrder() {
    I.checkOption(this.fields.orderType.educationSupervisionOrder);
  },

  checkEmergencyProtectionOrder() {
    I.checkOption(this.fields.orderType.emergencyProtectionOrder);
  },

  checkOtherOrder() {
    I.checkOption(this.fields.orderType.other);
  },

  enterDirections(testString = 'Test') {
    I.fillField(this.fields.directionsDetails, testString);
  },

  checkInterimCareOrder() {
    I.checkOption(this.fields.orderType.interimCareOrder);
  },

  checkInterimSupervisionOrder() {
    I.checkOption(this.fields.orderType.interimSupervisionOrder);
  },

  checkWhereabouts() {
    I.checkOption(this.fields.protectionOrders.whereabouts);
  },

  checkEntry() {
    I.checkOption(this.fields.protectionOrders.entry);
  },

  checkSearch() {
    I.checkOption(this.fields.protectionOrders.search);
  },

  checkProtectionOrdersOther() {
    I.checkOption(this.fields.protectionOrders.other);
  },

  enterProtectionOrdersDetails(testString = 'Test') {
    I.fillField(this.fields.protectionOrders.orderDetails, testString);
  },

  checkContact() {
    I.checkOption(this.fields.protectionDirections.contact);
  },

  checkAssessment() {
    I.checkOption(this.fields.protectionDirections.assessment);
  },

  checkMedicalPractitioner() {
    I.checkOption(this.fields.protectionDirections.medicalPractitioner);
  },

  checkExclusion() {
    I.checkOption(this.fields.protectionDirections.exclusion);
  },

  checkProtectionDirectionsOther() {
    I.checkOption(this.fields.protectionDirections.other);
  },

  enterProtectionDirectionsDetails(testString = 'Test') {
    I.fillField(this.fields.protectionDirections.directionsDetails, testString);
  },

  enterOrderDetails(testString = 'Test') {
    I.fillField(this.fields.orderType.otherDetails, testString);
  },

  checkDirections() {
    I.click(this.fields.directions);
  },

};
