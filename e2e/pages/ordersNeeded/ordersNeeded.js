/* global locate */

const I = actor();

module.exports = {
  fields: {
    orderType: {
      careOrder: locate('input').withAttr({id: 'orders_option-Care order'}),
      supervisionOrder: locate('input').withAttr({id: 'orders_option-Supervision order'}),
      educationSupervisionOrder: locate('input').withAttr({id: 'orders_option-Education supervision order'}),
      emergencyProtectionOrder: locate('input').withAttr({id: 'orders_option-Emergency protection order'}),
      other: locate('input').withAttr({id: 'orders_option-Other'}),
    },
    directionAndInterim: '#orders_directions',
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
