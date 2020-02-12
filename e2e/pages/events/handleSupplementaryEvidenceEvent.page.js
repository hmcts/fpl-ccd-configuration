const { I } = inject();

module.exports = {

  fields: {
    supplementaryEvidenceHandled: '#supplementaryEvidenceHandled-Yes',
  },

  handleSupplementaryEvidence() {
    I.click(this.fields.supplementaryEvidenceHandled);
  },
};
