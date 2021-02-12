const { I } = inject();

module.exports = {
  fields: {
    rejectionReasons: {
      incomplete: 'Application Incomplete',
      incorrect: 'Application Incorrect',
      clarificationNeeded: 'Clarification Needed',
    },
    note: '#returnApplication_note',
  },

  selectApplicationIncorrect() {
    I.checkOption(this.fields.rejectionReasons.incorrect);
    // I.runAccessibilityTest();
  },

  enterRejectionNote(note = 'PBA number is incorrect') {
    I.fillField(this.fields.note, note);
  },
};
