const {I} = inject();

module.exports = {
  fields: {
    note: '#caseNote',
  },

  async addNote(note) {
    await I.runAccessibilityTest();
    I.fillField(this.fields.note, note);
  },
};
