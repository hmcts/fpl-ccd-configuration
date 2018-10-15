const I = actor();

module.exports = {

  fields: {
    possibleCarer: {
      yes: '#internationalElements_possibleCarer-Yes',
      textField: '#internationalElements_possibleCarerReason',
    },

    significantEvents: {
      yes: '#internationalElements_significantEvents-Yes',
      textField: '#internationalElements_significantEventsReason',
    },

    issues: {
      no: '#internationalElements_issues-No',
    },

    proceedings: {
      yes: '#internationalElements_proceedings-Yes',
      textField: '#internationalElements_proceedingsReason',
    },
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
  },
};
