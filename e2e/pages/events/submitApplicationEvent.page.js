const { I } = inject();

module.exports = {

  fields: {
    consentCheckbox: '#submissionConsent-agree',
  },

  giveConsent() {
    I.checkOption(this.fields.consentCheckbox);
  },
};
