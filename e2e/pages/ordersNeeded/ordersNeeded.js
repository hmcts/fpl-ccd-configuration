/* global locate */

const I = actor();

module.exports = {
  fields: {
    orderType: {
      careOrder: locate('input').withAttr({id: 'orders_orderType-Care order'}),
      supervisionOrder: locate('input').withAttr({id: 'orders_orderType-Supervision order'}),
      educationSupervisionOrder: locate('input').withAttr({id: 'orders_orderType-Education supervision order'}),
      emergencyProtectionOrder: locate('input').withAttr({id: 'orders_orderType-Emergency protection order'}),
      other: locate('input').withAttr({id: 'orders_orderType-Other'}),
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
    I.checkOption(this.fields.orderType.educationSupervisionOrder);
  },

  checkEmergencyProtectionOrder() {
    I.checkOption(this.fields.orderType.emergencyProtectionOrder);
  },

  checkOtherOrder() {
    I.checkOption(this.fields.orderType.other);
  },

  enterDirectionAndInterim(testString = 'Test direction and interim') {
    I.fillField(this.fields.directionAndInterim, testString);
  },
};
