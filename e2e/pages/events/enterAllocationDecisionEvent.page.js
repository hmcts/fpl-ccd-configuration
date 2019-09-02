const I = actor();

module.exports = {

  fields: {
    decisionProposalRadioGroup: '#decisionProposal_proposal',
    proposalReason: '#decisionProposal_proposalReason',
  },

  selectAllocationDecision(proposal) {
    I.waitForElement(this.fields.decisionProposalRadioGroup);
    within(this.fields.decisionProposalRadioGroup, () => {
      I.click(locate('label').withText(proposal));
    });
  },

  enterProposalReason(reason) {
    I.fillField(this.fields.proposalReason, reason);
  },
};
