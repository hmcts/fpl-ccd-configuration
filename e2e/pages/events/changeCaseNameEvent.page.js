const I = actor();

module.exports = {

  fields: {
    caseName: '#caseName',
  },

  changeCaseName(caseName = 'Craigavon council v Smith') {
    I.fillField(this.fields.caseName, caseName);
  },
};
