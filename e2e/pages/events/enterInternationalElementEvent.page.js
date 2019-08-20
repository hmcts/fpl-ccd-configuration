const I = actor();

module.exports = {

  fields: {
    possibleCarer: {
      yes: '#internationalElement_possibleCarer-Yes',
      textField: '#internationalElement_possibleCarerReason',
    },

    significantEvents: {
      yes: '#internationalElement_significantEvents-Yes',
      textField: '#internationalElement_significantEventsReason',
    },

    issues: {
      no: '#internationalElement_issues-No',
    },

    proceedings: {
      yes: '#internationalElement_proceedings-Yes',
      textField: '#internationalElement_proceedingsReason',
    },

    internationalAuthorityInvolvement: {
      yes: '#internationalElement_internationalAuthorityInvolvement-Yes',
      textField: '#internationalElement_internationalAuthorityInvolvementDetails',
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
    I.click(this.fields.internationalAuthorityInvolvement.yes);
    I.fillField(this.fields.internationalAuthorityInvolvement.textField, 'International involvement reason');
  },
};
