const { I } = inject();

module.exports = {

  fields: {
    deleteCheckbox: '#deletionConsent-Delete',
  },

  tickDeletionConsent() {
    I.checkOption(this.fields.deleteCheckbox);
    //I.runAccessibilityTest();
  },
};
