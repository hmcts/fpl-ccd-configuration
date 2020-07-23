const { I } = inject();

module.exports = {
  fields: {
    reviewCmoRadioGroup: {
      yes: 'Yes, seal and send to all parties',
      no: 'No, the local authority needs to make changes',
    },
    changesRequested: '#reviewCMODecision_changesRequestedByJudge',
  },

  selectSealCmo() {
    I.click(this.fields.reviewCmoRadioGroup.yes);
  },

  selectReturnCmoForChanges() {
    I.click(this.fields.reviewCmoRadioGroup.no);
  },

  enterChangesRequested(note='PBA number is incorrect') {
    I.fillField(this.fields.changesRequested, note);
  },
};
