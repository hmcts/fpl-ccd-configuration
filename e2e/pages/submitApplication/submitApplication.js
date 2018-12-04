const I = actor();

module.exports = {

  fields: {
    consentCheckbox: '#submissionConsent_Consent-agree', 
  },
  
  giveConsentAndContinue() {
    I.checkOption(this.fields.consentCheckbox);
    I.click('Continue');
  },
};
