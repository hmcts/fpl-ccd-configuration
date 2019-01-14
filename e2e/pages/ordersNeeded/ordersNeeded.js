/* global locate */

const I = actor();

module.exports = {
  fields: {
    orderType: {
      careOrder: locate('input').withAttr({id: 'orders_orderType-CARE_ORDER'}),
      interimCareOrder: locate('input').withAttr({id: 'orders_orderType-INTERIM_CARE_ORDER'}),
      supervisionOrder: locate('input').withAttr({id: 'orders_orderType-SUPERVISION_ORDER'}),
      interimSupervisionOrder: locate('input').withAttr({id: 'orders_orderType-INTERIM_SUPERVISION_ORDER'}),
      educationSupervisionOrder: locate('input').withAttr({id: 'orders_orderType-EDUCATION_SUPERVISION_ORDER'}),
      emergencyProtectionOrder: locate('input').withAttr({id: 'orders_orderType-EMERGENCY_PROTECTION_ORDER'}),
      other: locate('input').withAttr({id: 'orders_orderType-OTHER'}),
      otherDetails: '#orders_otherOrder',
    },
    protectionOrders: {
      whereabouts: locate('input').withAttr({id: 'orders_emergencyProtectionOrders-CHILD_WHEREABOUTS'}),
      entry: locate('input').withAttr({id: 'orders_emergencyProtectionOrders-ENTRY_PREMISES'}),
      search: locate('input').withAttr({id: 'orders_emergencyProtectionOrders-SEARCH_FOR_CHILD'}),
      other: locate('input').withAttr({id: 'orders_emergencyProtectionOrders-OTHER'}),
      orderDetails: '#orders_emergencyProtectionOrderDetails',
    },
    protectionDirections: {
      contact: locate('input').withAttr({id: 'orders_emergencyProtectionOrderDirections-CONTACT_WITH_NAMED_PERSON'}),
      assessment: locate('input').withAttr({id: 'orders_emergencyProtectionOrderDirections-CHILD_MEDICAL_ASSESSMENT'}),
      medicalPractitioner: locate('input').withAttr({id: 'orders_emergencyProtectionOrderDirections-MEDICAL_PRACTITIONER'}),
      exclusion: locate('input').withAttr({id: 'orders_emergencyProtectionOrderDirections-EXCLUSION_REQUIREMENT'}),
      other: locate('input').withAttr({id: 'orders_emergencyProtectionOrderDirections-OTHER'}),
      directionsDetails: '#orders_emergencyProtectionOrderDirectionDetails',
    },

    directions: '#orders_directions-Yes',
    directionsDetails: '#orders_directionDetails',
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

  enterDirections(details) {
    I.fillField(this.fields.directionsDetails, details);
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

  enterProtectionOrdersDetails(details) {
    I.fillField(this.fields.protectionOrders.orderDetails, details);
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

  enterProtectionDirectionsDetails(details) {
    I.fillField(this.fields.protectionDirections.directionsDetails, details);
  },

  enterOrderDetails(details) {
    I.fillField(this.fields.orderType.otherDetails, details);
  },

  checkDirections() {
    I.click(this.fields.directions);
  },

};
