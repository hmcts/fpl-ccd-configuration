const I = actor();

module.exports = {

  fields: {
    allocationProposal: '#allocationProposal_proposal',
    proposalReason: '#allocationProposal_proposalReason',
  },

  selectAllocationProposal(proposal) {
    I.waitForElement(this.fields.allocationProposal);
    I.selectOption(this.fields.allocationProposal, proposal);
  },

  enterProposalReason(reason) {
    I.fillField(this.fields.proposalReason, reason);
  },
};
