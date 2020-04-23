const { I } = inject();

module.exports = {

  fields: {
    caseDataFilename: '#caseDataFilename',
  },

  setCaseDataFilename(caseDataFilename) {
    I.fillField(this.fields.caseDataFilename, caseDataFilename);
  },
};
