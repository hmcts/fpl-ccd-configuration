const I = actor();

module.exports = {

  fields: {
    deleteCheckbox: '#deletionConsent-Delete',
  },

  tickDeletionConsent() {
    I.checkOption(this.fields.deleteCheckbox);
  },
};
