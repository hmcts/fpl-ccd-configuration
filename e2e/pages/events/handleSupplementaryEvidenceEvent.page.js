const { I } = inject();

module.exports = {

  fields: {
    evidenceHandled: '#evidenceHandled-Yes',
  },

  async handleSupplementaryEvidence() {
    await I.runAccessibilityTest();
    console.log('handle supplementary evidence');
    I.click(this.fields.evidenceHandled);
  },
};
