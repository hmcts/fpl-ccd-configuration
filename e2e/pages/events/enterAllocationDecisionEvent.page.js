const { I } = inject();

module.exports = {

  fields: {
    allocationDecisionRadioGroup: '#allocationDecision_proposal',
    proposalReason: '#allocationDecision_proposalReason',
  },

  selectAllocationDecision(proposal) {
    I.waitForElement(this.fields.allocationDecisionRadioGroup);
    within(this.fields.allocationDecisionRadioGroup, () => {
      I.click(locate('label').withText(proposal));
    });
  },

  enterProposalReason(reason) {
    I.fillField(this.fields.proposalReason, reason);
  },
};
