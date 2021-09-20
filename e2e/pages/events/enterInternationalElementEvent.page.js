const { I } = inject();

module.exports = {

  fields: {
    possibleCarer: {
      yes: '#internationalElement_possibleCarer_Yes',
      textField: '#internationalElement_possibleCarerReason',
    },

    significantEvents: {
      yes: '#internationalElement_significantEvents_Yes',
      textField: '#internationalElement_significantEventsReason',
    },

    issues: {
      no: '#internationalElement_issues_No',
    },

    proceedings: {
      yes: '#internationalElement_proceedings_Yes',
      textField: '#internationalElement_proceedingsReason',
    },

    internationalAuthorityInvolvement: {
      yes: '#internationalElement_internationalAuthorityInvolvement_Yes',
      textField: '#internationalElement_internationalAuthorityInvolvementDetails',
    },
  },

  async fillForm() {
    I.click(this.fields.possibleCarer.yes);
    I.fillField(this.fields.possibleCarer.textField, 'test');
    I.click(this.fields.significantEvents.yes);
    I.fillField(this.fields.significantEvents.textField, 'test');
    I.click(this.fields.issues.no);
    I.click(this.fields.proceedings.yes);
    I.fillField(this.fields.proceedings.textField, 'test');
    I.click(this.fields.internationalAuthorityInvolvement.yes);
    I.fillField(this.fields.internationalAuthorityInvolvement.textField, 'International involvement reason');
    await I.runAccessibilityTest();
  },
};
