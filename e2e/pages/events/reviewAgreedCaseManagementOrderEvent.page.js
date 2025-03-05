const { I } = inject();

module.exports = {

  async selectCMOToReview(hearing) {
    I.waitForElement('#cmoToReviewList');
    I.selectOption('#cmoToReviewList', hearing);
    await I.runAccessibilityTest();
  },

  async selectSealCmo() {
    await I.runAccessibilityTest();
    I.click('#reviewCMODecision_decision-SEND_TO_ALL_PARTIES');
  },

  selectSealC21(index) {
    I.click(`#reviewDecision${index}_decision-SEND_TO_ALL_PARTIES`);
  },

  selectOthers(index) {
    I.checkOption(`#othersSelector_option${index}-SELECTED`);
  },

  async selectMakeChangesToCmo() {
    await I.runAccessibilityTest();
    I.click('No, I need to make changes');
  },

  selectReturnCmoForChanges() {
    I.click('No, the applicant needs to make changes');
  },

  selectReturnC21ForChanges(index) {
    I.click(`#reviewDecision${index}_decision-JUDGE_REQUESTED_CHANGES`);
  },

  enterChangesRequestedC21(index, note) {
    I.fillField(`#reviewDecision${index}_changesRequestedByJudge`, note);
  },

  enterChangesRequested(note) {
    I.fillField('#reviewCMODecision_changesRequestedByJudge', note);
  },

  uploadAmendedCmo(file) {
    I.attachFile('#reviewCMODecision_judgeAmendedDocument', file);
  },
};
