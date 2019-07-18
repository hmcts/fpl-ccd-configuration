const I = actor();

module.exports = {

  fields: {
    deleteCheckbox: '#delete-Delete',
  },

  delete() {
    I.checkOption(this.fields.deleteCheckbox);
  },
};
