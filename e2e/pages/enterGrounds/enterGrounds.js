const I = actor();

module.exports = {

  fields: {
    thresholdReason: {
      notReceivingCare: 'Not receiving care that would be reasonably expected from a parent',
      beyondControl: 'Beyond parental control',
    },
    thresholdDetails: '#grounds_thresholdDetails',
    groundsForApplication: {
      harmIfNotMoved: 'There\'s reasonable cause to believe the child is likely to suffer significant harm if they\'re not moved to accommodation provided by you, or on your behalf',
      harmIfMoved: 'There\'s reasonable cause to believe the child is likely to suffer significant harm if they don\'t stay in their current accommodation',
      urgentAccessRequired: 'You\'re making enquiries and need urgent access to the child to find out about their welfare, and access is being unreasonably refused',
    },
  },

  enterThresholdCriteriaDetails() {
    I.checkOption(this.fields.thresholdReason.notReceivingCare);
    I.fillField(this.fields.thresholdDetails, 'mock threshold details');
  },

  enterGroundsForEmergencyProtectionOrder() {
    I.checkOption(this.fields.groundsForApplication.harmIfNotMoved);
    I.checkOption(this.fields.groundsForApplication.harmIfMoved);
    I.checkOption(this.fields.groundsForApplication.urgentAccessRequired);
  },
};
