const I = actor();

module.exports = {

  fields: {
    consentCheckbox: '#delete-Delete',
  },

  delete() {
    I.checkOption(this.fields.consentCheckbox);
  },
};
