const { I } = inject();

const proposalReason = '#allocationDecision_proposalReason';
const judgeLevelRadio = '#allocationDecision_judgeLevelRadio-';

module.exports = {

  async selectAllocationDecision(proposal) {
    //await I.runAccessibilityTest();
    I.click(proposal);
  },

  enterProposalReason(reason) {
    I.fillField(proposalReason, reason);
  },

  selectCorrectLevelOfJudge(radioSelection) {
    I.click(judgeLevelRadio + radioSelection);
  },
};
