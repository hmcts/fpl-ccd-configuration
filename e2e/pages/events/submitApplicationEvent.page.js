const { I } = inject();
const dateFormat = require('dateformat');
const money = require('../../helpers/money_helper');

module.exports = {

  fields: {
    consentCheckbox: '#submissionConsent-agree',
  },

  giveConsent() {
    I.checkOption(this.fields.consentCheckbox);
    I.runAccessibilityTest();
  },

  seeDraftApplicationFile() {
    I.see(('draft_c110a_application_'.concat(dateFormat(new Date(), 'ddmmm') + '.pdf').toLowerCase()));
  },

  async getFeeToPay(){
    return money.parse(await I.grabTextFrom('ccd-read-money-gbp-field'));
  },
};
