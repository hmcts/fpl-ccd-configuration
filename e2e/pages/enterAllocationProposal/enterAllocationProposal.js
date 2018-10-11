const I = actor();

module.exports = {

	fields: {
		allocationProposal: '#allocationProposal_proposal',
		proposalReason: '#allocationProposal_proposalReason',
	},

	links: {
		presidentsGuidance: 'President\'s Guidance',
		schedule: 'Schedule',
	},

	selectAllocationProposal(proposal) {
		I.waitForElement(this.fields.allocationProposal);
		I.selectOption(this.fields.allocationProposal, proposal);
	},

	enterProposalReason(reason) {
		I.fillField(this.fields.proposalReason, reason);
	},

	clickDocumentLink(document) {
		if (document === this.links.presidentsGuidance) {
			I.click(this.links.presidentsGuidance);
		}
	}
};
