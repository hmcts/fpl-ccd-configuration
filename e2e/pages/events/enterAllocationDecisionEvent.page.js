const { I } = inject();

const proposalReason = '#allocationDecision_proposalReason';
const judgeLevelRadio = '#allocationDecision_judgeLevelRadio-';

module.exports = {

  selectAllocationDecision(proposal) {
    I.click(proposal);
    I.runAccessibilityTest();
  },

  enterProposalReason(reason) {
    I.fillField(proposalReason, reason);
  },

  selectCorrectLevelOfJudge(radioSelection) {
    I.click(judgeLevelRadio + radioSelection);
  },
};
