const { I } = inject();

const proposalReason = '#allocationDecision_proposalReason';
const judgeLevelRadio = '#allocationDecision_judgeLevelRadio_';

module.exports = {

  async selectAllocationDecision(proposal) {
    I.click(proposal);
  },

  async enterProposalReason(reason) {
    await I.runAccessibilityTest();
    I.fillField(proposalReason, reason);
  },

  selectCorrectLevelOfJudge(radioSelection) {
    I.click(judgeLevelRadio + radioSelection);
  },
};
