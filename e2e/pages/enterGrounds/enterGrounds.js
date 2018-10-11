const I = actor();

module.exports = {

	fields: {
		thresholdReason: {
			notReceivingCare: 'Not receiving care that would be reasonably expected from a parent',
			beyondControl: 'Beyond parental control'
		},
		thresholdDetails: '#grounds_thresholdDetails',
	},

	enterThresholdCriteriaDetails() {
		I.checkOption(this.fields.thresholdReason.notReceivingCare);
		I.fillField(this.fields.thresholdDetails, 'mock threshold details');
	}
};
