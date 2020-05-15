const { I } = inject();
const dateFormat = require('dateformat');

module.exports = {

  fields: {
    consentCheckbox: '#submissionConsent-agree',
  },

  giveConsent() {
    I.checkOption(this.fields.consentCheckbox);
  },

  seeDraftApplicationFile() {
    I.see(('draft_c110a_application_'.concat(dateFormat(new Date(), 'ddmmm') + '.pdf').toLowerCase()));
  },
};
