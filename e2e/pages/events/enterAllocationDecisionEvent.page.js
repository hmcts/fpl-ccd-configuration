const I = actor();
const allocationDecisionRadioGroup = '#allocationDecision_proposal';
const allocationDecisionRadioPrefix = 'allocationDecision_proposal-';
const proposalReason = '#allocationDecision_proposalReason';

module.exports = {

  selectAllocationDecision(proposal) {
    I.waitForElement(allocationDecisionRadioGroup);
    I.click(locate('input').withAttr({id: allocationDecisionRadioPrefix + proposal}));
  },

  enterProposalReason(reason) {
    I.fillField(proposalReason, reason);
  },
};
