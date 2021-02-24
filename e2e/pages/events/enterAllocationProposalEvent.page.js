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
    console.log('enter allocation proposal 1');
  },

  async enterProposalReason(reason) {
    await I.runAccessibilityTest();
    console.log('enter allocation proposal 2');
    I.fillField(this.fields.proposalReason, reason);
  },
};
