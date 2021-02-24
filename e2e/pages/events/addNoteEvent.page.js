const {I} = inject();

module.exports = {
  fields: {
    note: '#caseNote',
  },

  async addNote(note) {
    await I.runAccessibilityTest();
    console.log('add note 1');
    I.fillField(this.fields.note, note);
    await I.runAccessibilityTest();
    console.log('add note 2');
  },
};
