const { I } = inject();

module.exports = {

  fields: {
    caseName: '#caseName',
  },

  async changeCaseName(caseName = 'Craigavon council v Smith') {
    //await I.runAccessibilityTest();
    I.fillField(this.fields.caseName, caseName);
  },
};
