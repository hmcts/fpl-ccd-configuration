const I = actor();

module.exports = {

  fields: {
    consentCheckbox: '#submissionConsent-agree',
  },

  giveConsentAndProgress() {
    I.checkOption(this.fields.consentCheckbox);
    I.click('Continue');
    I.click('Submit');
  },
};
