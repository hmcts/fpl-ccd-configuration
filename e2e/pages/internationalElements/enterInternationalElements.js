const I = actor();

module.exports = {

	fields: {

		possibleCarer: {
			yes: '#international_PossibleCarer-Yes',
			textField: '#international_PossibleCarerReason'
		},

		significantEvents: {
			yes: '#international_SignificantEvents-Yes',
			textField: '#international_SignificantEvents'
		},

		issues: {
			no: '#international_Issues-No',
		},

		proceedings: {
			yes: '#international_Proceedings',
			textField: '#international_Proceedings'
		}
	},

	halfFillForm() {
		I.click(this.fields.possibleCarer.yes);
		I.fillField(this.fields.possibleCarer.textField, 'test');
		I.click(this.fields.significantEvents.yes);
		I.fillField(this.fields.significantEvents.textField, 'test');
	},

	fillForm() {
		I.click(this.fields.possibleCarer.yes);
		I.fillField(this.fields.possibleCarer.textField, 'test');
		I.click(this.fields.significantEvents.yes);
		I.fillField(this.fields.significantEvents.textField, 'test');
		I.click(this.fields.issues.no);
		I.click(this.fields.proceedings.yes);
		I.fillField(this.fields.proceedings.textField, 'test');
	}
};
