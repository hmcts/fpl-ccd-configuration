const {I} = inject();

module.exports = {

  fields: {
    allocationProposalRadioGroup: '#allocationProposal_proposal',
    proposalReason: '#allocationProposal_proposalReason',
  },

  selectAllocationProposal(proposal) {
    I.waitForElement(this.fields.allocationProposalRadioGroup);
    I.click(proposal);
    //I.runAccessibilityTest();
  },

  enterProposalReason(reason) {
    I.fillField(this.fields.proposalReason, reason);
  },
};
