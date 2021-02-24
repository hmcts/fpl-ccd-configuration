const { I } = inject();

const proposalReason = '#allocationDecision_proposalReason';
const judgeLevelRadio = '#allocationDecision_judgeLevelRadio-';

module.exports = {

  async selectAllocationDecision(proposal) {
    await I.runAccessibilityTest();
    console.log('enter allocation decision 1');
    I.click(proposal);
  },

  async enterProposalReason(reason) {
    await I.runAccessibilityTest();
    console.log('enter allocation decision 1');
    I.fillField(proposalReason, reason);
  },

  selectCorrectLevelOfJudge(radioSelection) {
    I.click(judgeLevelRadio + radioSelection);
  },
};
