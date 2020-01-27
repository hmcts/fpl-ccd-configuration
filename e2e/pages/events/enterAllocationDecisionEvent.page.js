const { I } = inject();

const allocationDecisionRadioGroup = '#allocationDecision_proposal';
const allocationDecisionRadioPrefix = 'allocationDecision_proposal-';
const proposalReason = '#allocationDecision_proposalReason';
const judgeLevelRadio = '#allocationDecision_judgeLevelRadio-';

module.exports = {

  selectAllocationDecision(proposal) {
    I.waitForElement(allocationDecisionRadioGroup);
    I.click(locate('input').withAttr({id: allocationDecisionRadioPrefix + proposal}));
  },

  enterProposalReason(reason) {
    I.fillField(proposalReason, reason);
  },

  selectCorrectLevelOfJudge(radioSelection) {
    I.click(judgeLevelRadio + radioSelection);
  },
};
