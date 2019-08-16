const I = actor();

module.exports = {

  fields: {
    allocationProposalRadioGroup: '#allocationProposal_proposal',
    proposalReason: '#allocationProposal_proposalReason',
  },

  selectAllocationProposal(proposal) {
    I.waitForElement(this.fields.allocationProposalRadioGroup);
    within(this.fields.allocationProposalRadioGroup, () => {
      I.click(locate('label').withText(proposal));
    });
  },

  enterProposalReason(reason) {
    I.fillField(this.fields.proposalReason, reason);
  },
};
