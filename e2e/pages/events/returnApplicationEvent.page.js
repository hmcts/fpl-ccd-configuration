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

  async selectApplicationIncorrect() {
    I.checkOption(this.fields.rejectionReasons.incorrect);
    await I.runAccessibilityTest();
  },

  async enterRejectionNote(note = 'PBA number is incorrect') {
    await I.runAccessibilityTest();
    I.fillField(this.fields.note, note);
  },
};
