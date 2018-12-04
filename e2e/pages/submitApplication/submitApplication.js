const I = actor();

module.exports = {

  fields: {
    consentCheckbox: '#submissionConsent_Consent-agree', 
  },
  
  giveConsent() {
    I.checkOption(this.fields.consentCheckbox);
  },
};
