const { I } = inject();

module.exports = {
  fields: {
    cmoToReviewList: '#cmoToReviewList',
    reviewCmoRadioGroup: {
      seal: 'Yes, seal and send to all parties',
      amend: 'No, I need to make changes',
      return: 'No, the local authority needs to make changes',
    },
    changesRequested: '#reviewCMODecision_changesRequestedByJudge',
    judgeAmendedDocument: '#reviewCMODecision_judgeAmendedDocument',
  },

  selectCMOToReview(date) {
    I.waitForElement(this.fields.cmoToReviewList);
    I.selectOption(this.fields.cmoToReviewList, `Case management hearing, ${date}`);
  },

  selectSealCmo() {
    I.click(this.fields.reviewCmoRadioGroup.seal);
  },

  selectMakeChangesToCmo() {
    I.click(this.fields.reviewCmoRadioGroup.amend);
  },

  selectReturnCmoForChanges() {
    I.click(this.fields.reviewCmoRadioGroup.return);
  },

  enterChangesRequested(note) {
    I.fillField(this.fields.changesRequested, note);
  },

  uploadAmendedCmo(file) {
    I.attachFile(this.fields.judgeAmendedDocument, file);
  },
};
