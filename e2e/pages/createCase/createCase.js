const I = actor();

module.exports = {

	fields: {
		jurisdiction: 'jurisdiction',
		caseType: 'case-type',
		event: 'event',
	},
	startButton: 'Start',

	createNewCase() {
		I.selectOption(this.fields.jurisdiction, 'Public Law DRAFT');
		I.selectOption(this.fields.caseType, 'Shared_Storage_DRAFT_v0.3');
		I.selectOption(this.fields.event, 'Initiate Case');
		I.click(this.startButton);
	}
};