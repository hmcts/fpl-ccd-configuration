const { I } = inject();

module.exports = {

  fields: {
    evidenceHandled: '#evidenceHandled-Yes',
  },

  handleSupplementaryEvidence() {
    I.click(this.fields.evidenceHandled);
  },
};
