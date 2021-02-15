const { I } = inject();

module.exports = {

  fields: {
    evidenceHandled: '#evidenceHandled-Yes',
  },

  async handleSupplementaryEvidence() {
    await I.runAccessibilityTest();
    I.click(this.fields.evidenceHandled);
  },
};
