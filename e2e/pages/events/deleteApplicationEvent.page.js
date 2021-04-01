const { I } = inject();

module.exports = {

  fields: {
    deleteCheckbox: '#deletionConsent-Delete',
  },

  async tickDeletionConsent() {
    I.checkOption(this.fields.deleteCheckbox);
    await I.runAccessibilityTest();
  },
};
