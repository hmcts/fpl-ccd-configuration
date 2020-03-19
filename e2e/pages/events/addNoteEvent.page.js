const {I} = inject();

module.exports = {
  fields: {
    note: '#caseNote',
  },

  addNote() {
    I.fillField(this.fields.note, 'Example note');
  },
};
