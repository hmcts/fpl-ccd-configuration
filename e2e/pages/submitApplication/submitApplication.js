const I = actor();

module.exports = {

  fields: {
    consentCheckbox: '#submissionConsent-AGREE',
  },

  giveConsent() {
    I.checkOption(this.fields.consentCheckbox);
  },
};
