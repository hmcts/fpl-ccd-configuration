const {I} = inject();

module.exports = {

  fields: {
    allocationProposalRadioGroup: '#allocationProposal_proposal',
    proposalReason: '#allocationProposal_proposalReason',
  },

  async selectAllocationProposal(proposal) {
    I.waitForElement(this.fields.allocationProposalRadioGroup);
    I.click(proposal);
    await I.runAccessibilityTest();
  },

  enterProposalReason(reason) {
    I.fillField(this.fields.proposalReason, reason);
  },
};
