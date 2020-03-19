const {I} = inject();

module.exports = {
  fields: {
    note: '#caseNote',
  },

  addNote(note) {
    I.fillField(this.fields.note, note);
  },
};
